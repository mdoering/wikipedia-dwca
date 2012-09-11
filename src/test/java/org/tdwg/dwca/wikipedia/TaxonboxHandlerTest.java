package org.tdwg.dwca.wikipedia;

import org.gbif.api.model.vocabulary.Language;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.DwcaWriter;
import org.gbif.utils.file.FileUtils;
import org.gbif.utils.file.InputStreamUtils;

import java.io.File;
import java.io.IOException;

import info.bliki.wiki.dump.WikiArticle;
import org.junit.Test;
import org.tdwg.dwca.wikipedia.taxonbox.Rank;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonInfo;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TaxonboxHandlerTest {
  private WikiArticle page = new WikiArticle();

  private TaxonInfo processPage(String title, String filename, Language lang) throws IOException, SAXException {
    InputStreamUtils isu = new InputStreamUtils();
    page.setId("1");
    page.setTitle(title);
    page.setText(isu.readEntireStream(FileUtils.classpathStream(filename)));

    File tmpDir = FileUtils.createTempDir();
    tmpDir.deleteOnExit();
    DwcaWriter writer = new DwcaWriter(DwcTerm.Taxon, tmpDir);
    TaxonboxHandler th = new TaxonboxHandler(lang.getIso2LetterCode(), writer);
    th.process(page, null);
    return th.getWikiModel().getTaxonInfo();
  }

  @Test
  public void testPuma() throws Exception {
    TaxonInfo taxon = processPage("Puma", "puma-en.txt", Language.ENGLISH);

    assertEquals("Puma", taxon.getScientificName());
    assertNull(taxon.getScientificNameAuthorship());
    assertEquals("Animalia", taxon.getKingdom());
    assertEquals("Chordata", taxon.getPhylum());
    assertEquals("Mammalia", taxon.getClazz());
    assertEquals("Carnivora", taxon.getOrder());
    assertEquals("Felidae", taxon.getFamily());
    assertEquals("Puma", taxon.getGenus());
    assertEquals(Rank.Genus, taxon.getRank());
    assertTrue(taxon.getVernacularNamesInDefaultLang().contains("Cougar"));
    assertEquals(68, taxon.getVernacularNames().size());
    assertEquals("Puma", taxon.getVernacularNames().get("de"));
    assertEquals("Poema", taxon.getVernacularNames().get("nl"));
    assertEquals("Middle Pleistocene to Recent (Pleistocene to Recent)", taxon.getFossilRange());
    assertTrue(taxon.getExtinctSymbol());
  }

  @Test
  public void testPumaConcolor() throws Exception {
    TaxonInfo taxon = processPage("Cougar", "pumaconcolor-en.txt", Language.ENGLISH);

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
    assertEquals("Middle Pleistocene to Recent (Pleistocene to Recent)", taxon.getFossilRange());
    assertNull(taxon.getExtinct());
    assertFalse(taxon.getExtinctSymbol());
  }

  @Test
  public void testOlivenbaum() throws Exception {
    TaxonInfo taxon = processPage("Olivenbaum", "olivenbaum.txt", Language.GERMAN);

    assertEquals("Olea europaea", taxon.getScientificName());
    assertEquals("L.", taxon.getScientificNameAuthorship());
    assertNull(taxon.getKingdom());
    assertNull(taxon.getPhylum());
    assertNull(taxon.getClazz());
    assertEquals("Lamiales", taxon.getOrder());
    assertEquals("Oleaceae", taxon.getFamily());
    assertEquals("Olea", taxon.getGenus());
    assertEquals(Rank.Species, taxon.getRank());
    assertTrue(taxon.getVernacularNamesInDefaultLang().contains("Echter Olivenbaum"));
    assertEquals(80, taxon.getVernacularNames().size());
    assertEquals("Olive", taxon.getVernacularNames().get("en"));
    assertEquals("Olijf", taxon.getVernacularNames().get("nl"));
    assertNull(taxon.getFossilRange());
  }

  @Test
  public void testArthrobacter() throws Exception {
    TaxonInfo taxon = processPage("Arthrobacter", "arthrobacter.txt", Language.GERMAN);

    assertEquals("Arthrobacter", taxon.getScientificName());
    assertEquals("Conn & Dimmick 1947", taxon.getScientificNameAuthorship());
    assertNull(taxon.getKingdom());
    // Abteilung maps to Divisio above phylum
    assertNull(taxon.getPhylum());
    assertEquals("Actinobacteria", taxon.getClazz());
    assertEquals("Actinomycetales", taxon.getOrder());
    assertEquals("Micrococcaceae", taxon.getFamily());
    assertEquals("Arthrobacter", taxon.getGenus());
    assertEquals(Rank.Genus, taxon.getRank());
    assertTrue(taxon.getVernacularNamesInDefaultLang().isEmpty());
    assertEquals(5, taxon.getVernacularNames().size());
    assertEquals("Arthrobacter", taxon.getVernacularNames().get("en"));
    assertEquals("Arthrobacter", taxon.getVernacularNames().get("fr"));
    assertNull(taxon.getFossilRange());
  }

  @Test
  public void testApatosaurus() throws Exception {
    TaxonInfo taxon = processPage("Apatosaurus", "apatosaurus.txt", Language.ENGLISH);

    assertEquals("Apatosaurus", taxon.getScientificName());
    assertEquals("Marsh, 1877", taxon.getScientificNameAuthorship());
    assertNull(taxon.getKingdom());
    assertNull(taxon.getPhylum());
    assertNull(taxon.getRank());
    assertTrue(taxon.getVernacularNamesInDefaultLang().isEmpty());
    assertEquals(40, taxon.getVernacularNames().size());
    assertEquals("Apatosaurus", taxon.getVernacularNames().get("de"));
    assertEquals("Apatosaurus", taxon.getVernacularNames().get("es"));
    assertEquals("Late Jurassic, 154-150 Ma", taxon.getFossilRange());
  }

}
