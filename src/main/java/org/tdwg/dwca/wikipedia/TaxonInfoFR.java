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
 * The french taxobox concept is very different, using a separate template for each property!
 * It contains much more information though, including ecological parameters
 *
 * A french taxobox starts with {{Taxobox début ...}} and ends with {{Taxobox fin}}
 *
 * TODO: implement the french taxobox concept
 * @See http://fr.wikipedia.org/wiki/Catégorie:Modèle_taxobox
 */
abstract class TaxonInfoFR extends TaxonInfoDE {
  public static final String TAXOBOX_NAME="Taxobox";

}
