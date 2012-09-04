package org.tdwg.dwca.wikipedia;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.dwc.text.DwcaWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import info.bliki.wiki.filter.PlainTextConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdwg.dwca.wikipedia.taxonbox.Image;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonInfo;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonboxWikiModel;
import org.xml.sax.SAXException;

public class TaxonboxHandler implements IArticleFilter {

  private final Logger log = LoggerFactory.getLogger(TaxonboxHandler.class);
  private final String lang;
  private final DwcaWriter writer;
  private Integer taxonCount = 0;
  // extra terms
  private final TermFactory termFactory;
  private final ConceptTerm termFossil;
  private final ConceptTerm termTrend;
  private final ConceptTerm wikipediaImage;
  private final ConceptTerm wikipediaThumb;
  private final ConceptTerm taxobox;
  private final TaxonboxWikiModel wikiModel;
  private Writer noNamesFoundWriter;

  private final Pattern SPLIT_SECTIONS = Pattern.compile("(?<!=)==([^=]+)==");
  private final Pattern REMOVE_FUNCTIONS = Pattern.compile("\\{\\{[a-zA-Z0-9-_ ]*\\}\\}");
  private final Pattern REMOVE_FOOTNOTES = Pattern.compile("\\[[0-9]{1,2}\\]");
  private final Set<String> IGNORE_SETIONS = Sets.newHashSet("see also", "references", "further reading",
    "external links", "reflist");
  private final Pattern EXTRACT_VERNACULARS = Pattern.compile("\\[\\[([a-z]{2,3}):([^\\]\\[]+)\\]\\]");
  private final Pattern REDIRECT = Pattern.compile("^.REDIRECT", Pattern.CASE_INSENSITIVE);

  public TaxonboxHandler(String lang, DwcaWriter writer) {
    this.writer = writer;
    this.lang = lang.toLowerCase();
    wikiModel = new TaxonboxWikiModel(lang);
    termFactory = new TermFactory();
    termFossil = termFactory.findTerm("http://wikipedia.org/taxon/fossilRange");
    termTrend = termFactory.findTerm("http://wikipedia.org/taxon/trend");
    wikipediaImage = termFactory.findTerm("http://wikipedia.org/image/link");
    wikipediaThumb = termFactory.findTerm("http://wikipedia.org/image/thumbnail");
    taxobox = termFactory.findTerm("http://wikipedia.org/taxobox");
    try {
      File noNames = File.createTempFile("wikipedia", "noname.txt");
      noNamesFoundWriter = new FileWriter(noNames);
      log.warn("Log taxa without scientific name into {}", noNames.getAbsolutePath());
    } catch (IOException e) {
      log.warn("Cant create no names writer: {}", e.getMessage());
    }
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
      plain = REMOVE_FUNCTIONS.matcher(plain).replaceAll(" ").trim();
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

    taxon.postprocess();

    if (taxon.getScientificName() == null) {
      log.warn("No scientific name found for taxon page {}", WikipediaUtils.getWikiLink(lang, page.getTitle()));
      noNamesFoundWriter.write(page.getTitle());
      noNamesFoundWriter.write("\n\n");
      noNamesFoundWriter.write(page.getText());
      noNamesFoundWriter.write("\n\n==========\n\n");
      return;
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
      row.put(DcTerm.language, lang);
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
      row.put(GbifTerm.isExtinct, taxon.getExtinct());
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
        row = Maps.newHashMap();
        row.put(DcTerm.identifier, WikipediaUtils.getImageLink(image.getUrl()));
        row.put(DcTerm.title, image.getImageCaption());
        row.put(wikipediaThumb, WikipediaUtils.getImageThumbnailLink(image.getUrl()));
        writer.addExtensionRecord(GbifTerm.Image, row);
      }
    }

    // description extension
    for (Map.Entry<String, String> section : sections.entrySet()) {
      row = Maps.newHashMap();
      row.put(DcTerm.type, section.getKey());
      row.put(DcTerm.description, section.getValue());
      row.put(DcTerm.language, lang);
      writer.addExtensionRecord(GbifTerm.Description, row);
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

  public TaxonboxWikiModel getWikiModel() {
    return wikiModel;
  }
}
