package org.tdwg.dwca.wikipedia;

import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gbif.api.vocabulary.Language;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ChecklistBuilderTest {

  @Test
  public void testBuild() throws Exception {
    // writes to /tmp/wikipedia
    WikipediaConfig cfg = new WikipediaConfig();
    cfg.lang = Language.ENGLISH;
    cfg.footnotes = true;
    cfg.offline = true;

    // copy test resource to tmp
    File tmp = cfg.getDumpFile();
    if (!tmp.exists()) {
      FileUtils.forceMkdir(tmp.getParentFile());
      tmp.createNewFile();
    }
    try (InputStream in = Resources.getResource("enwiki-20220420-pages-articles11.xml.bz2").openStream();
         OutputStream out = FileUtils.openOutputStream(tmp)
    ) {
      IOUtils.copyLarge(in, out);
    }

    ChecklistBuilder cb = new ChecklistBuilder(cfg);
    cb.run();
  }

  @Test
  @Ignore("manual full build")
  public void fullBuild() throws Exception {
    // writes to /tmp/wikipedia
    WikipediaConfig cfg = new WikipediaConfig();
    cfg.lang = Language.ENGLISH;
    cfg.footnotes = true;
    cfg.offline = false;
    cfg.keepTmp = true;

    ChecklistBuilder cb = new ChecklistBuilder(cfg);
    cb.run();
  }
}