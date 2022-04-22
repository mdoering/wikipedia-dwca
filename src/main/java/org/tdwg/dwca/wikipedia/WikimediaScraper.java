package org.tdwg.dwca.wikipedia;

import com.beust.jcommander.internal.Maps;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.gbif.utils.ExtendedResponse;
import org.gbif.utils.HttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdwg.dwca.wikipedia.taxonbox.Media;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class WikimediaScraper {
  private static final Logger LOG = LoggerFactory.getLogger(WikimediaScraper.class);
  private final HttpClient http;
  private final Map<String, String> licenses = Maps.newHashMap();
  private Writer noLicenses;

  public WikimediaScraper(HttpClient http, File noLicenseFile) throws IOException {
    this(http);
    if (noLicenseFile != null) {
      noLicenses = new FileWriter(noLicenseFile);
    }
  }

  public WikimediaScraper(HttpClient http) {
    this.http = http;
    licenses.put("pd", "Public Domain");
    licenses.put("pd-self", "Public Domain");
    licenses.put("pd-us-nps", "Public Domain images from the U.S. National Park Service");
    licenses.put("pd-author", "Public Domain");
    licenses.put("pd-user", "Public Domain");
    licenses.put("pd-us", "Public Domain");
    licenses.put("pd-usda", "Public Domain from United States Department of Agriculture");
    licenses.put("gfdl", "GNU Free Documentation License");
    licenses.put("fal", "Free Art License");
    licenses.put("odc", "Open Data Commons");
    licenses.put("cc0", "CC0 No Rights Reserved");
    licenses.put("cc-pd-mark", "Public Domain Mark 1.0");

    Map<String, String> commons = ImmutableMap.<String, String>builder()
      .put("by", "Attribution")
      .put("by-sa", "Attribution Share Alike")
      .put("by-nd", "Attribution No Derivatives")
      .put("by-nc", "Attribution Non-Commercial")
      .put("by-nc-sa", "Attribution Non-Commercial Share Alike")
      .put("by-nc-nd", "Attribution Non-Commercial No Derivatives")
      .build();
    Map<String, String> versions = ImmutableMap.<String, String>builder()
      .put("-2.0", " 2.0")
      .put("-3.0", " 3.0")
      .build();
    Map<String, String> langs = ImmutableMap.<String, String>builder()
      .put("", " Unported")
      .put("-de", " Germany")
      .put("-fr", " France")
      .put("-es", " Spain")
      .put("-us", " United States")
      .put("-uk", " UK")
      .put("-migrated", " Migrated")
      .build();
    final String prefix = "Creative Commons ";

    for (Map.Entry<String, String> attr : commons.entrySet()) {
      licenses.put("cc-" + attr.getKey(), prefix + attr.getValue());
      for (Map.Entry<String, String> v : versions.entrySet()) {
        licenses.put("cc-" + attr.getKey() + v.getKey(), prefix + attr.getValue() + v.getValue());
        for (Map.Entry<String, String> l : langs.entrySet()) {
          licenses.put("cc-" + attr.getKey() + v.getKey() + l.getKey(),
            prefix + attr.getValue() + v.getValue() + l.getValue());
        }
      }
    }
  }

  public void scrape(Media img) {
    try {
      parse(WikipediaUtils.getImageWikiLink(img.getUrl()), img);
      img.setPublisher("Wikimedia Commons");

    } catch (Exception e) {
      LOG.warn("Cannot scrape image metadata for {}", img.getUrl());
    }
  }

  private void parse(String url, Media img) throws IOException, URISyntaxException {
    Document doc = null;
    try {
    ExtendedResponse resp = null;
      resp = http.get(url);
      if (resp.getStatusCode() / 100 == 2) {
        doc = Jsoup.parse(resp.getContent());
      } else {
        LOG.warn("Failed to retrieve media object {}. HTTP {}", url, resp.getStatusCode());
        return;
      }
    } catch (URISyntaxException | IllegalArgumentException e) {
      LOG.debug("Failed to retrieve media object {}. Try again with plain URL connection: {}", url, e.getMessage());
      // try with plain URL connection which avoids URI instances that refuse some wikipedia URLs having quotes
      InputStream stream = new URL(url).openStream();
      doc = Jsoup.parse(stream, null, WikipediaUtils.WIKI_BASE);
    }

    Element summary = doc.getElementById("mw-imagepage-content");
    if(summary!=null){
      // basic properties
      Elements rows = summary.getElementsByTag("tr");
      if(rows != null){
        for (Element row : rows) {
          Elements cols = row.getElementsByTag("td");
          if (cols.size()==2) {
            setKeyVal(img, cols.get(0).text(), cols.get(1).text());
          }
        }
      }
      // this is too tough to parse...
      Elements licenses = summary.getElementsByClass("licensetpl");
      if (licenses != null) {
        for (Element table : licenses) {
        }
      }
      // special license footer categories
      Element catFooter = doc.getElementById("mw-hidden-catlinks");
      Elements hiddenCategories = catFooter.getElementsByTag("a");
      for (Element a : hiddenCategories) {
        setLicense(img, a.text());
      }

      // log images without a license
      if (noLicenses != null && Strings.isNullOrEmpty(img.getLicense())) {
        noLicenses.write("No image license found for " + url);
      }

    } else {
      if (noLicenses != null) {
        noLicenses.write("No image metadata found for " + url);
      }
    }
  }

  /**
   * Sets a license if none is yet set and we can interpret the value as a real license value.
   */
  private void setLicense(Media img, String val) {
    if (!Strings.isNullOrEmpty(val) && Strings.isNullOrEmpty(img.getLicense())) {
      String v = val.toLowerCase().trim().replace(" ", "-").replace("_", "-");
      if (licenses.containsKey(v)) {
        img.setLicense(this.licenses.get(v));
      } else {
        // accept any license value that starts with cc- pd or gfdl
        if (v.startsWith("cc-") || v.startsWith("pd-") || v.startsWith("gfdl-")) {
          img.setLicense(val);
        }
      }
    }
  }

  private void setKeyVal(Media img, String key, String val) {
    if (!Strings.isNullOrEmpty(key)) {
      String prop = key.trim().toLowerCase();
      if (prop.startsWith("description")) {
        img.setDescription(val.trim());
      } else if (prop.startsWith("author")) {
        img.setAuthor(val.trim());
      } else if (prop.startsWith("date")) {
        img.setDate(val.trim());
      } else if (prop.startsWith("source")) {
        img.setSource(val.trim());
      } else if (prop.startsWith("permission")) {
        setLicense(img, val.trim());
      }
    }
  }
}
