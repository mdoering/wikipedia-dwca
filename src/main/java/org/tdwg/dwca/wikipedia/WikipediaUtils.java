/*
 * Copyright 2011 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tdwg.dwca.wikipedia;

import org.gbif.api.vocabulary.Language;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class WikipediaUtils {

  private static final String WIKI_URL = "http://commons.wikimedia.org/wiki/File:";

  public static String getWikiLink(Language lang, String title) {
    try {
      return "http://"+lang.getIso2LetterCode()+".wikipedia.org/wiki/" + URLEncoder.encode(title.replaceAll(" +", "_"), "UTF-8");
    } catch (UnsupportedEncodingException e) {
    }
    return null;
  }

  public static String getWikiLink(Language lang, String title, String section) {
    String link = getWikiLink(lang, title);
    if (link != null) {
      return link + "#" + section.replaceAll(" +", "_");
    }
    return null;
  }

  /**
   * @See http://commons.wikimedia.org/wiki/Commons:Reusing%5Fcontent%5Foutside%5FWikimedia#Hotlinking
   * @param image
   * @return
   */
  public static String getImageLink(String image) {
    return "http://upload.wikimedia.org/wikipedia/commons/" + wikipediaImagePath(image);
  }

  /**
   * @See http://commons.wikimedia.org/wiki/Commons:Reusing%5Fcontent%5Foutside%5FWikimedia#Hotlinking
   * @param filename
   * @return
   */
  public static String normalizeFilename(String filename) {
    return filename.replaceAll(" +", "_");
  }

  /**
   * @param image the image name
   * @return the wikipedia commons image wiki page for a given image.
   */
  public static String getImageWikiLink(String image) {
    return WIKI_URL + WikipediaUtils.normalizeFilename(image);
  }

  /**
   * http://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Dortmund-Thomas-Konverter-IMG_0155.JPG/300px-Dortmund-Thomas-Konverter-IMG_0155.JPG
   *
   * @param image
   * @return
   */
  public static String getImageThumbnailLink(String image, int size) {
    return "http://upload.wikimedia.org/wikipedia/commons/thumb/" + wikipediaImagePath(image)+"/"+size+"px-"+ normalizeFilename(
      image);
  }

  /**
   * wikipedias default thumbnail size of 220 pixel
   * @param image
   * @return
   */
  public static String getImageThumbnailLink(String image) {
    return getImageThumbnailLink(image, 220);
  }

  public static String cleanText(String text) {
    return text.replaceAll("\\[\\s*\\[(.*)\\]\\s*\\]", " $1 ").replaceAll(" thumb\\|", " ").replaceAll("\\{\\{.+\\}\\}", " ");
  }

  /**
   * see http://stackoverflow.com/questions/247678/how-does-mediawiki-compose-the-image-paths
   * @param image
   * @return
   */
  public static String wikipediaImagePath(String image) {
    try {
      String filename = normalizeFilename(image);
      String digest = md5Hash(filename);
      return String.format("%s/%s%s/%s", digest.charAt(0), digest.charAt(0), digest.charAt(1), URLEncoder.encode(filename, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
    }
    return null;
  }

  public static String md5Hash(String original) {
    try {
      MessageDigest m = MessageDigest.getInstance("MD5");
      byte[] data = original.getBytes("UTF-8");
      m.update(data, 0, data.length);
      BigInteger bigInt = new BigInteger(1, m.digest());
      String hashtext = bigInt.toString(16);
      // Now we need to zero pad it if you actually want the full 32 chars.
      while (hashtext.length() < 32) {
        hashtext = "0" + hashtext;
      }
      return hashtext;
    } catch (UnsupportedEncodingException e) {
    } catch (NoSuchAlgorithmException e) {
    }
    return null;
  }
}
