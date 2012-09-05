package org.tdwg.dwca.wikipedia.taxonbox;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TaxonInfoTest {

  @Test
  public void testNameExpansion(){
    TaxonInfo tax = new TaxonInfo();
    tax.setGenus("Abies");
    tax.setScientificName("Abies alba");

    tax.postprocess(null, null);
    assertEquals("Abies alba", tax.getScientificName());

    tax.setScientificName("A. alba");
    tax.postprocess(null, null);
    assertEquals("Abies alba", tax.getScientificName());

    tax.setScientificName("A. a. var. alpina");
    tax.postprocess(null, null);
    assertEquals("Abies a. var. alpina", tax.getScientificName());

    tax.setSpeciesEpithet("alba");
    tax.setScientificName("A. a. var. alpina");
    tax.postprocess(null, null);
    assertEquals("Abies alba var. alpina", tax.getScientificName());
  }

}
