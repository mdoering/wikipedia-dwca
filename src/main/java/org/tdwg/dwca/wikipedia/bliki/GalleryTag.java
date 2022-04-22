package org.tdwg.dwca.wikipedia.bliki;

import info.bliki.htmlcleaner.ContentToken;
import info.bliki.wiki.filter.PlainTextConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdwg.dwca.wikipedia.taxonbox.Image;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonInfo;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonboxWikiModel;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * http://en.wikipedia.org/wiki/Help:Gallery_tag
 */
public class GalleryTag extends TaxonTag {
  private final static Logger LOG = LoggerFactory.getLogger(GalleryTag.class);
  private static Pattern IMAGES = Pattern.compile("(?:File|Image):([^|\n]+)\\s*(?:\\| *([^|\n]+))?", Pattern.CASE_INSENSITIVE);
  private static TaxonboxWikiModel internal;
  private static PlainTextConverter converter = new PlainTextConverter();

  public GalleryTag() {
    super("gallery");
  }

  void setWikiModel(TaxonboxWikiModel wikiModel) {
    internal = new TaxonboxWikiModel(wikiModel);
  }

  @Override
  public void processTaxon(TaxonInfo taxon) {
    for (Object child : getChildren()) {
      if (child instanceof ContentToken) {
        String content = ((ContentToken) child).getContent();
        // parse out Image and File
        Matcher m = IMAGES.matcher(content);
        while (m.find()) {
          Image img = new Image();
          img.setUrl(m.group(1));
          img.setTitle( parseTitle(m.group(2)) );
          taxon.getImages().add(img);
        }
      }
    }
  }

  private String parseTitle(String title) {
    try {
      return internal.render(converter, title);
    } catch (IOException e) {
      LOG.error("Error parsing title {}", title, e);
    }
    return title;
  }

}
