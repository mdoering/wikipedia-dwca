package org.tdwg.dwca.wikipedia;

import com.beust.jcommander.internal.Lists;
import org.gbif.utils.HttpUtil;
import org.junit.Before;
import org.junit.Test;
import org.tdwg.dwca.wikipedia.taxonbox.Image;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WikimediaScraperTest {
  WikimediaScraper scraper;
  @Before
  public void init() {
    scraper = new WikimediaScraper(HttpUtil.newSinglethreadedClient(20000));
  }
  @Test
  public void testScrape() throws Exception {
    Image img = new Image();

    img.setUrl("Puma_Sleeping.jpg");
    scraper.scrape(img);
    assertEquals("Own work", img.getSource());
    assertEquals("1 March 2007", img.getDate());
    assertEquals("Ltshears - Trisha M Shears", img.getAuthor());
    assertEquals("Public Domain", img.getLicense());


    img = new Image();
    img.setUrl("Tritylodon_BW.jpg");
    scraper.scrape(img);
    assertEquals("Own work", img.getSource());
    assertEquals("31 August 2007", img.getDate());
    assertEquals("Nobu Tamura (http://spinops.blogspot.com)", img.getAuthor());
    assertEquals("GNU Free Documentation License", img.getLicense());
    assertEquals("Tritylodon longaevus, a cynodont from the Early Jurassic of South Africa, pencil drawing", img.getDescription());

    img = new Image();
    img.setUrl("Eichh%C3%B6rnchen_D%C3%BCsseldorf_Hofgarten.jpg");
    scraper.scrape(img);
    assertEquals("Creative Commons Attribution Share Alike 2.0 Germany", img.getLicense());
  }

  @Test
  public void testScrapeLicenses() throws Exception {
    List<String> files = Lists.newArrayList(
        "\"Biman_Bangladesh_Airlines,Boeing_777-3E9ER.jpg",

      "Eichhörnchen_Düsseldorf_Hofgarten_edit.jpg",
      "Sciurus-vulgaris_hernandeangelis_stockholm_2008-06-04.jpg",
      "Ab_sciurus_vulgaris.jpg",
      "Young_aberts.jpg",
      "Puma_Sleeping.jpg",
      "\"Biman_Bangladesh_Airlines,Boeing_777-3E9ER.jpg",
      "\"_in_São_Paulo_(By_Felipe_Mostarda).jpg"
    );

    for (String f : files) {
      Image img = new Image();
      img.setUrl(f);
      scraper.scrape(img);
      assertNotNull(f, img.getLicense());
      assertNotNull(f, img.getDate());
      assertNotNull(f, img.getDescription());

      System.out.println(img);
    }
  }

}
