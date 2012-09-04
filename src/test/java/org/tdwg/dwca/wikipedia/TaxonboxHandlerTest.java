package org.tdwg.dwca.wikipedia;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.DwcaWriter;
import org.gbif.utils.file.FileUtils;
import org.gbif.utils.file.InputStreamUtils;

import java.io.File;
import java.io.IOException;

import info.bliki.wiki.dump.WikiArticle;
import org.junit.Before;
import org.junit.Test;

public class TaxonboxHandlerTest {
  private WikiArticle page = new WikiArticle();

  @Before
  public void initPage() throws IOException {
    InputStreamUtils isu = new InputStreamUtils();
    page.setId("1");
    page.setTitle("Cougar");
    page.setText(isu.readEntireStream(FileUtils.classpathStream("puma-en.txt")));
  }

  @Test
  public void testHandle() throws Exception {
    File tmpDir = FileUtils.createTempDir();
    tmpDir.deleteOnExit();
    DwcaWriter writer = new DwcaWriter(DwcTerm.Taxon, tmpDir);
    TaxonboxHandler ph = new TaxonboxHandler("en", writer);
    ph.process(page, null);
  }
}
