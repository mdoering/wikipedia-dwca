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
import java.util.List;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TaxonTemplateParserTest {
  @Test
  public void testFixParams(){
    TaxonTemplateParser p = new TaxonTemplateParser();
    List<String> params = new ArrayList<String>();
    params.add("author=Linné");
    params.add("genus=''Abies''");
    params.add("species=[[Abies olga|A.olga]]");
    params.add("species=[[Abies alba");
    params.add("A.alba]]");

    List<String> params2 = p.fixTemplateParams(params);
    assertEquals(params.size()-1, params2.size());
    assertEquals("species=[[Abies alba|A.alba]]", params2.get(3));
  }

  @Test
  public void testCleanValues() {
    assertEquals("Abies", TaxonTemplateParser.cleanValue("''Abies''"));
    assertEquals("Abies alba|A.alba", TaxonTemplateParser.cleanValue("[[Abies alba|A.alba"));
    assertEquals("A.olga", TaxonTemplateParser.cleanValue("[[Abies olga|A.olga]]"));
    assertEquals("A.olga", TaxonTemplateParser.cleanValue("[[Abies olga|A.olga]]"));
    assertEquals("Abies olga", TaxonTemplateParser.cleanValue("Abies&nbsp;olga"));
    assertEquals("Abies olga negra", TaxonTemplateParser.cleanValue("[[Abies olga <br/> negra"));

  }

}
