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

/**
 * @See http://de.wikipedia.org/wiki/Wikipedia:Taxoboxen
 */
abstract class TaxonInfoDE extends TaxonInfoEN {

  public static final String TAXOBOX_NAME = "Taxobox";

  public void setTaxon_name(String name) {
    this.name=name;
  }

  public void setTaxon_wissName(String name) {
    scientificName = name;
  }
  public void setTaxon_wissname(String name) {
    setTaxon_wissName(name);
  }

  public void setTaxon_autor(String author) {
    scientificNameAuthorship = author;
  }

  public void setTaxon_rang(String rank) {
    rankVerbatim = rank;
    this.rank=Rank.fromString(rank);
  }

  public void setTaxon1_wissName(String name) {
    setTaxon_wissName(name);
  }

  public void setTaxon1_wissname(String name) {
    setTaxon_wissName(name);
  }

  public void setTaxon1_autor(String author) {
    setTaxon_autor(author);
  }

  public void setTaxon1_rang(String rank) {
    setTaxon_rang(rank);
  }

  public void setTaxon2_name(String name) {
    classificationByIndex(7).setVernacular(name);
  }
  public void setTaxon2_wissName(String name) {
    classificationByIndex(7).setScientific(name);
  }
  public void setTaxon2_wissname(String name) {
    setTaxon2_wissName(name);
  }
  public void setTaxon2_rang(String rank) {
    classificationByIndex(7).setRank(rank);
  }
  public void setTaxon2_autor(String autor) {
    classificationByIndex(7).setAuthor(autor);
  }

  public void setTaxon3_name(String name) {
    classificationByIndex(6).setVernacular(name);
  }

  public void setTaxon3_wissName(String name) {
    classificationByIndex(6).setScientific(name);
  }

  public void setTaxon3_wissname(String name) {
    setTaxon3_wissName(name);
  }

  public void setTaxon3_autor(String autor) {
    classificationByIndex(6).setAuthor(autor);
  }


  public void setTaxon3_rang(String rank) {
    classificationByIndex(6).setRank(rank);
  }

  public void setTaxon4_name(String name) {
    classificationByIndex(5).setVernacular(name);
  }

  public void setTaxon4_wissName(String name) {
    classificationByIndex(5).setScientific(name);
  }

  public void setTaxon4_wissname(String name) {
    setTaxon4_wissName(name);
  }

  public void setTaxon4_autor(String autor) {
    classificationByIndex(5).setAuthor(autor);
  }

  public void setTaxon4_rang(String rank) {
    classificationByIndex(5).setRank(rank);
  }

  public void setTaxon5_name(String name) {
    classificationByIndex(4).setVernacular(name);
  }

  public void setTaxon5_wissName(String name) {
    classificationByIndex(4).setScientific(name);
  }

  public void setTaxon5_wissname(String name) {
    setTaxon5_wissName(name);
  }

  public void setTaxon5_autor(String autor) {
    classificationByIndex(4).setAuthor(autor);
  }

  public void setTaxon5_rang(String rank) {
    classificationByIndex(4).setRank(rank);
  }

  public void setTaxon6_name(String name) {
    classificationByIndex(3).setVernacular(name);
  }

  public void setTaxon6_wissName(String name) {
    classificationByIndex(3).setScientific(name);
  }
  public void setTaxon6_wissname(String name) {
    setTaxon6_wissName(name);
  }

  public void setTaxon6_autor(String autor) {
    classificationByIndex(3).setAuthor(autor);
  }

  public void setTaxon6_rang(String rank) {
    classificationByIndex(3).setRank(rank);
  }

  public void setTaxon7_name(String name) {
    classificationByIndex(2).setVernacular(name);
  }

  public void setTaxon7_wissName(String name) {
    classificationByIndex(2).setScientific(name);
  }
  public void setTaxon7_wissname(String name) {
    setTaxon7_wissName(name);
  }

  public void setTaxon7_autor(String autor) {
    classificationByIndex(2).setAuthor(autor);
  }

  public void setTaxon7_rang(String rank) {
    classificationByIndex(2).setRank(rank);
  }


  public void setTaxon8_name(String name) {
    classificationByIndex(1).setVernacular(name);
  }

  public void setTaxon8_wissName(String name) {
    classificationByIndex(1).setScientific(name);
  }

  public void setTaxon8_wissname(String name) {
    setTaxon7_wissName(name);
  }

  public void setTaxon8_autor(String autor) {
    classificationByIndex(1).setAuthor(autor);
  }

  public void setTaxon8_rang(String rank) {
    classificationByIndex(1).setRank(rank);
  }

  public void setTaxon9_name(String name) {
    classificationByIndex(0).setVernacular(name);
  }

  public void setTaxon9_wissName(String name) {
    classificationByIndex(0).setScientific(name);
  }

  public void setTaxon9_wissname(String name) {
    setTaxon7_wissName(name);
  }

  public void setTaxon9_autor(String autor) {
    classificationByIndex(0).setAuthor(autor);
  }

  public void setTaxon9_rang(String rank) {
    classificationByIndex(0).setRank(rank);
  }
  public void setBildbeschreibung(String image_caption) {
    image(0).setImageCaption(image_caption);
  }

  public void setBild(String image) {
    if (image != null && image.equalsIgnoreCase("ohne")) {
      image = null;
    }
    image(0).setImage(image);
  }

  public void setBildbeschreibung2(String image_caption) {
    image(1).setImageCaption(image_caption);
  }

  public void setBild2(String image) {
    if (image != null && image.equalsIgnoreCase("ohne")) {
      image = null;
    }
    image(1).setImage(image);
  }

  public void setBildbeschreibung3(String image_caption) {
    image(2).setImageCaption(image_caption);
  }

  public void setBild3(String image) {
    if (image != null && image.equalsIgnoreCase("ohne")) {
      image = null;
    }
    image(2).setImage(image);
  }

  public void setErdzeitaltervon(String from){
    fossilRangeFrom = from;
  }

  public void setErdzeitalterbis(String to) {
    fossilRangeFrom = to;
  }

  /**
   * thousands
   * @param from
   */
  public void setTausendvon(String from) {
    try {
      Integer x = Integer.parseInt(from);
      fossilRangeFromMio = x / 1000.0;
    } catch (NumberFormatException e) {
      // TODO: Handle exception
    }
  }

  /**
   * in thousand years
   * @param to
   */
  public void setTausendbis(String to) {
    try {
      Integer x = Integer.parseInt(to);
      fossilRangeToMio = x / 1000.0;
    } catch (NumberFormatException e) {
      // TODO: Handle exception
    }
  }

  /**
   * in million years
   * @param from
   */
  public void setMiovon(String from) {
    try {
      Integer x = Integer.parseInt(from);
      fossilRangeFromMio= x.doubleValue();
    } catch (NumberFormatException e) {
      // TODO: Handle exception
    }
  }

  /**
   * in million years
   * @param to
   */
  public void setMiobis(String to) {
    try {
      Integer x = Integer.parseInt(to);
      fossilRangeToMio = x.doubleValue();
    } catch (NumberFormatException e) {
      // TODO: Handle exception
    }
  }

  public void setFundorte(String localities) {
    fossilLocalities=localities       ;
  }

}
