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
import org.tdwg.dwca.wikipedia.taxonbox.Rank;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    TaxonInfo taxon = ph.getWikiModel().getTaxonInfo();
    assertEquals("Puma concolor", taxon.getScientificName());
    assertEquals("(Linnaeus, 1771)", taxon.getScientificNameAuthorship());
    assertEquals("Animalia", taxon.getKingdom());
    assertEquals("Chordata", taxon.getPhylum());
    assertEquals("Mammalia", taxon.getClazz());
    assertEquals("Carnivora", taxon.getOrder());
    assertEquals("Felidae", taxon.getFamily());
    assertEquals("Puma", taxon.getGenus());
    assertEquals(Rank.Species, taxon.getRank());
    assertTrue(taxon.getVernacularNamesInDefaultLang().contains("Cougar"));
    assertEquals(68, taxon.getVernacularNames().size());
    assertEquals("Puma", taxon.getVernacularNames().get("de"));
    assertEquals("Poema", taxon.getVernacularNames().get("nl"));
    assertEquals("Middle Pleistocene to Recent", taxon.getFossilRange());
  }
}
