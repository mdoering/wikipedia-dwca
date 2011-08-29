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

public class Name {
  private String scientific;
  private String author;
  private String vernacular;
  private Rank rank;

  public String getScientific() {
    return scientific;
  }

  public void setScientific(String scientific) {
    this.scientific = scientific;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getVernacular() {
    return vernacular;
  }

  public void setVernacular(String vernacular) {
    this.vernacular = vernacular;
  }

  public Rank getRank() {
    return rank;
  }

  public void setRank(Rank rank) {
    this.rank = rank;
  }

  public void setRank(String rank) {
    this.rank = Rank.fromString(rank);
  }
}
