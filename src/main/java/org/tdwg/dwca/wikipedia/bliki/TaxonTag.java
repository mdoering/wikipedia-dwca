package org.tdwg.dwca.wikipedia.bliki;

import info.bliki.wiki.tags.NowikiTag;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonInfo;

/**
 *
 */
public abstract class TaxonTag extends NowikiTag {
  private TaxonInfo taxon;

  public TaxonTag(String name) {
    super(name);
  }

  public void setTaxon(TaxonInfo taxon) {
    this.taxon = taxon;
  }

  abstract void processTaxon(TaxonInfo taxon);

  @Override
  public void addChild(Object child) {
    super.addChild(child);
    if (taxon != null) {
      processTaxon(taxon);
    }
  }

}
