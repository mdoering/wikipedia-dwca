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
abstract class TaxonInfoES extends TaxonInfoFR {
  public static final String TAXOBOX_NAME="Ficha_de_taxón";

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
