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

import com.beust.jcommander.JCommander;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import info.bliki.wiki.dump.WikiXMLParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.gbif.api.model.registry.Contact;
import org.gbif.api.model.registry.Dataset;
import org.gbif.api.model.registry.eml.DataDescription;
import org.gbif.api.vocabulary.ContactType;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.Language;
import org.gbif.dwc.DwcaWriter;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.registry.metadata.EMLWriter;
import org.gbif.utils.HttpClient;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.CompressionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Date;
import java.util.Map;


public class ChecklistBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(ChecklistBuilder.class);
  private final HttpClient http;
  private DwcaWriter writer;

  private WikipediaConfig cfg;
  private Date modifiedDate;
  private URL url;

  public ChecklistBuilder(WikipediaConfig cfg) {
    this.cfg = cfg;
    this.http = HttpUtil.newMultithreadedClient(1000 * 60 * 60 * 10, 5, 5);
  }

  public void run() {
    // download file?
    try {
      if (cfg.offline) {
        LOG.info("Offline mode, use existing dump file {}", cfg.getDumpFile());
      } else {
        download();
      }
      parse(cfg.getDumpFile());
      LOG.info("wikipedia archive created");

    } catch (IOException e) {
      LOG.error("Error creating the wikipedia archive", e);
    }
  }

  private void download() throws IOException {
    final File wikiDumpBz = cfg.getDumpFile();
    URL url = cfg.getWikipediaDumpUrl();
    LOG.info("Downloading latest wikipedia dump from " + url.toString());
    boolean success = http.downloadIfChanged(url, wikiDumpBz);
    if (success){
      LOG.info("Downloaded new wikipedia dump");
    } else{
      LOG.info("No newer wikipedia dump, use existing copy");
    }
  }

  private void parse(File wikiDumpBz) throws IOException{
    modifiedDate = new Date(wikiDumpBz.lastModified());
    // new writer
    File dwcaDir = org.gbif.utils.file.FileUtils.createTempDir("wikipedia-", "-dwca");
    LOG.info("Writing archive files to temporary folder "+dwcaDir);
    writer = new DwcaWriter(DwcTerm.Taxon, dwcaDir);

    // parse file
    LOG.info("Parsing dump file {}", wikiDumpBz.getAbsolutePath());
    TaxonboxHandler handler = new TaxonboxHandler(cfg, http, writer, new File(cfg.repo, "missing_licenses-"+cfg.lang+".txt"));
    try {
      WikiXMLParser wxp = new WikiXMLParser(wikiDumpBz, handler);
      wxp.parse();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      LOG.info("Unknown Taxoninfo properties: {}", handler.getWikiModel().getUnknownProperties());
      for (Map.Entry<String, Integer> tmpl : handler.getWikiModel().getUnknownTemplatesCounter().entrySet()) {
        LOG.debug("Unknown template >>{}<< {}x {}", tmpl.getKey(), tmpl.getValue(), handler.getWikiModel().getUnknownTemplates().get(tmpl.getKey()));
      }
    }

    // finish archive and zip it
    final File dwcaFile = cfg.getDwcaFile();
    LOG.info("Bundling archive at {}", dwcaFile);
    writer.setMetadata(buildEml(), "eml.xml");
    writer.close(); // adds eml and meta.xml file

    if (dwcaFile.exists()) {
      LOG.info("Delete existing archive {}", dwcaFile);
      dwcaFile.delete();
    } else {
      FileUtils.forceMkdir(dwcaFile.getParentFile());
    }
    LOG.info("Bundling archive at {}", dwcaFile);
    CompressionUtil.zipDir(dwcaDir, dwcaFile);

    // remove temp folder
    if (!cfg.keepTmp) {
      LOG.info("Remove temp working dir {}", dwcaDir);
      FileUtils.deleteDirectory(dwcaDir);
    }
    LOG.info("Wikipedia dwc archive completed at {} !", dwcaFile);
  }

  private String buildEml() throws IOException {
    Dataset dataset = new Dataset();
    dataset.setTitle(cfg.lang.getTitleEnglish() + " Wikipedia - Species Pages");
    dataset.setLanguage(Language.ENGLISH);
    dataset.setDataLanguage(cfg.lang);
    String description = Resources.toString(Resources.getResource("description.txt"), Charsets.UTF_8);
    dataset.setDescription(description
        .replaceAll("\\$LANG", cfg.lang.getTitleEnglish())
        .replaceAll("\\$DATE", DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(modifiedDate))
    );
    dataset.setPubDate(modifiedDate);
    dataset.setHomepage(cfg.getWikipediaHomepage());
    addDeveloper(dataset, ContactType.METADATA_AUTHOR, ContactType.ORIGINATOR,
      ContactType.ADMINISTRATIVE_POINT_OF_CONTACT);

    DataDescription d = new DataDescription();
    d.setUrl(cfg.getWikipediaDumpUri());
    d.setFormat("XML");
    d.setName("Wikipedia Article Dump");
    dataset.getDataDescriptions().add(d);

    StringWriter sw = new StringWriter();
    EMLWriter.newInstance().writeTo(dataset, sw);
    sw.close();
    return sw.toString();
  }

  private void addDeveloper(Dataset dataset, ContactType... roles){
    for (ContactType role : roles) {
      Contact markus = new Contact();
      markus.addEmail("mdoering@gbif.org");
      markus.setFirstName("Markus");
      markus.setLastName("Döring");
      markus.setCity("Berlin");
      markus.setCountry(Country.GERMANY);
      markus.setType(role);
      dataset.getContacts().add(markus);
    }
  }

  public static void main (String[] args) {
    WikipediaConfig cfg = new WikipediaConfig();
    new JCommander(cfg, args);
    new ChecklistBuilder(cfg).run();
  }
}
