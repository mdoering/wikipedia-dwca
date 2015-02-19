package org.tdwg.dwca.wikipedia.taxonbox;

import org.junit.Test;

public class AutomaticTaxonomyScraperTest {

  @Test
  public void testUpdateTaxonInfo() throws Exception {
    // http://en.wikipedia.org/wiki/Template:Taxonomy/Haliaeetus_leucocephalus
    TaxonInfo taxon = new TaxonInfo();
    taxon.setScientificName("Haliaeetus leucocephalus");
    AutomaticTaxonomyScraper.updateTaxonInfo(taxon);

    System.out.println(taxon);
  }
}
