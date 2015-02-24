package org.tdwg.dwca.wikipedia;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class ChecklistBuilderTest {

  @Test
  public void testMain() throws Exception {
    // writes to /tmp/wikipedia
    WikipediaConfig cfg = new WikipediaConfig();
    cfg.lang = "en";
    cfg.footnotes = true;
    cfg.offline = true;

    // copy test resource to tmp
    InputStream in = Resources.getResource("enwiki-latest-pages-articles1.xml.bz2").openStream();
    File tmp = cfg.getRepoFile("en-wikipedia.xml.bz2");
    FileUtils.forceMkdir(tmp.getParentFile());
    if (!tmp.exists()) {
      tmp.createNewFile();
    }
    OutputStream out = FileUtils.openOutputStream(tmp);
    IOUtils.copyLarge(in, out);
    in.close();
    out.close();

    ChecklistBuilder cb = new ChecklistBuilder(cfg);
    cb.parse();
  }
}