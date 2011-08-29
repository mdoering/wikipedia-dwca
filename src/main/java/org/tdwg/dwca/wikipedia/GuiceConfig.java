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

import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.FileUtils;

import java.io.IOException;
import java.util.Properties;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.apache.http.impl.client.DefaultHttpClient;

public class GuiceConfig implements Module{

  @Override
  public void configure(Binder binder) {
    binder.bind(WikipediaParser.class).in(Scopes.NO_SCOPE);
  }

  @Provides
  @Singleton
  public DefaultHttpClient provideHttpClient(){
    return HttpUtil.newMultithreadedClient();
  }

  @Provides
  @Singleton
  @Inject
  public HttpUtil provideHttpUtil(DefaultHttpClient client) {
    return new HttpUtil(client);
  }

}
