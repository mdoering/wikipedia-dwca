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

import org.gbif.api.model.vocabulary.Kingdom;
import org.gbif.api.model.vocabulary.Language;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * The french taxobox concept is very different, using a separate template for each property!
 * It contains much more information though, including ecological parameters
 *
 * A french taxobox starts with {{Taxobox début ...}} and ends with {{Taxobox fin}}
 *
 * TODO: implement the french taxobox concept
 * @See http://fr.wikipedia.org/wiki/Catégorie:Modèle_taxobox
 */
abstract class TaxonInfoFR extends TaxonInfoDE {
  private static final Language WIKI_LANG = Language.FRENCH;
  private static final Map<Kingdom, String> KINGDOM_PAGES = ImmutableMap.<Kingdom, String>builder()
    .put(Kingdom.ANIMALIA, "Animal")
    .put(Kingdom.ARCHAEA, "Archaea")
    .put(Kingdom.BACTERIA, "Bacteria")
    .put(Kingdom.CHROMISTA, "Chromista")
    .put(Kingdom.FUNGI, "Fungus")
    .put(Kingdom.PLANTAE, "Plante")
    .put(Kingdom.PROTOZOA, "Protozoaire")
    .put(Kingdom.VIRUSES, "Virus")
    .build();

  @Override
  protected String knownPageTitle(Kingdom kingdom, Language lang) {
    if (WIKI_LANG == lang) {
      return KINGDOM_PAGES.get(kingdom);
    }
    return super.knownPageTitle(kingdom, lang);
  }

}
