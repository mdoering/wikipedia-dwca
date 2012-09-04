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

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.DwcaWriter;
import org.gbif.metadata.eml.Eml;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.FileUtils;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChecklistBuilder {
  private Logger log = LoggerFactory.getLogger(ChecklistBuilder.class);
  private DwcaWriter writer;
  private static String DUMP_NAME = "wiki-latest-pages-articles.xml.bz2";

  @Parameter(names = {"-repo"}, description = "Directory to download wikipedia dumps to. If last versions are found a conditional get download will be done. Defaults to /tmp/wikipedia")
  public File repo = new File("/Users/mdoering/Desktop/wikipedia-data");

  @Parameter(names = {"-dwca"}, description = "Dwc archive file to be created. Defaults to a tmp file")
  public File dwcaFile;

  @Parameter(names = {"-lang"}, description = "Wikipedia language file to parse (en, es, fr, de, etc.). Defaults to english if no language or dump file is given")
  public String lang= "en";

  @Inject
  public ChecklistBuilder(){
  }

  public void parse() throws IOException{
    // download file?
    final String dumpName = lang + DUMP_NAME;
    File wikiDumpBz = new File(repo, dumpName);

    // download?
    //TODO: remove this, we use conditional downloads in production to check if there is a newer file online
    if (!wikiDumpBz.exists()){
      final URL url = new URL(String.format("http://dumps.wikimedia.org/%swiki/latest/%s",lang,dumpName));

      log.info("Downloading latest wikipedia dump from " + url.toString());
      HttpUtil http = new HttpUtil();
      boolean success = http.downloadIfChanged(url, wikiDumpBz);
      if (success){
        log.info("Downloaded new wikipedia dump");
      } else{
        log.info("No newer wikipedia dump, use existing copy");
      }
    }

    // new writer
    File dwcaDir = FileUtils.createTempDir("wikipedia-", "-dwca");
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
      org.apache.commons.io.FileUtils.deleteQuietly(dwcaFile);
    } else {
      org.apache.commons.io.FileUtils.forceMkdir(dwcaFile.getParentFile());
    }
    log.info("Bundling archive at {}", dwcaFile);
    CompressionUtil.zipDir(dwcaDir, dwcaFile);
    // remove temp folder
    //org.apache.commons.io.FileUtils.deleteDirectory(dwcaDir);

    log.info("Wikipedia dwc archive completed at {} !", dwcaFile);
  };


  private Eml buildEml(){
    Eml eml = new Eml();
    eml.setTitle("Wikipedia-"+lang+" Species Pages", "en");
    eml.setAbstract("Parsed taxobox pages of the wikipedia dump");
    eml.setHomeUrl("http://"+lang+".wikipedia.org");
    return eml;
  }

  public static void main (String[] args) throws IOException {
    Injector injector = Guice.createInjector(new GuiceConfig());
    ChecklistBuilder cmd = injector.getInstance(ChecklistBuilder.class);
    new JCommander(cmd,args);
    cmd.parse();
  }
}
