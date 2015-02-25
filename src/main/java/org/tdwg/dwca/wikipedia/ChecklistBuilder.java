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

import org.gbif.api.model.registry.Contact;
import org.gbif.api.model.registry.Dataset;
import org.gbif.api.model.registry.eml.DataDescription;
import org.gbif.api.vocabulary.ContactType;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.Language;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.DwcaWriter;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.CompressionUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import info.bliki.wiki.dump.WikiXMLParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChecklistBuilder {
  private Logger log = LoggerFactory.getLogger(ChecklistBuilder.class);
  private DwcaWriter writer;

  private final WikipediaConfig cfg;
  private Date modifiedDate;
  private URL url;

  public ChecklistBuilder(WikipediaConfig cfg) {
    this.cfg = cfg;
  }

  private File download() throws IOException {
    final File wikiDumpBz = cfg.getRepoFile(cfg.lang+"-wikipedia.xml.bz2");
    url = new URL(String.format("http://dumps.wikimedia.org/%swiki/latest/%swiki-latest-pages-articles.xml.bz2", cfg.lang, cfg.lang));
    if (cfg.offline) {
      log.info("Offline mode, use existing dump file {}", wikiDumpBz.getAbsolutePath());
    } else {
      log.info("Downloading latest wikipedia dump from " + url.toString());
      HttpUtil http = new HttpUtil(HttpUtil.newMultithreadedClient(61000,10,10));
      boolean success = http.downloadIfChanged(url, wikiDumpBz);
      if (success){
        log.info("Downloaded new wikipedia dump");
      } else{
        log.info("No newer wikipedia dump, use existing copy");
      }
    }
    modifiedDate = new Date(wikiDumpBz.lastModified());
    return wikiDumpBz;
  }

  public void parse() throws IOException{
    // download file?
    File wikiDumpBz = download();

    // new writer
    File dwcaDir = org.gbif.utils.file.FileUtils.createTempDir("wikipedia-", "-dwca");
    log.info("Writing archive files to temporary folder "+dwcaDir);
    writer = new DwcaWriter(DwcTerm.Taxon, dwcaDir);

    // parse file
    log.info("Parsing dump file {}", wikiDumpBz.getAbsolutePath());
    TaxonboxHandler handler = new TaxonboxHandler(cfg, writer, new File(cfg.repo, "missing_licenses-"+cfg.lang+".txt"));
    try {
      WikiXMLParser wxp = new WikiXMLParser(wikiDumpBz.getAbsolutePath(), handler);
      wxp.parse();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      log.info("Unknown Taxoninfo properties: {}", handler.getWikiModel().getUnknownProperties());
      for (Map.Entry<String, Integer> tmpl : handler.getWikiModel().getUnknownTemplatesCounter().entrySet()) {
        log.debug("Unknown template >>{}<< {}x {}", tmpl.getKey(), tmpl.getValue(), handler.getWikiModel().getUnknownTemplates().get(tmpl.getKey()));
      }
    }

    // finish archive and zip it
    final File dwcaFile = cfg.getDwcaFile();
    log.info("Bundling archive at {}", dwcaFile);
    writer.setEml(buildEml());
    writer.close(); // adds eml and meta.xml file

    if (dwcaFile.exists()) {
      log.info("Delete existing archive {}", dwcaFile);
      dwcaFile.delete();
    } else {
      FileUtils.forceMkdir(dwcaFile.getParentFile());
    }
    log.info("Bundling archive at {}", dwcaFile);
    CompressionUtil.zipDir(dwcaDir, dwcaFile);

    // remove temp folder
    if (!cfg.keepTmp) {
      log.info("Remove temp working dir {}", dwcaDir);
      FileUtils.deleteDirectory(dwcaDir);
    }
    log.info("Wikipedia dwc archive completed at {} !", dwcaFile);
  }

  private Dataset buildEml() throws IOException {
    Dataset dataset = new Dataset();
    dataset.setTitle(cfg.getLanguage().getTitleEnglish() + " Wikipedia - Species Pages");
    dataset.setLanguage(Language.ENGLISH);
    dataset.setDataLanguage(cfg.getLanguage());
    String description = Resources.toString(Resources.getResource("description.txt"), Charsets.UTF_8);
    dataset.setDescription(description.replaceAll("$LANGUAGE", cfg.getLanguage().getTitleEnglish())
      .replaceAll("$DATE", DateFormatUtils.ISO_DATE_FORMAT.format(modifiedDate)));
    dataset.setPubDate(modifiedDate);
    dataset.setHomepage(URI.create("http://" + cfg.lang + ".wikipedia.org"));
    addDeveloper(dataset, ContactType.METADATA_AUTHOR, ContactType.ORIGINATOR,
      ContactType.ADMINISTRATIVE_POINT_OF_CONTACT);

    DataDescription d = new DataDescription();
    d.setUrl(URI.create(url.toString()));
    d.setFormat("XML");
    d.setName("Wikipedia Article Dump");
    dataset.getDataDescriptions().add(d);

    return dataset;
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

  public static void main (String[] args) throws IOException {
    WikipediaConfig cfg = new WikipediaConfig();
    new JCommander(cfg,args);

    ChecklistBuilder cmd = new ChecklistBuilder(cfg);
    cmd.parse();
  }
}
