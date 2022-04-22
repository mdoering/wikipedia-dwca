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

public class Image implements Media{
  private String url;
  private String title;
  private String imageAlt;
  // further metadata
  private String author;
  private String publisher;
  private String license;
  private String source;
  private String date;
  private String description;


  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public void setUrl(String url) {
    this.url = url;
  }

  public String getImageAlt() {
    return imageAlt;
  }

  public void setImageAlt(String imageAlt) {
    this.imageAlt = imageAlt;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String imageCaption) {
    this.title = imageCaption;
  }

  @Override
  public String getAuthor() {
    return author;
  }

  @Override
  public void setAuthor(String author) {
    this.author = author;
  }

  @Override
  public String getLicense() {
    return license;
  }

  @Override
  public void setLicense(String license) {
    this.license = license;
  }

  @Override
  public String getSource() {
    return source;
  }

  @Override
  public void setSource(String source) {
    this.source = source;
  }

  @Override
  public String getDate() {
    return date;
  }

  @Override
  public void setDate(String date) {
    this.date = date;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getPublisher() {
    return publisher;
  }

  @Override
  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  @Override
  public String toString() {
    return "Image{" +
           "description='" + description + '\'' +
           ", date='" + date + '\'' +
           ", source='" + source + '\'' +
           ", license='" + license + '\'' +
           ", publisher='" + publisher + '\'' +
           ", author='" + author + '\'' +
           ", title='" + title + '\'' +
           ", imageAlt='" + imageAlt + '\'' +
           ", url='" + url + '\'' +
           '}';
  }
}
