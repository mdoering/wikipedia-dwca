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

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class WikipediaUtilsTest {

  @Test
  public void testWikipediaImagePath() throws Exception {
    assertEquals("5/53/Typha_latifolia_norway.jpg", WikipediaUtils.wikipediaImagePath("Typha_latifolia_norway.jpg"));
  }
}
