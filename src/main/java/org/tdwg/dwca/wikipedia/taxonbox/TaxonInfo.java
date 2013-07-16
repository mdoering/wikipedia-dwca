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

import java.util.Iterator;
import java.util.List;

/**
 * Class that represents all data from a wikipedia taxobox template regardless of the wikipedia language used.
 * The superclass hierarchy is used to separate the different setters and functionality needed to support various wikipedia languages in the same class but to keep them clearly separate in the code.
 * Unfortunately the taxon templates used in different languages are substantially different in their format.
 */
public class TaxonInfo extends TaxonInfoES{

  @Override
  public List<String> getSynonyms() {
    return nullFreeList(super.getSynonyms());
  }

  @Override
  public List<Image> getRangeMaps() {
    return nullFreeList(super.getRangeMaps());
  }

  @Override
  public List<Image> getImages() {
    return nullFreeList(super.getImages());
  }

  public List<Sound> getSounds() {
    return nullFreeList(super.getSounds());
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

}
