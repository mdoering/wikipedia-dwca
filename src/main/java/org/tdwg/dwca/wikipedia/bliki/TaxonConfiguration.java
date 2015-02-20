package org.tdwg.dwca.wikipedia.bliki;

import info.bliki.wiki.model.Configuration;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonboxWikiModel;

public class TaxonConfiguration extends Configuration {
  private static GalleryTag galleryTag = new GalleryTag();

  static {
    TAG_TOKEN_MAP.put("gallery", galleryTag);
  }

  public static TaxonConfiguration DEFAULT_CONFIGURATION = new TaxonConfiguration();

  public TaxonConfiguration() {
  }

  public static void setWikiModel(TaxonboxWikiModel wikiModel) {
    galleryTag.setWikiModel(wikiModel);
  }
}
