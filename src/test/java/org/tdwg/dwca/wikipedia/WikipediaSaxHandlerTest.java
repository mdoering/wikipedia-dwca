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

import org.gbif.utils.file.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WikipediaSaxHandlerTest {

  public void testParser() throws IOException {
    String wikiText = inputStreamToString(FileUtils.classpathStream("puma-en.xml"));
    // WikipediaSaxHandler handler = new WikipediaSaxHandler();
  }

  private String inputStreamToString(InputStream in) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
    StringBuilder stringBuilder = new StringBuilder();
    String line = null;

    while ((line = bufferedReader.readLine()) != null) {
      stringBuilder.append(line + "\n");
    }

    bufferedReader.close();
    return stringBuilder.toString();
  }
}
