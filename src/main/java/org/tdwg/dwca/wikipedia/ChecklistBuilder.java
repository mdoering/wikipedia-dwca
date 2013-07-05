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

import org.gbif.api.vocabulary.ContactType;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.DwcaWriter;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.CompressionUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import info.bliki.wiki.dump.WikiXMLParser;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChecklistBuilder {
  private Logger log = LoggerFactory.getLogger(ChecklistBuilder.class);
  private DwcaWriter writer;
  private static String DUMP_NAME = "wiki-latest-pages-articles.xml.bz2";

  @Parameter(names = {"-repo"}, description = "Directory to download wikipedia dumps to. If last versions are found a conditional get download will be done. Defaults to /tmp/wikipedia-data")
  public File repo = new File("/tmp/wikipedia-data");

  @Parameter(names = {"-dwca"}, description = "Dwc archive file to be created. Defaults to wikipedia-LANG-dwca.zip in the repository")
  public File dwcaFile;

  @Parameter(names = {"-lang"}, description = "Wikipedia language file to parse (en, es, fr, de, etc.). Defaults to english")
  public String lang= "en";

  @Parameter(names = {"-offline"}, description = "If true no wikipedia dumps will be downloaded and only local dump files will be used. Defaults to false")
  public boolean offline = false;

  @Parameter(names = {"-keepTmp"}, description = "If true temporay files during archive build will be kept. Defaults to false")
  public boolean keepTmp = false;

  @Parameter(names = "--help", help = true)
  private boolean help;

  @Inject
  public ChecklistBuilder(){
  }

  private File download(String dumpName) throws IOException {
    File wikiDumpBz = new File(repo, dumpName);

    if (!offline){
      final URL url = new URL(String.format("http://dumps.wikimedia.org/%swiki/latest/%s", lang, dumpName));

      log.info("Downloading latest wikipedia dump from " + url.toString());
      HttpUtil http = new HttpUtil();
      boolean success = http.downloadIfChanged(url, wikiDumpBz);
      if (success){
        log.info("Downloaded new wikipedia dump");
      } else{
        log.info("No newer wikipedia dump, use existing copy");
      }
    }

    return wikiDumpBz;
  }

  public void parse() throws IOException{
    // download file?
    final String dumpName = lang + DUMP_NAME;

    File wikiDumpBz = download(dumpName);

    // new writer
    File dwcaDir = org.gbif.utils.file.FileUtils.createTempDir("wikipedia-", "-dwca");
    log.info("Writing archive files to temporary folder "+dwcaDir);
    writer = new DwcaWriter(DwcTerm.Taxon, dwcaDir);

    // parse file
    log.info("Parsing dump file {}", wikiDumpBz.getAbsolutePath());
    TaxonboxHandler handler = new TaxonboxHandler(lang, writer);
    try {
      WikiXMLParser wxp = new WikiXMLParser(wikiDumpBz.getAbsolutePath(), handler);
      wxp.parse();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      log.info("Unknown Taxoninfo properties: {}", handler.getWikiModel().getUnknownProperties());
      for (Map.Entry<String, String> tmpl : handler.getWikiModel().getUnknownTemplates().entrySet()) {
        log.debug("Unknown template >>{}<< {}", tmpl.getKey(), tmpl.getValue());
      }
    }

    // finish archive and zip it
    if (dwcaFile == null) {
      dwcaFile = new File(repo, "wikipedia-" + lang + "-dwca.zip");
    }
    log.info("Bundling archive at {}", dwcaFile);
    writer.setEml(buildEml());
    writer.finalize();

    if (dwcaFile.exists()) {
      log.debug("Delete existing archive {}", dwcaFile);
      dwcaFile.delete();
    } else {
      FileUtils.forceMkdir(dwcaFile.getParentFile());
    }
    log.info("Bundling archive at {}", dwcaFile);
    CompressionUtil.zipDir(dwcaDir, dwcaFile);

    // remove temp folder
    if (!keepTmp) {
      FileUtils.deleteDirectory(dwcaDir);
    }

    log.info("Wikipedia dwc archive completed at {} !", dwcaFile);
  };


  private Eml buildEml(){
    Eml eml = new Eml();
    eml.setTitle("Wikipedia-" + lang + " Species Pages", "en");
    eml.setAbstract("Parsed taxobox pages of the wikipedia dump");
    eml.setHomepageUrl("http://" + lang + ".wikipedia.org");
    eml.setContact(getMarkus(ContactType.METADATA_AUTHOR));
    eml.setResourceCreator(getMarkus(ContactType.CONTENT_PROVIDER));
    eml.setLanguage(lang);
    return eml;
  }

  private Agent getMarkus(ContactType role){
    Agent markus = new Agent();
    markus.setEmail("mdoering@gbif.org");
    markus.setFirstName("Markus");
    markus.setLastName("Döring");
    markus.setRole(role.name());
    return markus;
  }
  public static void main (String[] args) throws IOException {
    Injector injector = Guice.createInjector(new GuiceConfig());
    ChecklistBuilder cmd = injector.getInstance(ChecklistBuilder.class);
    new JCommander(cmd,args);
    cmd.parse();
  }
}
