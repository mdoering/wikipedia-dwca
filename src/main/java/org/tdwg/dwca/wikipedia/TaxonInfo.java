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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Class that represents all data from a wikipedia taxobox template regardless of the wikipedia language used.
 * The superclass hierarchy is used to separate the different setters and functionality needed to support various wikipedia languages in the same class but to keep them clearly separate in the code.
 * Unfortunately the taxon templates used in different languages are substantially different in their format.
 *
 * @See http://en.wikipedia.org/wiki/Template:Taxobox
 * @See http://de.wikipedia.org/wiki/Wikipedia:Taxoboxen
 */
public class TaxonInfo extends TaxonInfoES{

  public String getStatusSystem() {
    return statusSystem;
  }

  public RefInfo getStatusRef() {
    return statusRef;
  }

  public String getFossilRange() {
    StringBuilder sb = new StringBuilder();
    if (!StringUtils.isBlank(fossilRange)){
      sb.append(fossilRange);
    }
    if (!StringUtils.isBlank(fossilRangeFrom) || !StringUtils.isBlank(fossilRangeTo)) {
      sb.append(" ");
      sb.append(StringUtils.trimToEmpty(fossilRangeFrom));

      sb.append(" - ");
      sb.append(StringUtils.trimToEmpty(fossilRangeTo));
    }
    if (fossilRangeFromMio!=null || fossilRangeToMio!=null) {
      sb.append(" ");
      sb.append(mioToString(fossilRangeFromMio));
      sb.append(" - ");
      sb.append(mioToString(fossilRangeToMio));
    }
    return StringUtils.trimToNull(sb.toString());
  }

  private String mioToString(Double mio){
    if (mio==null){
      return "";
    }
    return mio.toString();
  }

  public String getClassificationStatus() {
    return classificationStatus;
  }

  public List<Name> getClassification() {
    return nullFreeList(classification);
  }

  /**
   * dwc compatible string for the entire higher classification given
   * @return
   */
  public String getHigherClassification() {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for(Name n : getClassification()){
      if (!first){
        sb.append(";");
      }
      if (n.getScientific() != null){
        sb.append(n.getScientific());
        first = false;
      }else if (n.getVernacular()!=null){
        sb.append(n.getVernacular());
        first = false;
      }
    }
    return sb.toString();
  }

  public String scientificNameByRank(Rank rank) {
    Name n = nameByRank(rank);
    if (n != null) {
      return n.getScientific();
    }
    return null;
  }
  public Name nameByRank(Rank rank) {
    if (rank==null){
      return null;
    }
    for (Name n : classification){
      if (n != null && n.getRank()==rank) {
        return n;
      }
    }
    return null;
  }

  public String getSynonymsRef() {
    return synonymsRef;
  }

  public String getTypeSpecies() {
    return typeSpecies;
  }

  public String getTypeSpeciesAuthority() {
    return typeSpeciesAuthority;
  }

  public String getTypeGenus() {
    return typeGenus;
  }

  public String getScientificName() {
    return scientificName;
  }

  public String getScientificNameAuthorship() {
    return scientificNameAuthorship;
  }

  public String getName() {
    return name;
  }

  public String getStatus() {
    return status;
  }

  public String getExtinct() {
    return extinct;
  }

  public String getSubgenus() {
    return scientificNameByRank(Rank.Subgenus);
  }

  public String getDiversity() {
    return diversity;
  }

  public String getDiversityLink() {
    return diversityLink;
  }

  public String getSpecies() {
    return scientificNameByRank(Rank.Species);
  }

  public List<String> getSynonyms() {
    return synonyms;
  }

  public String getOrder() {
    return scientificNameByRank(Rank.Order);
  }

  public String getFamily() {
    return scientificNameByRank(Rank.Family);
  }

  public String getGenus() {
    return scientificNameByRank(Rank.Genus);
  }

  public String getKingdom() {
    return scientificNameByRank(Rank.Kingdom);
  }

  public String getPhylum() {
    return scientificNameByRank(Rank.Phylum);
  }

  public String getClassis() {
    return scientificNameByRank(Rank.Class);
  }

  public List<Image> getRangeMaps() {
    return nullFreeList(rangeMaps);
  }

  public List<Image> getImages() {
    return nullFreeList(images);
  }

  private List nullFreeList(List<?> list){
    Iterator iter = list.iterator();
    while(iter.hasNext()){
      Object obj = iter.next();
      if (obj==null){
        iter.remove();
      }
    }
    return list;
  }
  public Rank getRank() {
    return rank;
  }

  public String getRankVerbatim() {
    return rankVerbatim;
  }

  public String getTrend() {
    return trend;
  }

}
