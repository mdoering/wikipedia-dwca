package org.tdwg.dwca.wikipedia.taxonbox;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wiki model that is aware of most taxonomic templates plus the major citation and fossil/palaeo templates.
 * In detail the supported templates are:
 *
 * http://en.wikipedia.org/wiki/Template:Taxobox
 * http://de.wikipedia.org/wiki/Wikipedia:Taxoboxen
 * http://en.wikipedia.org/wiki/Template:Automatic_taxobox/doc
 *
 * http://en.wikipedia.org/wiki/Template:Speciesbox/doc
 * http://en.wikipedia.org/wiki/Template:Subspeciesbox/doc
 * http://en.wikipedia.org/wiki/Template:Infraspeciesbox/doc
 *
 * http://en.wikipedia.org/wiki/Template:Hybrid
 *
 * http://en.wikipedia.org/wiki/Template:Species_list/doc
 * http://en.wikipedia.org/wiki/Template:Taxon_list
 *
 * http://simple.wikipedia.org/wiki/Template:Fossil_range/doc
 * http://en.wikipedia.org/wiki/Template:Long_fossil_range
 * http://en.wikipedia.org/wiki/Template:Geological_range
 */
public class TaxonboxWikiModel extends WikiModel {
  private final Set<String> TAXOBOX_TEMPLATES = Sets.newHashSet("taxobox", "automatictaxobox", "fichadetaxón", "fichadetaxon");
  private final Set<String> CITATION_TEMPLATES = Sets.newHashSet("cite", "citeweb", "citebook", "citejournal");
  private final Set<String> FOSSIL_RANGE_TEMPLATES = Sets.newHashSet("fossilrange","geologicalrange", "longfossilrange");
  private final Set<String> SPECIES_LIST_TEMPLATES = Sets.newHashSet("specieslist", "taxonlist");
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final String lang;
  private TaxonInfo info;
  private Map<String, String> unknownProperties = Maps.newHashMap();
  private Map<String, String> unknownTemplates = Maps.newHashMap();
  private static final Pattern cleanNames = Pattern.compile("(''+|[†]|<[^>]+>)", Pattern.CASE_INSENSITIVE);
  private static final Pattern replOrName = Pattern.compile("\\((or [^()]+|plant|animal)\\)");

  private static final Pattern synonymsBr = Pattern.compile("<br */?>", Pattern.CASE_INSENSITIVE);
  private static final String[] CITE_suffix = new String[]{"","1","2","3","4","5","6","7","8","9"};
  private static final String[] CITE_last_aliases = new String[]{"author","authors","last"};

  private PlainTextConverter converter = new PlainTextConverter();

  public TaxonboxWikiModel(String lang) {
    super("http://image.wikipedia.org/${image}", "http://"+lang+".wikipedia.org/${title}");
    this.lang = lang;
  }

  @Override
  public void substituteTemplateCall(String templateName, Map<String, String> parameterMap, Appendable writer) throws IOException {
    templateName = templateName.toLowerCase().replaceAll("[ _-]","");
    if (TAXOBOX_TEMPLATES.contains(templateName)) {
      processTaxoBox(parameterMap);

    } else if (templateName.equalsIgnoreCase("hybrid")){
      writer.append(" × ");

    } else if (templateName.equalsIgnoreCase("speciesbox")){
      processSpeciesBox(Rank.Species, parameterMap, writer);

    } else if (templateName.equalsIgnoreCase("subspeciesbox")){
      processSpeciesBox(Rank.Subspecies, parameterMap, writer);

    } else if (templateName.equalsIgnoreCase("infraspeciesbox")){
      processSpeciesBox(Rank.Infraspecies, parameterMap, writer);

    } else if (FOSSIL_RANGE_TEMPLATES.contains(templateName)){
      processFossilRange(parameterMap, writer);

    } else if (CITATION_TEMPLATES.contains(templateName)) {
      processCitation(parameterMap, writer);

    } else if (SPECIES_LIST_TEMPLATES.contains(templateName)) {
      processSpeciesList(parameterMap, writer);

    } else{
      // log all other templates
      if (!unknownTemplates.containsKey(templateName)){
        unknownTemplates.put(templateName, parameterMap.toString());
      }
    }
  }

  private void processSpeciesBox(Rank rank, Map<String,String> parameterMap, Appendable writer) {
    info = new TaxonInfo();
    info.setRawParams(ImmutableMap.copyOf(parameterMap));

    if (parameterMap.containsKey("taxon")) {
      info.setTaxon(cleanNameValue(parameterMap.get("taxon")));
      info.setRank(rank);

    } else if (parameterMap.containsKey("genus")){
      if (parameterMap.containsKey("species")){
        String species = cleanNameValue(parameterMap.get("genus")) + " " + cleanNameValue(parameterMap.get("species"));
        if (parameterMap.containsKey("form")){
          info.setScientificNameAndRankIfLowest(Rank.Variety, species + " f. " + cleanNameValue(parameterMap.get("form")));
        } else if (parameterMap.containsKey("variety")){
          info.setScientificNameAndRankIfLowest(Rank.Variety, species + " var. " + cleanNameValue(parameterMap.get("species")));
        } else if (parameterMap.containsKey("subspecies")){
          info.setScientificNameAndRankIfLowest(Rank.Subspecies, species + " subsp. " + cleanNameValue(parameterMap.get("subspecies")));
        } else {
          info.setTaxon(species);
          info.setRank(Rank.Species);
          if (Rank.Species != rank) {
            log.warn("{} box found with species name {}. Raw params {}", new Object[]{rank, species, parameterMap});
          }
        }
      }
    }
    info.setScientificNameAuthorship(cleanRawValue(parameterMap.get("authority")));
    info.setName(cleanRawValue(parameterMap.get("name")));
    info.setGenus(cleanNameValue(parameterMap.get("genus")));
    info.setSubgenus(cleanNameValue(parameterMap.get("subgenus")));
    info.setExtinct(cleanNameValue(parameterMap.get("extinct")));
  }

  private void processSpeciesList(Map<String, String> parameterMap, Appendable writer) throws IOException {
    boolean startName = true;
    for (Map.Entry<String, String> tax : parameterMap.entrySet()) {
      writer.append( subRender(tax.getValue()) );
      startName = !startName;
      if (startName) {
        writer.append("<br/>");
      }
    }
  }

  private void processTaxoBox(Map<String, String> parameterMap) {
    info = new TaxonInfo();
    info.setRawParams(ImmutableMap.copyOf(parameterMap));
    for (String param : parameterMap.keySet()){
      String key = StringUtils.trimToEmpty(param.toLowerCase().replaceAll(" ", "_"));

      if (key.equals("status_ref")){
        // ignore for now

      }else if (key.equalsIgnoreCase("synonyms")){
        String[] synonyms = null;

        String rawSynonyms = parameterMap.get(param).trim();
        if (rawSynonyms.contains("*")) {
          // * seperator
          // * ''[[Ardea (genus)|Ardea]] paradisea'' <small>Lichtenstein, AAH, 1793</small>
          // * '''''Tetrapteryx capensis''''' <small>Thunberg, 1818</small>
          // * ''Anthropoides '''stanleyanus''''' <small>Vigors, 1826</small>
          // * ''[[Grus (genus)|Grus]] '''caffra''''' <small>Fritsch, 1868</small>
          synonyms = StringUtils.split(rawSynonyms, "*");

        } else if (synonymsBr.matcher(rawSynonyms).find()) {
          synonyms = synonymsBr.split(rawSynonyms);

        } else if (rawSynonyms.startsWith("{{")) {
          synonyms = cleanRawValue(rawSynonyms).split("<br/>");

        } else {
          synonyms = new String[]{rawSynonyms};
        }

         // clean and test names before adding them
        for (String synRaw : synonyms) {
          String syn = cleanRawValue(synRaw);
          if (syn != null) {
            info.addSynonym(syn);
          }
        }

      }else{
        String value = cleanRawValue(parameterMap.get(param));
        try {
          PropertyUtils.setProperty(info, key, value);
        } catch (IllegalAccessException e) {
          log.error("IllegalAccessException param={}", param);
        } catch (NoSuchMethodException e) {
          // expected - TaxonInfo bean doesnt cover all props
          // only log unknown props once
          if (!unknownProperties.containsKey(key)){
            unknownProperties.put(key, parameterMap.get(param));
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

  /**
   * {{fossil range|Late Devonian|present}}
   */
  private void processFossilRange(Map<String, String> parameterMap, Appendable writer) throws IOException {
    log.debug("Fossil range template found with params {}", parameterMap);
    // parse fossil range
    boolean closeBrackets = false;
    if (parameterMap.containsKey("3")) {
      writer.append(cleanRawValue(parameterMap.get("3")));
      writer.append(" (");
      closeBrackets = true;
    }

    writer.append(cleanRawValue(parameterMap.get("1")));

    if (parameterMap.containsKey("2")) {
      boolean number = false;
      try {
        Double.parseDouble(parameterMap.get("2").trim());
        writer.append("-");
        number = true;
      } catch (NumberFormatException e) {
        writer.append(" to ");
      }
      writer.append(cleanRawValue(parameterMap.get("2")));
      if (number) {
        writer.append(" Ma");
      }
    }

    if (closeBrackets) {
      writer.append(")");
    }
  }

  /**
   *
   * see http://en.wikipedia.org/wiki/Template:Citation
   * and http://de.wikipedia.org/wiki/Vorlage:Cite_web
   *
   * {{cite web
    | url = http://beta.uniprot.org/taxonomy/3744
    | title = Order '''Rosales'''
    | accessdate = 2008-04-24
    | author = UniProt
    | authorlink = UniProt
    }}
   * @param parameterMap
   * @param writer
   */
  private void processCitation(Map<String, String> parameterMap, Appendable writer) throws IOException {
    // title is required !!!
    String title = cleanRawValue(parameterMap.get("title"));
    if (title == null){
      return;
    }

    boolean started = false;
    // authors
    StringBuilder authorSB = new StringBuilder();
    for (String sfx : CITE_suffix) {
      for (String l : CITE_last_aliases) {
        String last = cleanRawValue(parameterMap.get(l+sfx));
        if (last != null){
          appendAuthor(last, cleanRawValue(parameterMap.get("first"+sfx)), authorSB);
        }
      }
    }
    if (authorSB.length() > 0){
      writer.append(authorSB.toString());
      started = true;
    }

    String date = getFirstParsedParameter(parameterMap, "date", "year");
    if (date != null){
      if (started){
        writer.append(" (");
        writer.append(date);
        writer.append(")");
      } else {
        writer.append(date);
      }
      started = true;
    }

    if (started){
      writer.append(": ");
    }
    writer.append(title);

    String work = getFirstParsedParameter(parameterMap, "work", "journal", "journal", "journal");
    if (work != null){
      writer.append(", ");
      writer.append(work);
    }

    String vol = cleanRawValue(parameterMap.get("volume"));
    if (vol != null){
      writer.append(", ");
      writer.append(vol);
    }

    String issue = cleanRawValue(parameterMap.get("issue"));
    if (issue != null){
      writer.append("(");
      writer.append(issue);
      writer.append(")");
    }

    if (work == null) {
      String edition = cleanRawValue(parameterMap.get("edition"));
      if (edition != null){
        writer.append("(");
        writer.append(edition);
        writer.append(")");
      }
    }

    String pages = getFirstParsedParameter(parameterMap, "page", "pages");
    if (pages!= null){
      writer.append(": pp. ");
      writer.append(pages);
    }

    String publisher = cleanRawValue(parameterMap.get("publisher"));
    if (publisher != null){
      if (title != null || work != null){
        writer.append(". ");
      }
      writer.append(publisher);
    }
  }

  private String getFirstParsedParameter(Map<String, String> parameterMap, String ... aliases) {
    for (String param : aliases) {
      String x = cleanRawValue(parameterMap.get(param));
      if (x != null) {
        return x;
      }
    }
    return null;
  }

  private void appendAuthor(String last, String first, StringBuilder sb) {
    if (!Strings.isNullOrEmpty(first) || !Strings.isNullOrEmpty(last)) {
      if (sb.length() > 0) {
        sb.append("; ");
      }
      if (!Strings.isNullOrEmpty(first) && !Strings.isNullOrEmpty(last)) {
        sb.append(last);
        sb.append(", ");
        sb.append(first);
      } else {
        sb.append(Strings.nullToEmpty(last));
        sb.append(Strings.nullToEmpty(first));
      }
    }
  }

  private String subRender(String wikiText){
    if (wikiText==null) return null;
    return parseTemplates(wikiText);
  }

  private String cleanRawValue(String val){
    if (val==null) return null;
    return Strings.emptyToNull(StringUtils.normalizeSpace(subRender(val)));
  }

  private String cleanNameValue(String val){
    if (val==null) return null;
    String cleaned = subRender(val);
    cleaned = cleanNames.matcher(cleaned).replaceAll(" ");
    cleaned = replOrName.matcher(cleaned).replaceAll(" ");
    return Strings.emptyToNull(StringUtils.normalizeSpace(cleaned));
  }

  public void reset() {
    info = null;
  }

  public boolean isSpeciesPage() {
    return info != null;
  }

  public TaxonInfo getTaxonInfo() {
    return info;
  }

  public Map<String, String> getUnknownProperties() {
    return unknownProperties;
  }

  public Map<String, String> getUnknownTemplates() {
    return unknownTemplates;
  }
}
