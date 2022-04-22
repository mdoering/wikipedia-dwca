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
 * The spanish taxobox is exactly the same as the english one, so we do not need any new setters!
 * The only difference is that the template is called "Ficha de taxón"
 * @See http://es.wikipedia.org/wiki/Plantilla:Ficha_de_taxón
 *
 * There also exists a multi-template approach similar to the french taxobox:
 * @See http://es.wikipedia.org/wiki/Wikiproyecto:Taxonom%C3%ADa/Cómo_construir_un_taxobox_(multiplantilla)
 *
 * this is not yet supported
 *
 */
public class TaxonInfoES extends TaxonInfoFR {
  private static final Language WIKI_LANG = Language.SPANISH;
  private static final Map<Kingdom, String> KINGDOM_PAGES = ImmutableMap.<Kingdom, String>builder()
    .put(Kingdom.ANIMALIA, "Animalia")
    .put(Kingdom.ARCHAEA, "Archaea")
    .put(Kingdom.BACTERIA, "Bacteria")
    .put(Kingdom.CHROMISTA, "Chromista")
    .put(Kingdom.FUNGI, "Fungus")
    .put(Kingdom.PLANTAE, "Plantae")
    .put(Kingdom.PROTOZOA, "Protozoo")
    .put(Kingdom.VIRUSES, "Virus")
    .build();
  public static final Set<String> IGNORE_SETIONS = Sets.newHashSet();

  @Override
  protected String knownPageTitle(Kingdom kingdom, Language lang) {
    if (WIKI_LANG == lang) {
      return KINGDOM_PAGES.get(kingdom);
    }
    return super.knownPageTitle(kingdom, lang);
  }

  public void setTrinominal(String trinomial) {
    super.setTrinomial(trinomial);
  }

  public void setTrinominal_authority(String trinomial_authority) {
    super.setTrinomial_authority(trinomial_authority);
  }

  public void setAlianza(String name) {
    super.setAlliance(name);
  }

  public void setEspecie(String species) {
    super.setSpecies(species);
  }

  public void setEspecies(String species) {
    super.setSpecies(species);
  }

  public void setDominio(String name) {
    super.setDomain(name);
  }

  public void setOrdo_entry(String order) {
    super.setOrdo(order);
  }

  public void setOrdre(String order) {
    super.setOrdo(order);
  }

  public void setNome(String name) {
    super.setName(name);
  }

  public void setImagen_caption(String image_caption) {
    super.setImage_caption(image_caption);
  }

  public void setClassis_athority(String classis_authority) {
    super.setClassis_authority(classis_authority);
  }

}
