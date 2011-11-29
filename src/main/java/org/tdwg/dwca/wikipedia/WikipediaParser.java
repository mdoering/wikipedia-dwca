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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class WikipediaParser {
  private Logger log = LoggerFactory.getLogger(WikipediaParser.class);
  private DwcaWriter writer;

  @Parameter(names = {"-dump"}, description = "Local wikipedia xml dump file to parse")
  public File dumpFile=new File("/Users/mdoering/Desktop/wikipedia-data/enwiki.xml");

  @Parameter(names = {"-repo"}, description = "Directory to download wikipedia dumps to. If last versions are found a conditional get download will be done. Defaults to /tmp/wikipedia")
  public File repo = new File("/tmp/wikipedia");

  @Parameter(names = {"-dwca"}, description = "Dwc archive file to be created. Defaults to a tmp file")
  public File dwcaFile;

  @Parameter(names = {"-lang"}, description = "Wikipedia language file to parse (en, es, fr, de, etc.). Defaults to english if no language or dump file is given")
  public String lang= "en";

  @Inject
  public WikipediaParser(){
  }

  public void parse() throws IOException{
    if (dumpFile == null) {
      dumpFile = new File(repo, "wiki" + lang + ".xml");
    }

    // get file
    if (!dumpFile.exists()){
      // http://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2
      // http://dumps.wikimedia.org/dewiki/latest/dewiki-latest-pages-articles.xml.bz2
      URL url = new URL(String.format("http://dumps.wikimedia.org/%swiki/latest/%swiki-latest-pages-articles.xml.bz2",lang,lang));
      File wikiDumpBz = new File(repo,"wiki"+lang+".bz2");

      log.info("Downloading latest wikipedia dump from " + url.toString());
      HttpUtil http = new HttpUtil();
      boolean success = http.downloadIfChanged(url, wikiDumpBz);
      if (success){
        log.info("Downloaded new wikipedia dump");
      } else{
        log.info("No newer wikipedia dump, use existing copy");
      }
      // decompress
      decompressDump(wikiDumpBz, dumpFile);
    }

    // new writer
    File dwcaDir = FileUtils.createTempDir("wikipedia-", "-dwca");
    log.info("Writing archive files to temporary folder "+dwcaDir);
    writer = new DwcaWriter(DwcTerm.Taxon, dwcaDir);

    // parse file
    try {
      // create XMLReader
      log.info("Parsing dump file {}", dumpFile.getAbsolutePath());
      FileReader reader = new FileReader(dumpFile);
      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      InputSource inputSource = new InputSource(reader);

      // sax parse with wikipedia handler
      xmlReader.setContentHandler(new WikipediaSaxHandler(writer, lang));

      // Parsen wird gestartet
      xmlReader.parse(inputSource);
    } catch (SAXException e) {
      e.printStackTrace();
    }

    // add missing columns in second iteration of data files
    writer.finalize();
    // finish archive and zip it
    if (dwcaFile == null) {
      dwcaFile = new File(repo, "wikipedia-" + lang + "-dwca.zip");
    }
    log.info("Bundling archive at {}", dwcaFile);
    writer.setEml(buildEml());
    writer.finalize();

    if (dwcaFile.exists()) {
      org.apache.commons.io.FileUtils.deleteQuietly(dwcaFile);
    } else {
      org.apache.commons.io.FileUtils.forceMkdir(dwcaFile.getParentFile());
    }
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
  private void decompressDump(File fin, File fout) throws IOException {
    log.info("Decompressing dump {} to {}", fin.getAbsolutePath(), fout.getAbsolutePath());

    FileInputStream in = new FileInputStream(fin);
    FileOutputStream out = new FileOutputStream(fout);
    BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
    final byte[] buffer = new byte[1024];
    int n = 0;
    while (-1 != (n = bzIn.read(buffer))) {
      out.write(buffer, 0, n);
    }
    out.close();
    bzIn.close();
  }

  public static void main (String[] args) throws IOException {
    Injector injector = Guice.createInjector(new GuiceConfig());
    WikipediaParser cmd = injector.getInstance(WikipediaParser.class);
    new JCommander(cmd,args);
    cmd.parse();
  }
}
