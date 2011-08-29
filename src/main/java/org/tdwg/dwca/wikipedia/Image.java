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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.DigestException;
import java.security.MessageDigest;

import sun.security.provider.MD5;

public class Image {
  private String image;
  private String imageAlt;
  private String imageCaption;

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
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

}
