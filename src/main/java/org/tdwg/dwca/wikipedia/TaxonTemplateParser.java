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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Template;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiTemplateParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.ResolvedTemplate;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the TemplateParser for the Taxobox species information.
 *
 *  for English wikipedia:
 * @See http://en.wikipedia.org/wiki/Template:Taxobox
 *
 *  for German  wikipedia:
 * @See http://de.wikipedia.org/wiki/Wikipedia:Taxoboxen
 * @See http://de.wikipedia.org/wiki/Wikipedia:Paläoboxen
 *
 * for Spanish:
 * @See http://es.wikipedia.org/wiki/Plantilla:Ficha_de_taxón
 *
 */
public class TaxonTemplateParser implements MediaWikiTemplateParser {

  private static final Pattern openLinkPattern = Pattern.compile("\\[\\[[^\\]]+$");
  private static final Pattern closeLinkPattern = Pattern.compile("^[^\\[]+\\]\\]");
  private static final Pattern replLink = Pattern.compile("\\[\\[(?:[^|\\]]+\\|)?([^\\]]+)\\]\\]");
  private static final Pattern replLinkAll = Pattern.compile("(\\[\\[|\\]\\])");

  private Logger log = LoggerFactory.getLogger(getClass());

  private final String citeTemplateName = "cite";
  private final String fossilRangeTemplateName = "fossil range";
  private Set<String> unknownProperties = new HashSet<String>();
  private Set<String> unknownTemplates = new HashSet<String>();
  private TaxonInfo lastTaxon;

  public TaxonTemplateParser(){
	}

  public TaxonInfo getLastTaxon() {
    return lastTaxon;
  }

  public String configurationInfo(){
		return "Standard Template treatment: parse biota infobox and delete all others";
	}

  public static String cleanValue(String val){
    if (val==null) return null;
    val = val.replaceAll("''+", "");
    val = val.replaceAll("[†]", "");
    // keep only shown link values
    val = replLink.matcher(val).replaceAll("$1");
    // remove all remaining link brackets
    val = replLinkAll.matcher(val).replaceAll("");
    //val = val.replaceAll("</? *ref( +name=[^>]+)? *>", "");
    // remove all xml tags
    val = val.replaceAll("<[^>]+>", " ");
    return StringUtils.trimToNull(val.replaceAll(" +|&nbsp;", " "));
  }
  protected List<String> fixTemplateParams(List<String> params){
    // parameters are not properly parsed by JWPL
    // if there is a pipe symbol inside a link e.g. [[Linnee|L.]] it will be split into t params at the pipe symbol. We work around this here
    String openLink = null;
    List<String> newParams = new ArrayList<String>();
    for (String param : params) {
      if (openLink==null){
        // no open link yet, this one?
        if (openLinkPattern.matcher(param).find()){
          openLink=param;
          continue;
        }
      }else{
        // link open, do we close it here?
        if (closeLinkPattern.matcher(param).find()) {
          newParams.add(openLink+"|"+param);
          openLink = null;
          continue;
        }else{
          // buggy wiki markup? keep params as they are...
          newParams.add(openLink);
          openLink = null;
        }
      }
      newParams.add(param);
    }

    return newParams;
  }

	public ResolvedTemplate parseTemplate(Template t, ParsedPage pp) {

		final String templateName = t.getName();

    ResolvedTemplate result = new ResolvedTemplate(t);
    result.setPreParseReplacement("");
    result.setPostParseReplacement("");

    // English, German, Spanish and French
    if (TaxonInfoEN.TAXOBOX_NAME.equalsIgnoreCase(templateName) ||
        TaxonInfoDE.TAXOBOX_NAME.equalsIgnoreCase(templateName) ||
        TaxonInfoES.TAXOBOX_NAME.equalsIgnoreCase(templateName) ||
        TaxonInfoFR.TAXOBOX_NAME.equalsIgnoreCase(templateName)){
      TaxonInfo info = new TaxonInfo();
      result.setParsedObject(info);
      for (String param : fixTemplateParams(t.getParameters())){
        String[] paramItems = StringUtils.split(param, "=", 2);
        if (paramItems.length==2){
          String key = StringUtils.trimToEmpty(paramItems[0]).toLowerCase().replaceAll(" ", "_");
          String value = cleanValue(StringUtils.trimToNull(paramItems[1]));
          if (key.equals("status_ref")){
            // ignore for now
            RefInfo ref = new RefInfo();
            info.setStatus_ref(ref);
          }else if (key.equalsIgnoreCase("synonyms")){
            info.setSynonyms(value);
          }else{
            try {
              PropertyUtils.setProperty(info, key, value);
            } catch (IllegalAccessException e) {
              log.error("IllegalAccessException param={}", param);
            } catch (NoSuchMethodException e) {
              // expected - TaxonInfo bean doesnt cover all props
              // only log unknown props once
              if (!unknownProperties.contains(key)){
                unknownProperties.add(key);
                log.info("Unknown Taxoninfo property={}, value={}", key, value);
              }
            } catch (IllegalArgumentException e) {
              // strange property names?
              log.warn("Illegal property {} : {}", key, e.getMessage());
            } catch (InvocationTargetException e) {
              log.error("InvocationTargetException param={}", param);
            }
          }
        }
      }

    } else if ((fossilRangeTemplateName.equalsIgnoreCase(templateName))){
      // parse fossil range
      StringBuilder range = new StringBuilder();
      boolean from=true;
      for (String param : t.getParameters()) {
        try {
          Double d = Double.parseDouble(param);
          range.append(d);
          if (from){
            range.append("-");
            from = false;
          }else{
            range.append(" ");
          }
        } catch (NumberFormatException e) {
          if (!param.contains("=")){
            range.append(param);
            range.append(" ");
          }
        }
        log.debug("Fossil range template found {}", range.toString());
        result.setPostParseReplacement(range.toString());
      }
    } else if ((citeTemplateName.equalsIgnoreCase(templateName))) {
      // parse citation
    } else{
      // drop all other templates
      if (log.isDebugEnabled() && !unknownTemplates.contains(templateName)){
        unknownTemplates.add(templateName);
        log.debug("New unkown template: {}", templateName);
      }
      result.setParsedObject(null);
    }

    // remember parsed object for later access
    lastTaxon= (TaxonInfo) result.getParsedObject();

    return result;
  }
}
