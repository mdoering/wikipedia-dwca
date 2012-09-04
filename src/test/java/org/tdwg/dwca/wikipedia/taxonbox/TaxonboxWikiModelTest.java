package org.tdwg.dwca.wikipedia.taxonbox;

import java.io.IOException;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TaxonboxWikiModelTest {

  private WikiModel wiki = new TaxonboxWikiModel("en");
  private PlainTextConverter converter = new PlainTextConverter();

  private String render(String wikiText) {
    Appendable app = new StringBuilder();
    try {
      wiki.render(converter, wikiText, app, false, true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return app.toString();
  }

  @Test
  public void testHybrid() throws Exception {
    assertEquals("A  ×  B", render("A {{Hybrid}} B"));
    assertEquals("×", render("{{hybrid}}").trim());
    assertEquals("×", render("{{hybrid|hallo}}").trim());
  }

  @Test
  public void testFossilRange() throws Exception {
    assertEquals("68-65 Ma", render("68-65 Ma"));
    assertEquals("68-65 Ma", render("68-65 Ma{{markus}}"));
    assertEquals("Permian", render("{{Fossil range|Permian}}"));
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
