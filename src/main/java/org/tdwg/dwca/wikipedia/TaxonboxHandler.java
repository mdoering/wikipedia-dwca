package org.tdwg.dwca.wikipedia;

import org.gbif.api.vocabulary.Language;
import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.dwc.text.DwcaWriter;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import info.bliki.wiki.filter.PlainTextConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdwg.dwca.wikipedia.taxonbox.Image;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonInfo;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonInfoDE;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonInfoEN;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonInfoES;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonInfoFR;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonboxWikiModel;
import org.xml.sax.SAXException;

public class TaxonboxHandler implements IArticleFilter {

  private final static Logger log = LoggerFactory.getLogger(TaxonboxHandler.class);
  private final Language lang;
  private final DwcaWriter writer;
  private Integer taxonCount = 0;
  private final WikimediaScraper imgScraper;
  private static final String TEXT_LICENSE = "CC-BY-SA 3.0";

  // extra terms
  private final TermFactory termFactory;
  private final ConceptTerm termFossil;
  private final ConceptTerm termTrend;
  private final ConceptTerm wikipediaImage;
  private final ConceptTerm wikipediaThumb;
  private final ConceptTerm taxobox;
  private final TaxonboxWikiModel wikiModel;

  private final Pattern SPLIT_SECTIONS = Pattern.compile("(?<!=)==([^=]+)==");
  private final Pattern REMOVE_TEMPLATES = Pattern.compile("\\{\\{[a-zA-Z0-9-_ ]*\\}\\}");
  private final Pattern REMOVE_FOOTNOTES = Pattern.compile("\\[[0-9]{1,2}\\]");
  private final Set<String> IGNORE_SETIONS = ImmutableSet.<String>builder()
    .addAll(TaxonInfoEN.IGNORE_SETIONS)
    .addAll(TaxonInfoDE.IGNORE_SETIONS)
    .addAll(TaxonInfoES.IGNORE_SETIONS)
    .addAll(TaxonInfoFR.IGNORE_SETIONS)
    .build();

  private final Pattern EXTRACT_VERNACULARS = Pattern.compile("\\[\\[([a-z]{2,3}):([^\\]\\[]+)\\]\\]");
  private final Pattern REDIRECT = Pattern.compile("^.REDIRECT", Pattern.CASE_INSENSITIVE);

  public TaxonboxHandler(String lang, DwcaWriter writer, File missingLicenseFile) throws IOException {
    this.writer = writer;
    this.lang = Language.fromIsoCode(lang);
    if (lang == null) {
      throw new IllegalArgumentException("Language {} not understood. Please use iso 2 or 3 character codes");
    }
    imgScraper = new WikimediaScraper(missingLicenseFile);
    wikiModel = new TaxonboxWikiModel(lang);
    termFactory = new TermFactory();
    termFossil = termFactory.findTerm("http://wikipedia.org/taxon/fossilRange");
    termTrend = termFactory.findTerm("http://wikipedia.org/taxon/trend");
    wikipediaImage = termFactory.findTerm("http://wikipedia.org/image/link");
    wikipediaThumb = termFactory.findTerm("http://wikipedia.org/image/thumbnail");
    taxobox = termFactory.findTerm("http://wikipedia.org/taxobox");
  }

  @Override
  public void process(WikiArticle page, Siteinfo siteinfo) throws SAXException {
    wikiModel.reset();
    // ignore categories, templates, etc. Only process main articles
    if (page.isMain() && !REDIRECT.matcher(page.getText()).find()) {
      LinkedHashMap<String, String> sections = splitPage(page);

      if (wikiModel.isSpeciesPage()) {
        try {
          processTaxonPage(page, wikiModel.getTaxonInfo(), sections);
        } catch (IOException e) {
          log.error("IOException writing taxon page {}", page.getTitle());
        }
      }
    }
  }

  private LinkedHashMap<String, String> splitPage(WikiArticle page) {
    LinkedHashMap<String, String> sections = Maps.newLinkedHashMap();
    Matcher m = SPLIT_SECTIONS.matcher(page.getText());
    String title = "Abstract";
    int lastIndex = 0;
    while (m.find()) {
      String body = page.getText().substring(lastIndex, m.start());
      addSection(sections, title, body);
      // if we havent found a taxonbox in the first section break out
      if (!wikiModel.isSpeciesPage()) {
        return sections;
      }
      // title of the next section
      title = m.group(1);
      lastIndex = m.end();
    }
    String body = page.getText().substring(lastIndex);
    addSection(sections, title, body);

    return sections;
  }

  private void extractVernacularNames(String body) {
    if (wikiModel.isSpeciesPage()) {
      Matcher m = EXTRACT_VERNACULARS.matcher(body);
      while (m.find()) {
        wikiModel.getTaxonInfo().getVernacularNames().put(m.group(1), m.group(2).trim());
      }
    }
  }

  private void addSection(Map<String, String> sections, String title, String body) {
    String titleNormed = wikiModel.render(new PlainTextConverter(), title).trim();
    if (!Strings.isNullOrEmpty(titleNormed) && !IGNORE_SETIONS.contains(titleNormed.toLowerCase()) && !titleNormed.toLowerCase().startsWith("additional ")) {
      String plain = wikiModel.render(new PlainTextConverter(), body);
      // replace remaining {{xyz}}
      plain = REMOVE_TEMPLATES.matcher(plain).replaceAll(" ").trim();
      // remove [1], [2] etc
      //plain = REMOVE_FOOTNOTES.matcher(plain).replaceAll(" ");
      // replace newlines with <br/>
      plain = plain.replaceAll("\n *\n", "<br/><br/>");

      if (!Strings.isNullOrEmpty(plain)) {
        sections.put(titleNormed, plain);
      }
    }
    // discover vernacular name links
    extractVernacularNames(body);
  }

  private void processTaxonPage(WikiArticle page, TaxonInfo taxon, LinkedHashMap<String, String> sections) throws IOException {

    taxon.postprocess(page, lang);

    if (taxon.getScientificName() == null) {
      log.info("No scientific name found in infobox of page {}. Using article title {} as name", WikipediaUtils.getWikiLink(lang, page.getTitle()), page.getTitle());
      taxon.setScientificName(page.getTitle());
    }

    taxonCount++;
    log.debug("Processing #" + taxonCount + " {}: {}", WikipediaUtils.getWikiLink(lang, page.getTitle()), taxon.getScientificName());
    // write core record
    writer.newRecord(page.getId());
    writer.addCoreColumn(DcTerm.source, WikipediaUtils.getWikiLink(lang, page.getTitle()));
    writer.addCoreColumn(DcTerm.modified, page.getTimeStamp());
    writer.addCoreColumn(DwcTerm.scientificName, taxon.getScientificName());
    writer.addCoreColumn(DwcTerm.scientificNameAuthorship, taxon.getScientificNameAuthorship());
    if (taxon.getRank() != null) {
      writer.addCoreColumn(DwcTerm.taxonRank, taxon.getRank().name());
    }
    writer.addCoreColumn(DwcTerm.verbatimTaxonRank, taxon.getRankVerbatim());
    writer.addCoreColumn(DwcTerm.kingdom, taxon.getKingdom());
    writer.addCoreColumn(DwcTerm.phylum, taxon.getPhylum());
    writer.addCoreColumn(DwcTerm.classs, taxon.getClazz());
    writer.addCoreColumn(DwcTerm.order, taxon.getOrder());
    writer.addCoreColumn(DwcTerm.family, taxon.getFamily());
    writer.addCoreColumn(DwcTerm.genus, taxon.getGenus());
    writer.addCoreColumn(DwcTerm.subgenus, taxon.getSubgenus());
    // other non core
    writer.addCoreColumn(termTrend, taxon.getTrend());
    writer.addCoreColumn(termFossil, taxon.getFossilRange());
    writer.addCoreColumn(taxobox, taxon.getRawParams().toString());

    Map<ConceptTerm, String> row;

    // vernacular name extension
    for (String vname : taxon.getVernacularNamesInDefaultLang()) {
      if (Strings.isNullOrEmpty(vname) || vname.equalsIgnoreCase(taxon.getScientificName())) {
        continue;
      }
      row = Maps.newHashMap();
      row.put(DwcTerm.vernacularName, vname);
      row.put(DcTerm.language, lang.getIso2LetterCode());
      row.put(GbifTerm.isPreferredName, "true");
      writer.addExtensionRecord(GbifTerm.VernacularName, row);
    }

    // other languages
    for (Map.Entry<String, String> vn : taxon.getVernacularNames().entrySet()) {
      if (Strings.isNullOrEmpty(vn.getValue()) || vn.getValue().equalsIgnoreCase(taxon.getScientificName())) {
        continue;
      }
      row = Maps.newHashMap();
      row.put(DwcTerm.vernacularName, vn.getValue());
      row.put(DcTerm.language, vn.getKey());
      writer.addExtensionRecord(GbifTerm.VernacularName, row);
    }

    // species profile extension
    String fr = taxon.getFossilRange();
    if (fr != null) {
      row = Maps.newHashMap();
      row.put(GbifTerm.livingPeriod, fr);
      if (taxon.getExtinct() != null) {
        row.put(GbifTerm.isExtinct, taxon.getExtinct());
      } else {
        row.put(GbifTerm.isExtinct, String.valueOf(taxon.getExtinctSymbol()));
      }
      writer.addExtensionRecord(GbifTerm.SpeciesProfile, row);
    }

    // distribution extension
    //TODO: publish distribution maps as images or html formatted textual descriptions???
    for (Image image : taxon.getRangeMaps()) {
      if (!StringUtils.isBlank(image.getUrl())) {
        row = Maps.newHashMap();
        row.put(wikipediaImage, WikipediaUtils.getImageLink(image.getUrl()));
        row.put(wikipediaThumb, WikipediaUtils.getImageThumbnailLink(image.getUrl()));
        row.put(DwcTerm.locality, image.getImageCaption());
        writer.addExtensionRecord(GbifTerm.Distribution, row);
      }
    }

    // image extension
    for (Image image : taxon.getImages()) {
      if (!StringUtils.isBlank(image.getUrl())) {
        image = imgScraper.scrape(image);
        row = Maps.newHashMap();
        row.put(DcTerm.identifier, WikipediaUtils.getImageLink(image.getUrl()));
        row.put(DcTerm.references, WikipediaUtils.getImageWikiLink(image.getUrl()));
        row.put(DcTerm.title, image.getImageCaption());
        row.put(DcTerm.creator, image.getAuthor());
        row.put(DcTerm.created, image.getDate());
        row.put(DcTerm.license, image.getLicense());
        row.put(DcTerm.publisher, image.getPublisher());
        row.put(DcTerm.source, image.getSource());
        row.put(wikipediaThumb, WikipediaUtils.getImageThumbnailLink(image.getUrl()));
        row.put(DcTerm.description, image.getDescription());
        writer.addExtensionRecord(GbifTerm.Image, row);
      }
    }

    // description extension
    for (Map.Entry<String, String> section : sections.entrySet()) {
      row = Maps.newHashMap();
      row.put(DcTerm.type, section.getKey());
      row.put(DcTerm.description, section.getValue());
      row.put(DcTerm.language, lang.getIso2LetterCode());
      row.put(DcTerm.license, TEXT_LICENSE);
      writer.addExtensionRecord(GbifTerm.Description, row);
    }


    // types extension
    if (!Strings.isNullOrEmpty(taxon.getTypeSpecies())) {
      row = Maps.newHashMap();
      row.put(DwcTerm.typeStatus, "type species");
      row.put(DwcTerm.scientificName, concatSciName(taxon.getTypeSpecies(), taxon.getTypeSpeciesAuthority()));
      writer.addExtensionRecord(GbifTerm.TypesAndSpecimen, row);

    } else if (Strings.isNullOrEmpty(taxon.getTypeGenus())) {
      row = Maps.newHashMap();
      row.put(DwcTerm.typeStatus, "type genus");
      row.put(DwcTerm.scientificName, concatSciName(taxon.getTypeGenus(), taxon.getTypeGenusAuthority()));
      writer.addExtensionRecord(GbifTerm.TypesAndSpecimen, row);
    }


    // create individual records for each synonym
    int synIdx = 1;
    for (String synonym : taxon.getSynonyms()) {
      String synID = page.getId() + "-syn" + synIdx;
      writer.newRecord(synID);

      writer.addCoreColumn(DwcTerm.scientificName, synonym);
      writer.addCoreColumn(DwcTerm.acceptedNameUsage, taxon.getScientificName());
      writer.addCoreColumn(DwcTerm.acceptedNameUsageID, page.getId());
      writer.addCoreColumn(DwcTerm.taxonomicStatus, "synonym");
      synIdx++;
    }
  }

  private String concatSciName(String name, String authority) {
    if (!Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(authority) && !name.toLowerCase().contains(
      authority.toLowerCase())) {
      return name + " " + authority;
    }
    return name;
  }

  public TaxonboxWikiModel getWikiModel() {
    return wikiModel;
  }
}
