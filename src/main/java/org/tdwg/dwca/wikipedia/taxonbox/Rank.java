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

import org.apache.commons.lang3.StringUtils;

/**
 * @See http://en.wikipedia.org/wiki/Template:Taxobox#Complete_blank_template
 * @See http://de.wikipedia.org/wiki/Wikipedia:Taxoboxen
 */
public enum Rank {
  Group,
  Superdomain,
  Domain,
  Superkingdom,
  KingdomClade,
  Kingdom("reich", "regnum"),
  Subkingdom,
  Superdivision,
  Superphylum,
  Divisio("division"),
  PhylumClade,
  Phylum("stamm"),
  Subdivision,
  Subphylum,
  Infraphylum,
  Microphylum,
  Nanophylum,
  Superclass,
  ClassClade,
  Class("class", "classis", "klasse"),
  Subclass,
  Infraclass,
  Supercohort("supercohors"),
  Cohort("cohors"),
  Subcohort("subcohors"),
  Magnorder,
  Superorder,
  OrderClade,
  Order("ordo", "ordnung"),
  Suborder,
  Infraorder,
  Parvorder,
  Zoodivisio("zoodivision"),
  Zoosectio("zoosection"),
  Zoosubsectio("zoosubsection"),
  Superfamily,
  FamilyClade,
  Family("familia", "familie"),
  Subfamily("subfamilia", "unterfamilie"),
  Supertribe,
  Tribe("tribus"),
  Subtribe("subtribus"),
  Alliance,
  GenusClade,
  Genus("gattung"),
  Subgenus("untergattung"),
  Section("sectio"),
  Subsection("subsectio"),
  Series,
  Subseries,
  Species_Group,
  Species_Subgroup,
  Species_Complex,
  Species("art"),
  Infraspecies,
  Subspecies("unterart"),
  Variety("varietät","varietas"),
  Form("forma"),
  Uninterpretable;

  private final String[] alts;

  Rank(String ... alternatives) {
    this.alts = alternatives;
  }

  public static Rank fromString(String rank){
    if (StringUtils.isBlank(rank)){
      return null;
    }
    rank=rank.toLowerCase().trim();
    for (Rank r : Rank.values()){
      if (r.name().equalsIgnoreCase(rank)){
        return r;
      }
      for (String alt : r.alts) {
        if (alt.equalsIgnoreCase(rank)) {
          return r;
        }
      }
    }
    return Rank.Uninterpretable;
  }

  public boolean isLowerThan(Rank rank) {
    if (rank == null) return true;
    return this.ordinal() > rank.ordinal();
  }

  public boolean isHigherThan(Rank rank) {
    if (rank == null) return true;
    return this.ordinal() < rank.ordinal();
  }
}
