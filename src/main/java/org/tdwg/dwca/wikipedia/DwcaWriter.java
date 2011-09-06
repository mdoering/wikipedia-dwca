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
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.ArchiveWriter;
import org.gbif.file.CSVReader;
import org.gbif.file.TabWriter;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlWriter;
import org.gbif.utils.file.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DwcaWriter {
  private Logger log = LoggerFactory.getLogger(getClass());
  private File dir;
  private int recordNum;
  private String coreId;
  private Map<ConceptTerm, String> coreRow;
  private final ConceptTerm coreRowType;
  private Map<ConceptTerm, TabWriter> writers = new HashMap<ConceptTerm, TabWriter>();
  private Map<ConceptTerm, String> dataFileNames = new HashMap<ConceptTerm, String>();
  // key=rowType, value=columns
  private Map<ConceptTerm, List<ConceptTerm>> terms = new HashMap<ConceptTerm, List<ConceptTerm>>();
  private Eml eml;


  private DwcaWriter(File dir, ConceptTerm coreRowType) throws IOException {
    this.dir = dir;
    this.coreRowType = coreRowType;
  }

  public DwcaWriter(ConceptTerm coreRowType, File dir) throws IOException {
    this.dir = dir;
    this.coreRowType= coreRowType;
    addRowType(coreRowType);
  }

  private void addRowType(ConceptTerm rowType) throws IOException {
    terms.put(rowType, new ArrayList<ConceptTerm>());

    String dfn = rowType.simpleNormalisedName().toLowerCase()+".txt";
    dataFileNames.put(rowType, dfn);
    File df = new File(dir, dfn+"-1st_pass");
    org.apache.commons.io.FileUtils.forceMkdir(df.getParentFile());
    OutputStream out = new FileOutputStream(df);
    TabWriter wr = new TabWriter(out);
    writers.put(rowType, wr);
  }

  public void newRecord(String id) throws IOException {
    // flush last record
    flushLastCoreRecord();
    // start new
    recordNum++;
    coreId=id;
    coreRow=new HashMap<ConceptTerm, String>();
  }

  private void flushLastCoreRecord() throws IOException {
    if (coreRow != null && coreId != null) {
      writeRow(coreRow, coreRowType);
    }
  }
  private void writeRow(Map<ConceptTerm, String> rowMap, ConceptTerm rowType) throws IOException {
      TabWriter writer = writers.get(rowType);
      List<ConceptTerm> coreTerms = terms.get(rowType);
    String[] row = new String[coreTerms.size()+1];
    row[0] = coreId;
    for (ConceptTerm term : rowMap.keySet()) {
      int column = 1 + coreTerms.indexOf(term);
      row[column] = rowMap.get(term);
    }
    writer.write(row);
  }
  public void addCoreColumn(ConceptTerm term, String value){
    List<ConceptTerm> coreTerms = terms.get(coreRowType);
    if (!coreTerms.contains(term)){
      coreTerms.add(term);
    }
    coreRow.put(term, value);
  }

  public void addExtensionRecord(ConceptTerm rowType, Map<ConceptTerm, String> row) throws IOException {
    // make sure we know the extension rowtype
    if (!terms.containsKey(rowType)){
      addRowType(rowType);
    }
    // make sure we know all terms
    List<ConceptTerm> knownTerms = terms.get(rowType);
    for (ConceptTerm term : row.keySet()){
      if (!knownTerms.contains(term)) {
        knownTerms.add(term);
      }
    }
    // write extension record
    writeRow(row, rowType);
  }

  public void setEml(Eml eml) {
    this.eml = eml;
  }

  /**
   * writes meta.xml and eml.xml to the archive
   * and closes tab writers
   */
  public void finalize() throws IOException{
    addEml();
    addMeta();
    // flush last record
    flushLastCoreRecord();
    // close writers
    for (TabWriter w : writers.values()){
      w.close();
    }
    // add missing columns in second iteration
    addMissingColumns();
  }

  public static void main (String[] args) throws IOException {
    DwcaWriter dw = new DwcaWriter(new File("/Users/markus/Desktop/wikipedia-es-dwca"), DwcTerm.Taxon);

    Map<ConceptTerm, Integer> exts = new HashMap<ConceptTerm, Integer>();
    exts.put(DwcTerm.Taxon, 18);
    exts.put(GbifTerm.Description, 3);
    exts.put(GbifTerm.Distribution, 3);
    exts.put(GbifTerm.Image, 3);
    exts.put(GbifTerm.SpeciesProfile, 1);
    exts.put(GbifTerm.VernacularName, 3);

    for (ConceptTerm rt : exts.keySet()){
      String dfn = rt.simpleNormalisedName().toLowerCase() + ".txt";
      dw.terms.put(rt, new ArrayList<ConceptTerm>());
      dw.dataFileNames.put(rt, dfn);
      while (dw.terms.get(rt).size() < exts.get(rt)) {
        dw.terms.get(rt).add(DwcTerm.Taxon);
      }
    }

    dw.addMissingColumns();
  }

  private void addMissingColumns() {
    for (ConceptTerm rowType : terms.keySet()) {
      int columns = terms.get(rowType).size()+1; // +1 for the coreID
      String dfn = this.dataFileNames.get(rowType);
      log.debug("Adding missing columns {} for data file {}", columns, dfn);
      File f1stPass = new File(dir, dfn + "-1st_pass");
      File fFinal = new File(dir, dfn);
      BufferedReader reader= null;
      FileWriter out=null;
      try {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(f1stPass), "UTF-8"));
        out = new FileWriter(fFinal);

        String line = reader.readLine();
        while (line!=null){
          // If we haven't reached EOF yet
          int missingCols = columns - StringUtils.countMatches(line,"\t") - 1;
          if (missingCols>0){
            line += StringUtils.repeat('\t', missingCols);
          }
          out.write(line + "\n");
          // get new line
          line = reader.readLine();
        }
      } catch (IOException e) {
        log.error("IOException", e);
      } finally {
        // close reader/writer
        try {
          if (out!=null) out.close();
          if (reader!= null) reader.close();
        } catch (IOException e) {
          log.error("IOException", e);
        }
      }
      // remove first pass file
      f1stPass.delete();
    }
  }
  private void addEml() throws IOException{
    if (eml!=null){
      File emlFile = new File(dir, "eml.xml");
      try {
        EmlWriter.writeEmlFile(emlFile, eml);
      } catch (TemplateException e) {
        throw new IOException("EML template exception: "+e.getMessage(), e);
      }
    }
  }

  private void addMeta() throws IOException {
    File metaFile = new File(dir, "meta.xml");

    Archive arch = new Archive();
    if (eml!=null){
      arch.setMetadataLocation("eml.xml");
    }
    arch.setCore(buildArchiveFile(arch, DwcTerm.Taxon));
    for (ConceptTerm rowType : this.terms.keySet()){
      if (!DwcTerm.Taxon.equals(rowType)){
        arch.addExtension(buildArchiveFile(arch,rowType));
      }
    }
    try {
      ArchiveWriter.writeMetaFile(metaFile, arch);
    } catch (TemplateException e) {
      throw new IOException("Meta.xml template exception: " + e.getMessage(), e);
    }
  }

  private ArchiveFile buildArchiveFile(Archive archive, ConceptTerm rowType){
    ArchiveFile af = ArchiveFile.buildTabFile();
    af.setArchive(archive);
    af.addLocation(dataFileNames.get(rowType));

    af.setEncoding("utf-8");
    af.setIgnoreHeaderLines(0);
    af.setRowType(rowType.qualifiedName());

    ArchiveField id = new ArchiveField();
    id.setIndex(0);
    af.setId(id);

    int idx = 0;
    for (ConceptTerm c : this.terms.get(rowType)){
      idx++;
      ArchiveField field = new ArchiveField();
      field.setIndex(idx);
      field.setTerm(c);
      af.addField(field);
    }

    return af;
  }
}
