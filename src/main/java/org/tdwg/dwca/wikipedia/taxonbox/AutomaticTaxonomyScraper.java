package org.tdwg.dwca.wikipedia.taxonbox;

import java.io.IOException;

import com.google.common.base.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdwg.dwca.wikipedia.WikipediaUtils;

public class AutomaticTaxonomyScraper {
  private static final String PREFIX = "http://en.wikipedia.org/wiki/Template:Taxonomy/Quetzalcoatlus";
  private static final Logger LOG = LoggerFactory.getLogger(AutomaticTaxonomyScraper.class);

  public static void updateTaxonInfo(TaxonInfo taxon) {
    if (!Strings.isNullOrEmpty(taxon.getScientificName())) {
      String url = PREFIX + WikipediaUtils.normalizeFilename(taxon.getScientificName());
      try {
        parse(url, taxon);
      } catch (IOException e) {
        LOG.error("Error parsing automatic taxonomy template for url {}", url);
      } catch (Exception e) {
        LOG.error("Error parsing automatic taxonomy template for url {}", url);
      }
    }
  }

  private static void parse(String url, TaxonInfo taxon) throws IOException {
    Document doc = Jsoup.connect(url).get();
    Element content = doc.getElementById("mw-content-text");
    if(content != null){

      // extinct & rank
      Element table = content.getElementsByClass("wikitable").first();
      if (table != null) {
        Elements rows = table.getElementsByTag("tr");
        if(rows != null){
          for (Element row : rows) {
            Elements cols = row.getElementsByTag("td");
            if (cols.size() == 2) {
              setKeyVal(taxon, cols.get(0).text(), cols.get(1).getElementsByTag("code").first());
            }
          }
        }
      }

      // classification
      Element classification = content.getElementsByClass("biota").first();
      if (classification != null) {
        Elements rows = classification.getElementsByTag("tr");
        if(rows != null){
          for (Element row : rows) {
            Elements cols = row.getElementsByTag("td");
            if (cols.size() == 2) {
              setHigherTaxon(taxon, cols.get(0).text(), cols.get(1).getElementsByTag("span").first());
            }
          }
        }
      }
    }
  }

  /**
   * We ignore all clades and unranked parents and only keep the recognizable ranks.
   */
  private static void setHigherTaxon(TaxonInfo taxon, String rankVerbatim, Element valueElem) {
    if (!Strings.isNullOrEmpty(rankVerbatim)) {
      Rank rank = Rank.fromString(rankVerbatim);
      if (rank != null) {
        // value is the first span
        Element valueElem2 = valueElem.getElementsByTag("span").first();
        if (valueElem2 != null) {
          // ignore extinct symbol
          String value = valueElem2.text().replace("†", "").trim();
          switch (rank) {
            case Kingdom: taxon.setKingdom(value); break;
            case Phylum: taxon.setPhylum(value); break;
            case Class: taxon.setClassis(value); break;
            case Order: taxon.setOrder(value); break;
            case Family: taxon.setFamily(value); break;
            case Genus: taxon.setGenus(value); break;
            case Subgenus: taxon.setSubgenus(value); break;
          }
        }
      }
    }
  }


  private static void setKeyVal(TaxonInfo taxon, String key, Element codeValue) {
    if (codeValue != null) {
      String val = codeValue.text();
      if ("extinct".equalsIgnoreCase(key)) {
        taxon.setExtinct(val);

      } else if ("rank".equalsIgnoreCase(key)) {
        taxon.setRankVerbatim(val);
        if (taxon.getRank() == null) {
          taxon.setRank(Rank.fromString(val));
        }
      }
    }
  }

}
