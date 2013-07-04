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

public class Image {
  private String url;
  private String imageAlt;
  private String imageCaption;
  // further metadata
  private String author;
  private String publisher;
  private String license;
  private String source;
  private String date;
  private String description;


  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getImageAlt() {
    return imageAlt;
  }

  public void setImageAlt(String imageAlt) {
    this.imageAlt = imageAlt;
  }

  public String getImageCaption() {
    return imageCaption;
  }

  public void setImageCaption(String imageCaption) {
    this.imageCaption = imageCaption;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getLicense() {
    return license;
  }

  public void setLicense(String license) {
    this.license = license;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPublisher() {
    return publisher;
  }

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
           ", imageCaption='" + imageCaption + '\'' +
           ", imageAlt='" + imageAlt + '\'' +
           ", url='" + url + '\'' +
           '}';
  }
}
