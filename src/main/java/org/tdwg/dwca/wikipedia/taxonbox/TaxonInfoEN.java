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


import org.gbif.api.vocabulary.Kingdom;
import org.gbif.api.vocabulary.Language;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/**
 * Support for various english wikipedia taxobox formats.
 */
public class TaxonInfoEN extends TaxonInfoBase{
  private static final Language WIKI_LANG = Language.ENGLISH;
  private static final Map<Kingdom, String> KINGDOM_PAGES = ImmutableMap.<Kingdom, String>builder()
    .put(Kingdom.ANIMALIA, "Animal")
    .put(Kingdom.ARCHAEA, "Archaea")
    .put(Kingdom.BACTERIA, "Bacteria")
    .put(Kingdom.CHROMISTA, "Chromista")
    .put(Kingdom.FUNGI, "Fungus")
    .put(Kingdom.PLANTAE, "Plant")
    .put(Kingdom.PROTOZOA, "Protozoa")
    .put(Kingdom.VIRUSES, "Virus")
    .build();
  public static final Set<String> IGNORE_SETIONS = Sets.newHashSet("see also", "references", "further reading", "external links", "reflist");

  @Override
  protected String knownPageTitle(Kingdom kingdom, Language lang) {
    if (WIKI_LANG == lang) {
      return KINGDOM_PAGES.get(kingdom);
    }
    return null;
  }

  public void setBinomial(String binomial) {
    setScientificNameAndRankIfLowerOrEqual(Rank.Species, binomial);
  }

  /**
   * common typo
   */
  public void setBinominal(String binomial) {
    setBinomial(binomial);
  }

  public void setTaxon(String taxon) {
    if (hasScientificName() ){
      log.warn("existing name {} replaced by {}", getScientificName(), taxon);
    }
    setScientificName(taxon);
  }

  public void setAuthority(String authority) {
    setScientificNameAuthorship(authority);
  }

  public void setLatin_name(String latinName) {
    setTaxon(latinName);
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
    setScientificNameAndRankIfLowest(Rank.Infraspecies, trinomial);
  }

  public void setTrinomial_authority(String trinomial_authority) {
    setScientificNameAuthorship(Rank.Infraspecies, trinomial_authority);
  }


  public void setName(String name) {
    addVernacularNameInDefaultLang(name);
  }

  public void setStatus_system(String status_system) {
    setStatusSystem(status_system);
  }

  public void setFossil_range(String fossil_range) {
    setFossilRange(fossil_range);
  }

  public void setImage(String url) {
    image(0).setUrl(url);
  }

  public void setImage_alt(String image_alt) {
    image(0).setImageAlt(image_alt);
  }

  public void setImage_caption(String image_caption) {
    image(0).setTitle(image_caption);
  }

  public void setImage2(String url) {
    image(1).setUrl(url);
  }

  public void setImage2_alt(String image_alt) {
    image(1).setImageAlt(image_alt);
  }

  public void setImage2_caption(String image_caption) {
    image(1).setTitle(image_caption);
  }

  public void setRegnum(String regnum) {
    setKingdom(regnum);
  }

  public void setClassis(String classis) {
    setClazz(classis);
  }

  public void setOrdo(String order) {
    setOrder(order);
  }

  public void setFamilia(String familia) {
    setFamily(familia);
  }

  public void setSubfamilia(String familia) {
    setScientificNameAndRankIfLowest(Rank.Subfamily, familia);
  }

  public void setRegnum_authority(String authorship) {
    setScientificNameAuthorship(Rank.Kingdom, authorship);
  }

  public void setOrder_authority(String authorship) {
    setScientificNameAuthorship(Rank.Order, authorship);
  }

  public void setSubfamilia_authority(String authorship) {
    setScientificNameAuthorship(Rank.Subfamily, authorship);
  }

  public void setSynonyms_ref(String synonyms_ref) {
    setSynonymsRef(synonyms_ref);
  }

  public void setType_species(String type_species) {
    setTypeSpecies(type_species);
  }

  public void setType_species_authority(String type_species_authority) {
    setTypeSpeciesAuthority(type_species_authority);
  }

  public void setDiversity_link(String diversityLink) {
    setDiversityLink(diversityLink);
  }

  public void setRange_map(String range_map) {
    rangeMap(0).setUrl(range_map);
  }

  public void setRange_map_alt(String range_map_alt) {
    rangeMap(0).setImageAlt(range_map_alt);
  }

  public void setRange_map_caption(String range_map_caption) {
    rangeMap(0).setTitle(range_map_caption);
  }

  public void setRange_map2(String range_map) {
    rangeMap(1).setUrl(range_map);
  }

  public void setRange_map2_alt(String range_map_alt) {
    rangeMap(1).setImageAlt(range_map_alt);
  }

  public void setRange_map2_caption(String range_map_caption) {
    rangeMap(1).setTitle(range_map_caption);
  }

  public void setRange_map3(String range_map) {
    rangeMap(2).setUrl(range_map);
  }

  public void setRange_map3_alt(String range_map_alt) {
    rangeMap(2).setImageAlt(range_map_alt);
  }

  public void setRange_map3_caption(String range_map_caption) {
    rangeMap(2).setTitle(range_map_caption);
  }

  public void setRange_map4(String range_map) {
    rangeMap(3).setUrl(range_map);
  }

  public void setRange_map4_alt(String range_map_alt) {
    rangeMap(3).setImageAlt(range_map_alt);
  }

  public void setRange_map4_caption(String range_map_caption) {
    rangeMap(3).setTitle(range_map_caption);
  }


  public void setPhylum_authority(String authority) {
    setScientificNameAuthorship(Rank.Phylum, authority);
  }

  public void setClassis_authority(String authority) {
    setScientificNameAuthorship(Rank.Class, authority);
  }

  public void setOrdo_authority(String authority) {
    setScientificNameAuthorship(Rank.Order, authority);
  }

  public void setFamilia_authority(String authority) {
    setScientificNameAuthorship(Rank.Family, authority);
  }

  public void setGenus_authority(String authority) {
    setScientificNameAuthorship(Rank.Genus, authority);
  }

  public void setSubgenus_authority(String authority) {
    setScientificNameAuthorship(Rank.Subgenus, authority);
  }

  public void setSpecies_authority(String authority) {
    setScientificNameAuthorship(Rank.Species, authority);
  }


  public void setBinomial2(String binomial2) {
    //TODO: create second taxon with the same infos but different species name and authority
    log.debug("Binomial2 found: {} with current name {}", binomial2, getScientificName());
  }
  public void setBinomial2_authority(String binomial_authority2) {
    //TODO: do sth useful with this
  }

  public void setBinomial_authority2(String binomial_authority2) {
    setBinomial2_authority(binomial_authority2);
  }

  public void setType_genus(String type_genus) {
    setTypeGenus(type_genus);
  }

  public void setType_genus_authority(String typeGenusAuthority) {
    setTypeGenusAuthority(typeGenusAuthority);
  }

  public void setSuperdomain(String name) {
    setScientificNameAndRankIfLowest(Rank.Superdomain, name);
  }

  public void setDomain(String name) {
    setScientificNameAndRankIfLowest(Rank.Domain, name);
  }

  public void setSuperregnum(String name) {
    setScientificNameAndRankIfLowest(Rank.Superkingdom, name);
  }

  public void setSubregnum(String name) {
    setScientificNameAndRankIfLowest(Rank.Subkingdom, name);
  }

  public void setSuperdivisio(String name) {
    setScientificNameAndRankIfLowest(Rank.Superdivision, name);
  }

  public void setSuperphylum(String name) {
    setScientificNameAndRankIfLowest(Rank.Superphylum, name);
  }

  public void setDivisio(String name) {
    setScientificNameAndRankIfLowest(Rank.Divisio, name);
  }

  public void setSubdivisio(String name) {
    setScientificNameAndRankIfLowest(Rank.Subdivision, name);
  }

  public void setSubphylum(String name) {
    setScientificNameAndRankIfLowest(Rank.Subphylum, name);
  }

  public void setInfraphylum(String name) {
    setScientificNameAndRankIfLowest(Rank.Infraphylum, name);
  }

  public void setMicrophylum(String name) {
    setScientificNameAndRankIfLowest(Rank.Microphylum, name);
  }

  public void setNanophylum(String name) {
    setScientificNameAndRankIfLowest(Rank.Nanophylum, name);
  }

  public void setSuperclassis(String name) {
    setScientificNameAndRankIfLowest(Rank.Superclass, name);
  }

  public void setSubclassis(String name) {
    setScientificNameAndRankIfLowest(Rank.Subclass, name);
  }

  public void setInfraclassis(String name) {
    setScientificNameAndRankIfLowest(Rank.Infraclass, name);
  }

  public void setSupercohort(String name) {
    setScientificNameAndRankIfLowest(Rank.Supercohort, name);
  }

  public void setCohort(String name) {
    setScientificNameAndRankIfLowest(Rank.Cohort, name);
  }

  public void setSubcohort(String name) {
    setScientificNameAndRankIfLowest(Rank.Subcohort, name);
  }


  public void setMagnordo(String name) {
    setScientificNameAndRankIfLowest(Rank.Magnorder, name);
  }

  public void setSuperordo(String name) {
    setScientificNameAndRankIfLowest(Rank.Superorder, name);
  }

  public void setSubordo(String name) {
    setScientificNameAndRankIfLowest(Rank.Suborder, name);
  }

  public void setSuborder(String name) {
    setSubordo(name);
  }

  public void setInfraordo(String name) {
    setScientificNameAndRankIfLowest(Rank.Infraorder, name);
  }

  public void setParvordo(String name) {
    setScientificNameAndRankIfLowest(Rank.Parvorder, name);
  }

  public void setZoodivisio(String name) {
    setScientificNameAndRankIfLowest(Rank.Zoodivisio, name);
  }

  public void setZoosectio(String name) {
    setScientificNameAndRankIfLowest(Rank.Zoosectio, name);
  }

  public void setZoosubsectio(String name) {
    setScientificNameAndRankIfLowest(Rank.Zoosubsectio, name);
  }

  public void setSuperfamilia(String name) {
    setScientificNameAndRankIfLowest(Rank.Superfamily, name);
  }

  public void setSupertribus(String name) {
    setScientificNameAndRankIfLowest(Rank.Supertribe, name);
  }

  public void setTribus(String name) {
    setScientificNameAndRankIfLowest(Rank.Tribe, name);
  }

  public void setSubtribus(String name) {
    setScientificNameAndRankIfLowest(Rank.Subtribe, name);
  }

  public void setAlliance(String name) {
    setScientificNameAndRankIfLowest(Rank.Alliance, name);
  }

  public void setSectio(String name) {
    setScientificNameAndRankIfLowest(Rank.Section, name);
  }

  public void setSubsectio(String name) {
    setScientificNameAndRankIfLowest(Rank.Subsection, name);
  }

  public void setSeries(String name) {
    setScientificNameAndRankIfLowest(Rank.Series, name);
  }

  public void setSubseries(String name) {
    setScientificNameAndRankIfLowest(Rank.Subseries, name);
  }

  public void setSpecies_group(String name) {
    setScientificNameAndRankIfLowest(Rank.SpeciesGroup, name);
  }

  public void setSpecies_subgroup(String name) {
    setScientificNameAndRankIfLowest(Rank.SpeciesSubgroup, name);
  }

  public void setSpecies_complex(String name) {
    setScientificNameAndRankIfLowest(Rank.SpeciesComplex, name);
  }

  public void setSubspecies(String name) {
    setScientificNameAndRankIfLowest(Rank.Subspecies, name);
  }

  public void setVariety(String name) {
    setScientificNameAndRankIfLowest(Rank.Variety, name);
  }

  public void setForm(String name) {
    setScientificNameAndRankIfLowest(Rank.Form, name);
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
    setScientificNameAuthorship(Rank.SpeciesGroup, author);
  }

  public void setSpecies_subgroup_authority(String author) {
    setScientificNameAuthorship(Rank.SpeciesSubgroup, author);
  }

  public void setSpecies_complex_authority(String author) {
    setScientificNameAuthorship(Rank.SpeciesComplex, author);
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

  public void setUnranked_regnum(String kingdom) {
    setScientificNameAndRankIfLowest(Rank.KingdomClade, kingdom);
  }

  public void setUnranked_kingdom(String kingdom) {
    setUnranked_regnum(kingdom);
  }

  public void setUnranked_phylum(String phylum) {
    setScientificNameAndRankIfLowest(Rank.PhylumClade, phylum);
  }

  public void setUnranked_classis(String classis) {
    setScientificNameAndRankIfLowest(Rank.ClassClade, classis);
  }

  public void setUnranked_class(String classis) {
    setUnranked_classis(classis);
  }

  public void setUnranked_ordo(String order) {
    setScientificNameAndRankIfLowest(Rank.OrderClade, order);
  }

  public void setUnranked_order(String order) {
    setUnranked_ordo(order);
  }

  public void setUnranked_familia(String family) {
    setScientificNameAndRankIfLowest(Rank.FamilyClade, family);
  }

  public void setUnranked_family(String family) {
    setUnranked_familia(family);
  }

  public void setUnranked_genus(String genus) {
    setScientificNameAndRankIfLowest(Rank.GenusClade, genus);
  }
}
