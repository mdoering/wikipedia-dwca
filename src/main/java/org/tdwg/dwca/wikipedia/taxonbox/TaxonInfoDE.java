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
package org.tdwg.dwca.wikipedia.taxonbox;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.gbif.api.vocabulary.Kingdom;
import org.gbif.api.vocabulary.Language;

import java.util.Map;
import java.util.Set;

/**
 * http://de.wikipedia.org/wiki/Wikipedia:Taxoboxen
 */
public class TaxonInfoDE extends TaxonInfoEN {
  private static final Language WIKI_LANG = Language.GERMAN;
  private static final Map<Kingdom, String> KINGDOM_PAGES = ImmutableMap.<Kingdom, String>builder()
    .put(Kingdom.ANIMALIA, "Tier")
    .put(Kingdom.ARCHAEA, "Archaeen")
    .put(Kingdom.BACTERIA, "Bakterien")
    //.put(Kingdom.CHROMISTA, null)
    .put(Kingdom.FUNGI, "Pilze")
    .put(Kingdom.PLANTAE, "Pflanzen")
    .put(Kingdom.PROTOZOA, "Protozoen")
    .put(Kingdom.VIRUSES, "Viren")
    .build();
  public static final Set<String> IGNORE_SETIONS = Sets.newHashSet("einzelnachweise", "quellen", "weblinks", "literatur");

  @Override
  protected String knownPageTitle(Kingdom kingdom, Language lang) {
    if (WIKI_LANG == lang) {
      return KINGDOM_PAGES.get(kingdom);
    }
    return super.knownPageTitle(kingdom, lang);
  }

  /**
   * http://de.wikipedia.org/wiki/Wikipedia:Pal%C3%A4oboxen
   * Used for extinct palaeo taxa
   * @param modus
   */
  public void setModus(String modus) {
    if ("Paläobox".equalsIgnoreCase(modus)) {
      setExtinct("true");
    }
  }

  public void setTaxon_name(String name) {
    name(0).setVernacular(name);
  }

  public void setTaxon_wissname(String name) {
    name(0).setScientific(name);
  }

  public void setTaxon_autor(String author) {
    name(0).setAuthor(author);
  }

  public void setTaxon_rang(String rank) {
    name(0).setRankAndVerbatim(rank);
  }

  public void setTaxon1_name(String name) {
    name(1).setVernacular(name);
  }

  public void setTaxon1_wissname(String name) {
    name(1).setScientific(name);
  }

  public void setTaxon1_autor(String author) {
    name(1).setAuthor(author);
  }

  public void setTaxon1_rang(String rank) {
    name(1).setRankAndVerbatim(rank);
  }

  public void setTaxon2_name(String name) {
    name(2).setVernacular(name);
  }

  public void setTaxon2_wissname(String name) {
    name(2).setScientific(name);
  }

  public void setTaxon2_rang(String rank) {
    name(2).setRankAndVerbatim(rank);
  }

  public void setTaxon2_autor(String autor) {
    name(2).setAuthor(autor);
  }

  public void setTaxon3_name(String name) {
    name(3).setVernacular(name);
  }

  public void setTaxon3_wissname(String name) {
    name(3).setScientific(name);
  }

  public void setTaxon3_autor(String autor) {
    name(3).setAuthor(autor);
  }

  public void setTaxon3_rang(String rank) {
    name(3).setRankAndVerbatim(rank);
  }

  public void setTaxon4_name(String name) {
    name(4).setVernacular(name);
  }

  public void setTaxon4_wissname(String name) {
    name(4).setScientific(name);
  }

  public void setTaxon4_autor(String autor) {
    name(4).setAuthor(autor);
  }

  public void setTaxon4_rang(String rank) {
    name(4).setRankAndVerbatim(rank);
  }

  public void setTaxon5_name(String name) {
    name(5).setVernacular(name);
  }

  public void setTaxon5_wissname(String name) {
    name(5).setScientific(name);
  }

  public void setTaxon5_autor(String autor) {
    name(5).setAuthor(autor);
  }

  public void setTaxon5_rang(String rank) {
    name(5).setRankAndVerbatim(rank);
  }

  public void setTaxon6_name(String name) {
    name(6).setVernacular(name);
  }

  public void setTaxon6_wissname(String name) {
    name(6).setScientific(name);
  }

  public void setTaxon6_autor(String autor) {
    name(6).setAuthor(autor);
  }

  public void setTaxon6_rang(String rank) {
    name(6).setRankAndVerbatim(rank);
  }

  public void setTaxon7_name(String name) {
    name(7).setVernacular(name);
  }

  public void setTaxon7_wissname(String name) {
    name(7).setScientific(name);
  }

  public void setTaxon7_autor(String autor) {
    name(7).setAuthor(autor);
  }

  public void setTaxon7_rang(String rank) {
    name(7).setRankAndVerbatim(rank);
  }


  public void setTaxon8_name(String name) {
    name(8).setVernacular(name);
  }

  public void setTaxon8_wissname(String name) {
    name(8).setScientific(name);
  }

  public void setTaxon8_autor(String autor) {
    name(8).setAuthor(autor);
  }

  public void setTaxon8_rang(String rank) {
    name(8).setRankAndVerbatim(rank);
  }

  public void setTaxon9_name(String name) {
    name(9).setVernacular(name);
  }

  public void setTaxon9_wissname(String name) {
    name(9).setScientific(name);
  }

  public void setTaxon9_autor(String autor) {
    name(9).setAuthor(autor);
  }

  public void setTaxon9_rang(String rank) {
    name(9).setRankAndVerbatim(rank);
  }
  public void setBildbeschreibung(String image_caption) {
    setImage_caption(image_caption);
  }

  public void setBild(String image) {
    if (image != null && image.equalsIgnoreCase("ohne")) {
      image = null;
    }
    setImage(image);
  }

  public void setBildbeschreibung2(String image_caption) {
    setImage2_caption(image_caption);
  }

  public void setBild2(String image) {
    if (image != null && image.equalsIgnoreCase("ohne")) {
      image = null;
    }
    setImage2(image);
  }

  public void setBildbeschreibung3(String image_caption) {
    image(2).setTitle(image_caption);
  }

  public void setBild3(String image) {
    if (image != null && image.equalsIgnoreCase("ohne")) {
      image = null;
    }
    image(2).setUrl(image);
  }

  public void setErdzeitaltervon(String from){
    setFossilRangeFrom(from);
  }

  public void setErdzeitalterbis(String to) {
    setFossilRangeTo(to);
  }

  /**
   * thousands
   * @param from
   */
  public void setTausendvon(String from) {
    try {
      Integer x = Integer.parseInt(from);
      setFossilRangeFromMio(x / 1000.0);
    } catch (NumberFormatException e) {
      log.warn("Cannot parse integer FossilRangeFromMio {}", from);
    }
  }

  /**
   * in thousand years
   * @param to
   */
  public void setTausendbis(String to) {
    try {
      Integer x = Integer.parseInt(to);
      setFossilRangeToMio(x / 1000.0);
    } catch (NumberFormatException e) {
      log.warn("Cannot parse integer FossilRangeToMio {}", to);
    }
  }

  /**
   * in million years
   * @param from
   */
  public void setMiovon(String from) {
    try {
      Integer x = Integer.parseInt(from);
      setFossilRangeFromMio(x.doubleValue());
    } catch (NumberFormatException e) {
      log.warn("Cannot parse integer FossilRangeFromMio {}", from);
    }
  }

  /**
   * in million years
   * @param to
   */
  public void setMiobis(String to) {
    try {
      Integer x = Integer.parseInt(to);
      setFossilRangeToMio(x.doubleValue());
    } catch (NumberFormatException e) {
      log.warn("Cannot parse integer FossilRangeToMio {}", to);
    }
  }

  public void setFundorte(String localities) {
    setFossilLocalities(localities);
  }

}
