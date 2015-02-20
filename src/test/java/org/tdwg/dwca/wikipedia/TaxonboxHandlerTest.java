package org.tdwg.dwca.wikipedia;

import org.gbif.api.vocabulary.Language;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.DwcaWriter;
import org.gbif.utils.file.FileUtils;
import org.gbif.utils.file.InputStreamUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import info.bliki.wiki.dump.WikiArticle;
import org.junit.Ignore;
import org.junit.Test;
import org.tdwg.dwca.wikipedia.taxonbox.Rank;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonInfo;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TaxonboxHandlerTest {

  private TaxonInfo processPage(String title, String filename, Language lang) throws IOException, SAXException {
    TaxonboxHandler th = getHandler(lang);
    th.process(article(title, filename), null);
    return th.getWikiModel().getTaxonInfo();
  }

  private WikiArticle article(String title, String filename) throws IOException {
    InputStreamUtils isu = new InputStreamUtils();
    WikiArticle page = new WikiArticle();
    page.setId("1");
    page.setTitle(title);
    page.setText(isu.readEntireStream(FileUtils.classpathStream(filename)));
    return page;
  }

  private TaxonboxHandler getHandler(Language lang) throws IOException, SAXException {
    File tmpDir = FileUtils.createTempDir();
    tmpDir.deleteOnExit();
    DwcaWriter writer = new DwcaWriter(DwcTerm.Taxon, tmpDir);
    WikipediaConfig cfg = new WikipediaConfig();
    cfg.lang = lang.getIso2LetterCode();
    return new TaxonboxHandler(cfg, writer, null);
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


  @Test
  public void testBinomial2() throws Exception {
    TaxonInfo taxon = processPage("Anoa", "binomial2.txt", Language.ENGLISH);

    assertEquals("Bubalus quarlesi", taxon.getScientificName());
    assertEquals("(Ouwens, 1910)", taxon.getScientificNameAuthorship());
    assertEquals("Animalia", taxon.getKingdom());
    assertEquals("Chordata", taxon.getPhylum());
    assertEquals("Mammalia", taxon.getClazz());
    assertEquals("Artiodactyla", taxon.getOrder());
    assertEquals("Bovidae", taxon.getFamily());
    assertEquals("Bubalus", taxon.getGenus());
    assertEquals(Rank.Species, taxon.getRank());
    assertEquals("Anoa", taxon.getVernacularNamesInDefaultLang().iterator().next());
    assertEquals(29, taxon.getVernacularNames().size());
    assertEquals("Flachland-Anoa", taxon.getVernacularNames().get("de"));
    assertEquals("Аноа", taxon.getVernacularNames().get("av"));
    assertNull(taxon.getFossilRange());
  }

  @Test
  public void testFabaceae() throws Exception {
    TaxonInfo taxon = processPage("Fabaceae", "fabaceae.txt", Language.ENGLISH);

    assertEquals("Fabaceae", taxon.getScientificName());
    //TODO: fix assertions, type_genus
  }

  /**
   * http://en.wikipedia.org/wiki/Red_wolf
   */
  @Test
  public void testWolf() throws Exception {
    TaxonInfo taxon = processPage("Red_wolf", "red_wolf.txt", Language.ENGLISH);

    assertEquals("Canis lupus rufus", taxon.getScientificName());
    assertEquals("Audubon & Bachman, 1851", taxon.getScientificNameAuthorship());
    assertEquals("Animalia", taxon.getKingdom());
    assertEquals("Chordata", taxon.getPhylum());
    assertEquals("Mammalia", taxon.getClazz());
    assertEquals("Carnivora", taxon.getOrder());
    assertEquals("Canidae", taxon.getFamily());
    assertEquals("Canis", taxon.getGenus());
    assertEquals(Rank.Subspecies, taxon.getRank());
  }

  /**
   * http://en.wikipedia.org/wiki/Sumac
   */
  @Test
  public void testRhus() throws Exception {
    TaxonInfo taxon = processPage("Sumac", "rhus.txt", Language.ENGLISH);
  }

  /**
   * http://en.wikipedia.org/wiki/Pansy
   */
  @Test
  public void testPansy() throws Exception {
    TaxonInfo taxon = processPage("Pansy", "pansy.txt", Language.ENGLISH);
  }

  /**
   * http://en.wikipedia.org/wiki/Mirror_carp
   */
  @Test
  public void testCyprinus() throws Exception {
    TaxonInfo taxon = processPage("Mirror_carp", "cyprinus.txt", Language.ENGLISH);
  }

  /**
   * missing values for height and trunk diameter
   * https://github.com/mdoering/wikipedia-dwca/issues/11
   */
  @Test
  public void testSplitPage() throws Exception {
    TaxonboxHandler h = getHandler(Language.ENGLISH);
    WikiArticle page = article("Agathis microstachya", "agathis.txt");
    LinkedHashMap<String, String> sections = h.splitPage(page);
    assertEquals(5, sections.size());
    assertEquals("A. microstachya grows up to about 50 m in height and 2.7 m in diameter. The trunk is unbuttressed, straight and with little taper. Distinctive features are coarse, flaky bark, medium-sized cones with 160-210 scales, and leaves with numerous longitudinal, parallel veins.", sections.get("Description"));
  }

  @Test
  public void testImageGalleryTag() throws Exception {
    TaxonInfo taxon = processPage("Scaevola taccada", "scaevola_taccada.txt", Language.ENGLISH);
    assertEquals(11, taxon.getImages().size());
  }

  /**
   * https://github.com/mdoering/wikipedia-dwca/issues/10
   * http://en.wikipedia.org/wiki/Scaevola_taccada#Use
   */
  @Test
  @Ignore("Fix is incomplete")
  public void testFooterCitations() throws Exception {
    TaxonboxHandler h = getHandler(Language.ENGLISH);
    WikiArticle page = article("Scaevola taccada", "scaevola_taccada.txt");
    LinkedHashMap<String, String> sections = h.splitPage(page);
    assertEquals(6, sections.size());
    assertEquals("In some islands of the Pacific, Scaevola taccada is used to prevent coastal erosion as well as for landscaping. It is also planted on the beach crests to protect other cultivated plants from the salt spray. Parts of the plant are also used in Polynesian and Asian traditional medicine. It has also been proven to be \"an excellent remedy as antidiabetic, antipyretic, antiinflamatory, anticoagulant and as skeletal muscle relaxant without any adverse reactions\" by the department of pharmacy of Annamalai University, India - Amelia Earhart May Have Survived Months as CastawayHistorically tn the Maldives the leaves of this bush were often used as famine food.Eating on the Islands - As times have changed, so has the Maldives' unique cuisine and culture", sections.get("Use"));
  }


}
