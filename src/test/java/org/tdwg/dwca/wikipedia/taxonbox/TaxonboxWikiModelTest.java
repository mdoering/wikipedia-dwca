package org.tdwg.dwca.wikipedia.taxonbox;

import org.gbif.utils.file.InputStreamUtils;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TaxonboxWikiModelTest {

  private WikiModel wiki = new TaxonboxWikiModel("en");
  private PlainTextConverter converter = new PlainTextConverter();
  private InputStreamUtils isu = new InputStreamUtils();

  private String render(String wikiText) {
    return StringUtils.normalizeSpace(wiki.render(converter, wikiText));
  }

  @Test
  public void testRender() throws Exception {
    WikiModel wiki = new TaxonboxWikiModel("en");
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
    assertEquals("citation", render("citation"));
    assertEquals("UniProt: Order Rosales", render("{{cite web|url = http://beta.uniprot.org/taxonomy/3744| title = Order '''Rosales'''| accessdate = 2008-04-24| author = UniProt| authorlink = UniProt}}"));
    assertEquals("Döring, Markus (2000): PonTaurus: pp. 121-133", render("{{cite book| title = PonTaurus| year = 2000 | first = Markus| last = Döring | pages=121-133}}"));
  }

}
