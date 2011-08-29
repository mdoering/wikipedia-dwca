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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @See http://en.wikipedia.org/wiki/Template:Taxobox
 */
abstract class TaxonInfoEN {
  public static final String TAXOBOX_NAME = "Taxobox";
  protected static final Logger log = LoggerFactory.getLogger(TaxonInfo.class);
  //name
  protected String scientificName;
  protected String scientificNameAuthorship;
  protected Rank rank;
  protected String rankVerbatim;
  protected String name; // vernacular
  // conservation status
  protected String status; // iucn status
  protected String extinct; // year
  protected String statusSystem;
  protected RefInfo statusRef;
  protected String fossilRange;
  protected String fossilLocalities;
  protected String fossilRangeFrom;
  protected String fossilRangeTo;
  protected Double fossilRangeFromMio;
  protected Double fossilRangeToMio;
  protected String trend;
  // images
  protected List<Image> images = new ArrayList<Image>();
  // range maps
  protected List<Image> rangeMaps = new ArrayList<Image>();
  // classification
  protected String classificationStatus;
  protected LinkedList<Name> classification = new LinkedList<Name>();
  // synonyms
  protected List<String> synonyms = new ArrayList<String>();
  protected String synonymsRef;
  // types
  protected String typeSpecies;
  protected String typeSpeciesAuthority;
  protected String typeGenus;
  protected String typeGenusAuthority;
  //
  protected String diversity; // c. 120species
  protected String diversityLink;



  public void setBinomial(String binomial) {
    setScientificName(Rank.Species, binomial);
  }

  public void setLatin_name(String binomial) {
    scientificName=binomial;
  }

  /**
   * common typo
   */
  public void setBinominal(String binomial) {
    setBinomial(binomial);
  }



  protected boolean isLowestName(Rank r){
    if (scientificName==null || rank==null || r.ordinal() >= rank.ordinal()){
      return true;
    }
    return false;
  }

  public void setBinomial_authority(String binomial_authority) {
    setScientificNameAuthorship(Rank.Species, binomial_authority);
  }

  /**
   * common typo
   */
  public void setBinominal_authority(String binomial_authority) {
    setBinomial_authority(binomial_authority);
  }

  public void setTrinomial(String trinomial) {
    setScientificName(Rank.Subspecies, trinomial);
  }

  public void setTrinomial_authority(String trinomial_authority) {
    setScientificNameAuthorship(Rank.Subspecies, trinomial_authority);
  }

  private void setScientificName(Rank rank, String name){
    classificationByRank(rank).setScientific(name);
    classificationByRank(rank).setRank(rank);
    if (isLowestName(rank)) {
      scientificName = name;
      this.rank=rank;
    }
  }

  private void setScientificNameAuthorship(Rank rank, String authorship) {
    classificationByRank(rank).setAuthor(authorship);
    classificationByRank(rank).setRank(rank);
    if (isLowestName(rank)) {
      scientificNameAuthorship = authorship;
    }
  }

  public void setName(Rank rank, String name) {
    classificationByRank(rank).setVernacular(name);
    classificationByRank(rank).setRank(rank);
    if (isLowestName(rank)) {
      this.name = name;
    }
  }

  public void setName(String name) {
    this.name = name;
  }



  public void setStatus(String status) {
    this.status = status;
  }


  public void setExtinct(String extinct) {
    this.extinct = extinct;
  }


  public void setStatus_system(String status_system) {
    this.statusSystem = status_system;
  }


  public void setStatus_ref(RefInfo status_ref) {
    this.statusRef = status_ref;
  }


  public void setFossil_range(String fossil_range) {
    this.fossilRange = fossil_range;
  }


  public void setTrend(String trend) {
    this.trend = trend;
  }


  public void setRank(Rank rank) {
    this.rank = rank;
  }


  public void setRankVerbatim(String rankVerbatim) {
    this.rankVerbatim = rankVerbatim;
  }

  protected Image image(int idx) {
    try {
      if (images.get(idx) == null) {
        images.set(idx, new Image());
      }
    } catch (IndexOutOfBoundsException e) {
      while(images.size() <= idx){
        images.add(null);
      }
      images.set(idx, new Image());
    }
    return images.get(idx);
  }

  protected Name classificationByIndex(int idx) {
    if (classification.size() > idx) {
      classification.set(idx, new Name());
    }else{
      while (classification.size() <= idx) {
        classification.add(null);
      }
      classification.set(idx, new Name());
    }
    return classification.get(idx);
  }

  protected Name classificationByRank(Rank rank) {
    // reserve the first 10 names for classification given by regular list, not ranks
    return classificationByIndex(rank.ordinal() + 9);
  }

  public void setImage(String image) {
    image(0).setImage(image);
  }

  public void setImage_alt(String image_alt) {
    image(0).setImageAlt(image_alt);
  }

  public void setImage_caption(String image_caption) {
    image(0).setImageCaption(image_caption);
  }


  public void setImage2(String image) {
    image(1).setImage(image);
  }

  public void setImage2_alt(String image_alt) {
    image(1).setImageAlt(image_alt);
  }

  public void setImage2_caption(String image_caption) {
    image(1).setImageCaption(image_caption);
  }

  public void setClassification_status(String classification_status) {
    this.classificationStatus = classification_status;
  }

  public void setRegnum(String regnum) {
    setScientificName(Rank.Kingdom, regnum);
  }


  public void setPhylum(String phylum) {
    setScientificName(Rank.Phylum, phylum);
  }


  public void setClassis(String classis) {
    setScientificName(Rank.Class, classis);
  }

  public void setOrder(String order) {
    setScientificName(Rank.Order, order);
  }

  public void setOrdo(String order) {
    setOrder(order);
  }

  public void setFamilia(String familia) {
    setScientificName(Rank.Family, familia);
  }

  public void setSubfamilia(String familia) {
    setScientificName(Rank.Subfamily, familia);
  }

  public void setGenus(String genus) {
    setScientificName(Rank.Genus, genus);
  }

  public void setSubgenus(String subgenus) {
    setScientificName(Rank.Subgenus, subgenus);
  }

  public void setSpecies(String species) {
    setScientificName(Rank.Species, species);
  }

  public void setRegnum_authority(String regnum) {
    setScientificNameAuthorship(Rank.Kingdom, regnum);
  }


  public void setOrder_authority(String order) {
    setScientificNameAuthorship(Rank.Order, order);
  }

  public void setSubfamilia_authority(String familia) {
    setScientificNameAuthorship(Rank.Subfamily, familia);
  }

  public void setSynonyms_ref(String synonyms_ref) {
    this.synonymsRef = synonyms_ref;
  }

  public void setSynonyms(String value) {
    // synonyms = ''species1''<small>Authority1</small><br/> ''species2''<small>Authority2</small>
    // synonyms = *''species1''<br/><small>Authority1</small> *''species2''<br/><small>Authority2</small>
    value = value.replaceAll("< *small *>", " ");
    value = value.replaceAll("</ *small *>", "");
    String[] synonyms;
    if (value.contains("*")) {
      value = value.replaceAll("< *br */?>", " ");
      synonyms = StringUtils.split(value, "*");
    } else {
      synonyms = StringUtils.splitByWholeSeparator(value, "<br/>");
    }
    for (String syn : synonyms) {
      syn = StringUtils.trimToNull(syn.replaceAll(" +", " "));
      if (syn!=null){
        this.synonyms.add(syn);
      }
    }
  }

  public void setType_species(String type_species) {
    this.typeSpecies = type_species;
  }


  public void setType_species_authority(String type_species_authority) {
    this.typeSpeciesAuthority = type_species_authority;
  }


  public void setDiversity(String diversity) {
    this.diversity = diversity;
  }

  public void setDiversity_link(String diversityLink) {
    this.diversityLink = diversityLink;
  }

  protected Image rangeMap(int idx) {
    try {
      if (rangeMaps.get(idx) == null) {
        rangeMaps.set(idx, new Image());
      }
    } catch (IndexOutOfBoundsException e) {
      while (rangeMaps.size() <= idx) {
        rangeMaps.add(null);
      }
      rangeMaps.set(idx, new Image());
    }
    return rangeMaps.get(idx);
  }

  public void setRange_map(String range_map) {
    rangeMap(0).setImage(range_map);
  }

  public void setRange_map_alt(String range_map_alt) {
    rangeMap(0).setImageAlt(range_map_alt);
  }

  public void setRange_map_caption(String range_map_caption) {
    rangeMap(0).setImageCaption(range_map_caption);
  }

  public void setRange_map2(String range_map) {
    rangeMap(1).setImage(range_map);
  }

  public void setRange_map2_alt(String range_map_alt) {
    rangeMap(1).setImageAlt(range_map_alt);
  }

  public void setRange_map2_caption(String range_map_caption) {
    rangeMap(1).setImageCaption(range_map_caption);
  }

  public void setRange_map3(String range_map) {
    rangeMap(2).setImage(range_map);
  }

  public void setRange_map3_alt(String range_map_alt) {
    rangeMap(2).setImageAlt(range_map_alt);
  }

  public void setRange_map3_caption(String range_map_caption) {
    rangeMap(2).setImageCaption(range_map_caption);
  }

  public void setRange_map4(String range_map) {
    rangeMap(3).setImage(range_map);
  }

  public void setRange_map4_alt(String range_map_alt) {
    rangeMap(3).setImageAlt(range_map_alt);
  }

  public void setRange_map4_caption(String range_map_caption) {
    rangeMap(3).setImageCaption(range_map_caption);
  }


  public void setPhylum_authority(String phylum_authority) {
    //TODO: set authorship if lowest rank
  }

  public void setClassis_authority(String classis_authority) {
    //TODO: set authorship if lowest rank
  }

  public void setOrdo_authority(String ordo_authority) {
    //TODO: set authorship if lowest rank
  }

  public void setFamilia_authority(String familia_authority) {
    //TODO: set authorship if lowest rank
  }

  public void setGenus_authority(String genus_authority) {
    //TODO: set authorship if lowest rank
  }

  public void setSubgenus_authority(String subgenus_authority) {
    //TODO: set authorship if lowest rank
  }

  public void setSpecies_authority(String species_authority) {
    //TODO: set authorship if lowest rank
  }


  public void setBinomial2(String binomial2) {
    //TODO: do sth useful with this
    log.debug("Binomial2: {}", binomial2);
  }
  public void setBinomial_authority2(String binomial_authority2) {
    //TODO: do sth useful with this
    log.debug("Binomial2 authority: {}", binomial_authority2);
  }



  public void setType_genus(String type_genus) {
    this.typeGenus = type_genus;
  }

  public void setType_genus_authority(String typeGenusAuthority) {
    this.typeGenusAuthority = typeGenusAuthority;
  }

  public void setSuperdomain(String name) {
    setScientificName(Rank.Superdomain, name);
  }

  public void setDomain(String name) {
    setScientificName(Rank.Domain, name);
  }

  public void setSuperregnum(String name) {
    setScientificName(Rank.Superkingdom, name);
  }

  public void setSubregnum(String name) {
    setScientificName(Rank.Subkingdom, name);
  }

  public void setSuperdivisio(String name) {
    setScientificName(Rank.Superdivision, name);
  }

  public void setSuperphylum(String name) {
    setScientificName(Rank.Superphylum, name);
  }

  public void setDivisio(String name) {
    setScientificName(Rank.Divisio, name);
  }

  public void setSubdivisio(String name) {
    setScientificName(Rank.Subdivision, name);
  }

  public void setSubphylum(String name) {
    setScientificName(Rank.Subphylum, name);
  }

  public void setInfraphylum(String name) {
    setScientificName(Rank.Infraphylum, name);
  }

  public void setMicrophylum(String name) {
    setScientificName(Rank.Microphylum, name);
  }

  public void setNanophylum(String name) {
    setScientificName(Rank.Nanophylum, name);
  }

  public void setSuperclassis(String name) {
    setScientificName(Rank.Superclass, name);
  }

  public void setSubclassis(String name) {
    setScientificName(Rank.Subclass, name);
  }

  public void setInfraclassis(String name) {
    setScientificName(Rank.Infraclass, name);
  }

  public void setSupercohort(String name) {
    setScientificName(Rank.Supercohort, name);
  }

  public void setCohort(String name) {
    setScientificName(Rank.Cohort, name);
  }

  public void setSubcohort(String name) {
    setScientificName(Rank.Subcohort, name);
  }


  public void setMagnordo(String name) {
    setScientificName(Rank.Magnorder, name);
  }

  public void setSuperordo(String name) {
    setScientificName(Rank.Superorder, name);
  }

  public void setSubordo(String name) {
    setScientificName(Rank.Suborder, name);
  }

  public void setSuborder(String name) {
    setSubordo(name);
  }

  public void setInfraordo(String name) {
    setScientificName(Rank.Infraorder, name);
  }

  public void setParvordo(String name) {
    setScientificName(Rank.Parvorder, name);
  }

  public void setZoodivisio(String name) {
    setScientificName(Rank.Zoodivisio, name);
  }

  public void setZoosectio(String name) {
    setScientificName(Rank.Zoosectio, name);
  }

  public void setZoosubsectio(String name) {
    setScientificName(Rank.Zoosubsectio, name);
  }

  public void setSuperfamilia(String name) {
    setScientificName(Rank.Superfamily, name);
  }

  public void setSupertribus(String name) {
    setScientificName(Rank.Supertribe, name);
  }

  public void setTribus(String name) {
    setScientificName(Rank.Tribe, name);
  }

  public void setSubtribus(String name) {
    setScientificName(Rank.Subtribe, name);
  }

  public void setAlliance(String name) {
    setScientificName(Rank.Alliance, name);
  }

  public void setSectio(String name) {
    setScientificName(Rank.Section, name);
  }

  public void setSubsectio(String name) {
    setScientificName(Rank.Subsection, name);
  }

  public void setSeries(String name) {
    setScientificName(Rank.Series, name);
  }

  public void setSubseries(String name) {
    setScientificName(Rank.Subseries, name);
  }

  public void setSpecies_group(String name) {
    setScientificName(Rank.Species_Group, name);
  }

  public void setSpecies_subgroup(String name) {
    setScientificName(Rank.Species_Subgroup, name);
  }

  public void setSpecies_complex(String name) {
    setScientificName(Rank.Species_Complex, name);
  }

  public void setSubspecies(String name) {
    setScientificName(Rank.Subspecies, name);
  }

  public void setVariety(String name) {
    setScientificName(Rank.Variety, name);
  }

  public void setForm(String name) {
    setScientificName(Rank.Form, name);
  }

  public void setSuperdomain_authority(String author) {
    setScientificNameAuthorship(Rank.Superdomain, author);
  }

  public void setDomain_authority(String author) {
    setScientificNameAuthorship(Rank.Domain, author);
  }

  public void setSuperregnum_authority(String author) {
    setScientificNameAuthorship(Rank.Superkingdom, author);
  }

  public void setSubregnum_authority(String author) {
    setScientificNameAuthorship(Rank.Subkingdom, author);
  }

  public void setSuperdivisio_authority(String author) {
    setScientificNameAuthorship(Rank.Superdivision, author);
  }

  public void setSuperphylum_authority(String author) {
    setScientificNameAuthorship(Rank.Superphylum, author);
  }

  public void setDivisio_authority(String author) {
    setScientificNameAuthorship(Rank.Divisio, author);
  }

  public void setSubdivisio_authority(String author) {
    setScientificNameAuthorship(Rank.Subdivision, author);
  }

  public void setSubphylum_authority(String author) {
    setScientificNameAuthorship(Rank.Subphylum, author);
  }

  public void setInfraphylum_authority(String author) {
    setScientificNameAuthorship(Rank.Infraphylum, author);
  }

  public void setMicrophylum_authority(String author) {
    setScientificNameAuthorship(Rank.Microphylum, author);
  }

  public void setNanophylum_authority(String author) {
    setScientificNameAuthorship(Rank.Nanophylum, author);
  }

  public void setSuperclassis_authority(String author) {
    setScientificNameAuthorship(Rank.Superclass, author);
  }

  public void setSubclassis_authority(String author) {
    setScientificNameAuthorship(Rank.Subclass, author);
  }

  public void setInfraclassis_authority(String author) {
    setScientificNameAuthorship(Rank.Infraclass, author);
  }

  public void setSupercohort_authority(String author) {
    setScientificNameAuthorship(Rank.Supercohort, author);
  }

  public void setCohort_authority(String author) {
    setScientificNameAuthorship(Rank.Cohort, author);
  }

  public void setSubcohort_authority(String author) {
    setScientificNameAuthorship(Rank.Subcohort, author);
  }

  public void setMagnordo_authority(String author) {
    setScientificNameAuthorship(Rank.Magnorder, author);
  }

  public void setSuperordo_authority(String author) {
    setScientificNameAuthorship(Rank.Superorder, author);
  }

  public void setSubordo_authority(String author) {
    setScientificNameAuthorship(Rank.Suborder, author);
  }

  public void setSuborder_authority(String author) {
    setSubordo_authority(author);
  }

  public void setInfraordo_authority(String author) {
    setScientificNameAuthorship(Rank.Infraorder, author);
  }

  public void setParvordo_authority(String author) {
    setScientificNameAuthorship(Rank.Parvorder, author);
  }

  public void setZoodivisio_authority(String author) {
    setScientificNameAuthorship(Rank.Zoodivisio, author);
  }

  public void setZoosectio_authority(String author) {
    setScientificNameAuthorship(Rank.Zoosectio, author);
  }

  public void setZoosubsectio_authority(String author) {
    setScientificNameAuthorship(Rank.Zoosubsectio, author);
  }

  public void setSuperfamilia_authority(String author) {
    setScientificNameAuthorship(Rank.Superfamily, author);
  }

  public void setSupertribus_authority(String author) {
    setScientificNameAuthorship(Rank.Supertribe, author);
  }

  public void setTribus_authority(String author) {
    setScientificNameAuthorship(Rank.Tribe, author);
  }

  public void setSubtribus_authority(String author) {
    setScientificNameAuthorship(Rank.Subtribe, author);
  }

  public void setAlliance_authority(String author) {
    setScientificNameAuthorship(Rank.Alliance, author);
  }

  public void setSectio_authority(String author) {
    setScientificNameAuthorship(Rank.Section, author);
  }

  public void setSubsectio_authority(String author) {
    setScientificNameAuthorship(Rank.Subsection, author);
  }

  public void setSeries_authority(String author) {
    setScientificNameAuthorship(Rank.Series, author);
  }

  public void setSubseries_authority(String author) {
    setScientificNameAuthorship(Rank.Subseries, author);
  }

  public void setSpecies_group_authority(String author) {
    setScientificNameAuthorship(Rank.Species_Group, author);
  }

  public void setSpecies_subgroup_authority(String author) {
    setScientificNameAuthorship(Rank.Species_Subgroup, author);
  }

  public void setSpecies_complex_authority(String author) {
    setScientificNameAuthorship(Rank.Species_Complex, author);
  }

  public void setSubspecies_authority(String author) {
    setScientificNameAuthorship(Rank.Subspecies, author);
  }

  public void setVariety_authority(String author) {
    setScientificNameAuthorship(Rank.Variety, author);
  }

  public void setForm_authority(String author) {
    setScientificNameAuthorship(Rank.Form, author);
  }
}
