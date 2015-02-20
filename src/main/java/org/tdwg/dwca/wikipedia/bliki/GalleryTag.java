package org.tdwg.dwca.wikipedia.bliki;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.bliki.htmlcleaner.ContentToken;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.tags.NowikiTag;
import org.tdwg.dwca.wikipedia.taxonbox.Image;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonInfo;
import org.tdwg.dwca.wikipedia.taxonbox.TaxonboxWikiModel;


/**
 * http://en.wikipedia.org/wiki/Help:Gallery_tag
 */
public class GalleryTag extends NowikiTag {
  private static Pattern IMAGES = Pattern.compile("(?:File|Image):([^|\n]+)\\s*(?:\\| *([^|\n]+))?", Pattern.CASE_INSENSITIVE);
  private static TaxonboxWikiModel WIKI_MODEL;
  private static TaxonboxWikiModel internal;
  private static PlainTextConverter converter = new PlainTextConverter();
  private TaxonInfo taxon;

  public GalleryTag() {
    super("gallery");
  }

  void setWikiModel(TaxonboxWikiModel wikiModel) {
    GalleryTag.WIKI_MODEL = wikiModel;
    internal = new TaxonboxWikiModel(wikiModel);
  }

  @Override
  public Object clone() {
    GalleryTag tag = (GalleryTag) super.clone();
    tag.taxon = WIKI_MODEL.getTaxonInfo();
    return tag;
  }

  @Override
  public void getBodyString(Appendable buf) throws IOException {
    // only parse galleries if there is a taxon to attach to
    if (taxon == null) return;

    for (Object child : getChildren()) {
      if (child instanceof ContentToken) {
        String content = ((ContentToken) child).getContent();
        // parse out Image and File
        Matcher m = IMAGES.matcher(content);
        while (m.find()) {
          Image img = new Image();
          img.setUrl(m.group(1));
          img.setTitle( internal.render(converter, m.group(2)) );
          System.out.println(img);
          taxon.getImages().add(img);
        }
      }
    }
  }

}
