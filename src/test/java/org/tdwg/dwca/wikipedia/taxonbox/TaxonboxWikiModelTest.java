package org.tdwg.dwca.wikipedia.taxonbox;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import org.apache.commons.lang3.StringUtils;
import org.gbif.utils.file.InputStreamUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.tdwg.dwca.wikipedia.WikipediaConfig;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TaxonboxWikiModelTest {
  private static WikipediaConfig cfg = new WikipediaConfig();
  private WikiModel wiki = new TaxonboxWikiModel(cfg);
  private PlainTextConverter converter = new PlainTextConverter();
  private InputStreamUtils isu = new InputStreamUtils();

  private String render(String wikiText) {
    try {
      return StringUtils.normalizeSpace(wiki.render(converter, wikiText));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  public void testCounterOrdering() throws Exception {
    TaxonboxWikiModel wm = new TaxonboxWikiModel(cfg);
    wm.unknownTemplatesCounter.put("converter", 29);
    wm.unknownTemplatesCounter.put("converter-min", 1);
    wm.unknownTemplatesCounter.put("converter-max", 45);
    wm.unknownTemplatesCounter.put("converter2", 12);
    wm.unknownTemplatesCounter.put("converter3", 1);

    assertEquals(5, wm.getUnknownTemplatesCounter().size());
    int last = 100;
    for (Map.Entry<String, Integer> entry : wm.getUnknownTemplatesCounter().entrySet()) {
      assertTrue(entry.getValue() <= last);
      last = entry.getValue();
    }
  }

  @Test
  public void testRender() throws Exception {
    WikiModel wiki = new TaxonboxWikiModel(cfg);
    PlainTextConverter converter = new PlainTextConverter();
    assertEquals("68-65 Ma", wiki.render(converter, "{{Fossil range|68|65|}}"));
    assertEquals("Hello", wiki.render(converter, "Hello"));
    assertEquals("Permian", wiki.render(converter, "{{Fossil range|'''Permian'''}}"));
  }

  @Test
  public void testHybrid() throws Exception {
    assertEquals("A × B", render("A {{Hybrid}} B"));
    assertEquals("×", render("{{hybrid}}").trim());
    assertEquals("×", render("{{hybrid|hallo}}").trim());
  }

  @Test
  public void testSpeciesList() throws Exception {
    assertEquals("Abies alba Mill. Puma concolor (Linnaeus, 1771) Passer Linnaeus, 1771", render("{{species list|Abies alba| Mill. | Puma concolor | ([[Carolus Linnaeus|Linnaeus]], 1771) | Passer | Linnaeus, 1771}}"));
  }

  @Test
  public void testPlainList() throws Exception {
    assertEquals("cat dog horse cow sheep pig", render(isu.readEntireStream(isu.classpathStream("plainlist1.txt"))));
    assertEquals("cat dog horse cow sheep pig", render(isu.readEntireStream(isu.classpathStream("plainlist2.txt"))));
  }

  @Test
  public void testCollapsibleList() throws Exception {
    assertEquals("Dean Allison Chris Charlton David Christopherson Wayne Marston David Sweet", render(isu.readEntireStream(isu.classpathStream("collapsible1.txt"))));
    assertEquals("Iceland Liechtenstein Norway Switzerland", render(isu.readEntireStream(isu.classpathStream("collapsible2.txt"))));
  }

  @Test
  public void testFossilRange() throws Exception {
    assertEquals("Permian", render("{{Fossil range|Permian}}"));
    assertEquals("68-65 Ma", render("68-65 Ma"));
    assertEquals("68-65 Ma", render("68-65 Ma{{Carla}}"));
    assertEquals("Permian", render("{{Fossil range|'''Permian'''}}"));
    assertEquals("68-65 Ma", render("{{Fossil range|68|65|earliest=Permian|latest=0|PS= (See article for discussion)}}"));
    assertEquals("68-65 Ma", render("{{Fossil range|68|65|}}"));
    assertEquals("Late Cretaceous (68-65.5 Ma)", render("{{Fossil range|68|65.5|Late Cretaceous}}"));
  }

  @Test
  public void testCitation() throws Exception {
    cfg.footnotes=true;
    assertEquals("citation", render("citation"));
    assertEquals("UniProt: Order Rosales", render("{{cite web|url = http://beta.uniprot.org/taxonomy/3744| title = Order '''Rosales'''| accessdate = 2008-04-24| author = UniProt| authorlink = UniProt}}"));
    assertEquals("Döring, Markus (2000): PonTaurus: pp. 121-133", render("{{cite book| title = PonTaurus| year = 2000 | first = Markus| last = Döring | pages=121-133}}"));
  }


  @Test
  public void testConvert() throws Exception {
    assertEquals("Convert", render("Convert"));
    assertEquals("about 50 m in height and 2.7 m in diameter", render("about {{Convert|50|m|abbr = on}} in height and {{Convert|2.7|m|abbr = on}} in diameter"));
  }

  /**
   * https://github.com/mdoering/wikipedia-dwca/issues/10
   * http://en.wikipedia.org/wiki/Template:RFK6.1
   */
  @Test
  @Ignore
  public void testRFPK61() throws Exception {
    assertEquals("Convert", render("Convert"));
    assertEquals("", render("<ref name=AustTRFPK6.1>{{AustTRFPK6.1 |url= http://keys.trin.org.au:8080/key-server/data/0e0f0504-0103-430d-8004-060d07080d04/media/Html/taxon/Scaevola_taccada.htm |accessdate=16 Mar 2013 }}</ref>"));
  }


}
