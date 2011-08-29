/*
 * Copyright 2011 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tdwg.dwca.wikipedia;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.metadata.handler.SimpleSaxHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.Paragraph;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Section;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContainer;
import de.tudarmstadt.ukp.wikipedia.parser.Template;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.ModularParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.ResolvedTemplate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * parses page name & id
 * and delegates to text parser
 * writes immediately every taxon to the dwca writer
 */
public class WikipediaSaxHandler extends SimpleSaxHandler{
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final String lang;
  private Integer taxonCount = 0;
  private final DwcaWriter writer;
  private Integer pageID;
  private String title;
  private String text;
  private String timestamp;
  // wikipedia text parser
  private MediaWikiParserFactory pf;
  private ModularParser parser;
  private TaxonTemplateParser taxonParser;
  //
  private final TermFactory termFactory;
  private final ConceptTerm termFossil;
  private final ConceptTerm termTrend;
  private final ConceptTerm wikipediaImage;
  private final ConceptTerm wikipediaThumb;

  private static final Pattern NON_TEXT = Pattern.compile("^(#|\\{\\{)(redirect|weiterleitung)]", Pattern.CASE_INSENSITIVE);

  public WikipediaSaxHandler(DwcaWriter writer, String lang) {
    this.lang=lang.toLowerCase();
    pf = new MediaWikiParserFactory();
    parser = (ModularParser) pf.createParser();
    taxonParser = new TaxonTemplateParser();
    parser.setTemplateParser(taxonParser);
    this.writer = writer;
    termFactory = new TermFactory();
    termFossil = termFactory.findTerm("http://wikipedia.org/taxon/fossilRange");
    termTrend = termFactory.findTerm("http://wikipedia.org/taxon/trend");
    wikipediaImage = termFactory.findTerm("http://wikipedia.org/image/link");
    wikipediaThumb = termFactory.findTerm("http://wikipedia.org/image/thumbnail");
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    super.startElement(uri, localName, qName, attributes);
    // start new page?
    if (localName.equalsIgnoreCase("page")) {
      // reset props
      pageID = null;
      title = null;
      text = null;
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    super.endElement(uri, localName, qName);
    // end page?
    if (pageID==null && localName.equalsIgnoreCase("id")){
      // reset id
      try {
        pageID=Integer.parseInt(this.content);
      } catch (NumberFormatException e) {
        // swallow
      }
    } else if (localName.equalsIgnoreCase("title")) {
      // reset id
      title = content;
    } else if (localName.equalsIgnoreCase("timestamp")) {
      // <timestamp > 2010 - 08 - 26T22:38:36Z</timestamp >
      timestamp = StringUtils.trimToNull(content);
    }
    else if (localName.equalsIgnoreCase("text")) {
      // text to parse
      text = content;
    }

    else if (localName.equalsIgnoreCase("page")) {
      // get a ParsedPage object
      if (text!=null){
        ParsedPage pp = null;
        if (!NON_TEXT.matcher(text).find()){
          try {
            pp = parser.parse(text);
          } catch (Exception e) {
            // catch parser errors - library isn't 100% fail proof it seems
            log.warn("Failed to parse page {}-{}: " + e.getMessage(), pageID, title);
          }
        }
        if (pp != null) {
          TaxonInfo taxon = taxonParser.getLastTaxon();
          if (taxon != null) {
            // this is a taxon page !!!
            try {
              processTaxonPage(pp, taxon);
            } catch (IOException e) {
              log.error("IOException when processing taxon page", e);
            }
          }
        }
      }
    }
  }

  private void processTaxonPage(ParsedPage pp, TaxonInfo taxon) throws IOException {
    if (taxon.getScientificName()==null){
      log.warn("No scientific name found for taxon page {}", WikipediaUtils.getWikiLink(lang, title));
      return;
    }
    taxonCount++;
    log.debug("Processing #"+taxonCount+" {} : {}", WikipediaUtils.getWikiLink(lang, title), taxon.getScientificName());
    // write core record
    writer.newRecord(pageID.toString());
    writer.addCoreColumn(DcTerm.source, WikipediaUtils.getWikiLink(lang, title));
    writer.addCoreColumn(DcTerm.modified, timestamp);
    writer.addCoreColumn(DwcTerm.scientificName, taxon.getScientificName());
    writer.addCoreColumn(DwcTerm.scientificNameAuthorship, taxon.getScientificNameAuthorship());
    writer.addCoreColumn(DwcTerm.kingdom, taxon.getKingdom());
    writer.addCoreColumn(DwcTerm.phylum, taxon.getPhylum());
    writer.addCoreColumn(DwcTerm.classs, taxon.getClassis());
    writer.addCoreColumn(DwcTerm.order, taxon.getOrder());
    writer.addCoreColumn(DwcTerm.family, taxon.getFamily());
    writer.addCoreColumn(DwcTerm.genus, taxon.getGenus());
    writer.addCoreColumn(DwcTerm.subgenus, taxon.getSubgenus());
    writer.addCoreColumn(DwcTerm.higherClassification, taxon.getHigherClassification());
    // other non core
    writer.addCoreColumn(DwcTerm.vernacularName, taxon.getName());
    writer.addCoreColumn(termTrend, taxon.getTrend());
    writer.addCoreColumn(termFossil, taxon.getFossilRange());

    Map<ConceptTerm, String> row;

    // vernacular name extension
    if (!StringUtils.isBlank(taxon.getName()) && !taxon.getName().equalsIgnoreCase(taxon.getScientificName())){
      row = new HashMap<ConceptTerm, String>();
      row.put(DwcTerm.vernacularName, taxon.getName());
      row.put(DcTerm.language, lang);
      row.put(GbifTerm.isPreferredName, "true");
      writer.addExtensionRecord(GbifTerm.VernacularName, row);
    }
    // also use language links as common names
    if (pp.getLanguagesElement()!=null){
      for (Link l : pp.getLanguages()) {
        // de:Puma
        String vern = l.getText();
        if (vern!=null && vern.contains(":")){
          String[] splitVern = StringUtils.split(org.apache.commons.lang3.StringUtils.trimToEmpty(vern), ":", 2);
          if (splitVern.length==2 && splitVern[0].length()==2 && !splitVern[1].equalsIgnoreCase(taxon.getScientificName())){
            row = new HashMap<ConceptTerm, String>();
            row.put(DwcTerm.vernacularName, splitVern[1]);
            row.put(DcTerm.language, splitVern[0]);
            writer.addExtensionRecord(GbifTerm.VernacularName, row);
          }
        }
      }
    }

    // species profile extension
    String fr = taxon.getFossilRange();
    if (fr!=null) {
      row = new HashMap<ConceptTerm, String>();
      row.put(GbifTerm.livingPeriod, fr);
      writer.addExtensionRecord(GbifTerm.SpeciesProfile, row);
    }

    // distribution extension
    for (Image image : taxon.getRangeMaps()){
      if (!StringUtils.isBlank(image.getImage())) {
        row = new HashMap<ConceptTerm, String>();
        row.put(wikipediaImage, WikipediaUtils.getImageLink(image.getImage()));
        row.put(wikipediaThumb, WikipediaUtils.getImageThumbnailLink(image.getImage()));
        row.put(DwcTerm.locality, image.getImageCaption());
        writer.addExtensionRecord(GbifTerm.Distribution, row);
      }
    }

    // image extension
    for (Image image : taxon.getImages()) {
      if (!StringUtils.isBlank(image.getImage())) {
        row = new HashMap<ConceptTerm, String>();
        row.put(DcTerm.identifier, WikipediaUtils.getImageLink(image.getImage()));
        row.put(DcTerm.title, image.getImageCaption());
        row.put(wikipediaThumb, WikipediaUtils.getImageThumbnailLink(image.getImage()));
        writer.addExtensionRecord(GbifTerm.Image, row);
      }
    }

    // description extension
    for (Section section : pp.getSections()) {
      row = new HashMap<ConceptTerm, String>();

      boolean isFirst=true;
      StringBuilder description = new StringBuilder();
      List<Paragraph> paras = section.getParagraphs();
      for (Paragraph p : paras) {
        if (!isFirst){
          description.append("\\n");
        }
        String paraText = StringUtils.trimToNull(p.getText());
        if (paraText!=null){
          paraText.replaceAll(" thumb\\|"," ");
          description.append(paraText);
          isFirst=false;
        }
      }

      row.put(DcTerm.type, section.getTitle()==null ? "General": section.getTitle());
      row.put(DcTerm.description, description.toString());
      row.put(DcTerm.language, lang);
      writer.addExtensionRecord(GbifTerm.Description, row);

    }


    // create individual records for each synonym
    int synIdx = 1;
    for (String synonym : taxon.getSynonyms()){
      String synID = pageID+"-syn"+ synIdx;
      writer.newRecord(synID);

      writer.addCoreColumn(DwcTerm.scientificName, synonym);
      writer.addCoreColumn(DwcTerm.acceptedNameUsage, taxon.getScientificName());
      writer.addCoreColumn(DwcTerm.acceptedNameUsageID, pageID.toString());
      writer.addCoreColumn(DwcTerm.taxonomicStatus, "synonym");
      writer.addCoreColumn(DwcTerm.kingdom, taxon.getKingdom());
      synIdx++;
    }

  }


}
