package org.tdwg.dwca.wikipedia.taxonbox;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
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
 * http://de.wikipedia.org/wiki/Wikipedia:Pal%C3%A4oboxen
 * TODO: find out ways to retrieve the classification from the Taxonomy templates for automatic boxes
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
 * http://en.wikipedia.org/wiki/Template:Plainlist
 * http://en.wikipedia.org/wiki/Template:Flatlist
 * http://en.wikipedia.org/wiki/Template:Collapsible_list
 *
 * http://simple.wikipedia.org/wiki/Template:Fossil_range/doc
 * http://en.wikipedia.org/wiki/Template:Long_fossil_range
 * http://en.wikipedia.org/wiki/Template:Geological_range
 *
 * http://en.wikipedia.org/wiki/Template:Cite
 * http://en.wikipedia.org/wiki/Template:Cite_journal
 * http://en.wikipedia.org/wiki/Template:Cite_book
 * http://en.wikipedia.org/wiki/Template:Cite_web
 *
 *
 * TODO: support more templates:
 * http://en.wikipedia.org/wiki/Wikipedia:WikiProject_Tree_of_Life/Cultivar_infobox
 * http://de.wikipedia.org/wiki/Wikipedia:Viroboxen
 */
public class TaxonboxWikiModel extends WikiModel {
  private final Set<String> TAXOBOX_TEMPLATES = Sets.newHashSet("taxobox", "automatictaxobox", "fichadetaxón", "fichadetaxon");
  private final Set<String> CITATION_TEMPLATES = Sets.newHashSet("cite", "citeweb", "citebook", "citejournal");
  private final Set<String> FOSSIL_RANGE_TEMPLATES = Sets.newHashSet("fossilrange","geologicalrange", "longfossilrange");
  private final Set<String> SPECIES_LIST_TEMPLATES = Sets.newHashSet("specieslist", "taxonlist");
  private final Set<String> PLAIN_LIST_TEMPLATES = Sets.newHashSet("plainlist", "flatlist");
  private final Logger log = LoggerFactory.getLogger(getClass());
  protected final String lang;
  private TaxonInfo info;
  private boolean multipleTaxa = false;
  private Map<String, String> unknownProperties = Maps.newHashMap();
  private Map<String, String> unknownTemplates = Maps.newHashMap();
  private static final Pattern REPL_REF_TAG = Pattern.compile("< *ref[a-zA-Z0-9 =\"\'_-]*>[^<>]+</ *ref *>", Pattern.CASE_INSENSITIVE);
  private static final Pattern IS_EXTINCT = Pattern.compile("[†‡]");
  private static final Pattern CLEAN_NAMES = Pattern.compile("[†‡\"'„“+|<>\\[\\]]", Pattern.CASE_INSENSITIVE);
  private static final Pattern REMOVE_QUESTION_MARK = Pattern.compile("\\?");
  private static final Pattern REPL_BRACKET_REMARKS = Pattern.compile("\\( *(or [^()]+|\\?|plant|animal) *\\)");
  private final Pattern REMOVE_TEMPLATES = Pattern.compile("\\{\\{[a-zA-Z0-9-_ ]*\\}\\}");

  private static final Pattern BR_PATTERN = Pattern.compile("<br */?>", Pattern.CASE_INSENSITIVE);
  private static final Pattern SPECIES_LIST_PATTERN = Pattern.compile("\\{\\{(species|taxon)[ _-]?list", Pattern.CASE_INSENSITIVE);
  private static final String[] CITE_suffix = new String[]{"","1","2","3","4","5","6","7","8","9"};
  private static final String[] CITE_last_aliases = new String[]{"author","authors","last"};

  private PlainTextConverter converter = new PlainTextConverter();
  private TaxonboxWikiModel internalWiki;

  public TaxonboxWikiModel(String lang) {
    super("http://image.wikipedia.org/${image}", "http://"+lang+".wikipedia.org/${title}");
    this.lang = lang;
    internalWiki = new TaxonboxWikiModel(this);
  }

  private TaxonboxWikiModel(TaxonboxWikiModel wiki) {
    super("http://image.wikipedia.org/${image}", "http://"+wiki.lang+".wikipedia.org/${title}");
    this.lang = wiki.lang;
  }

  @Override
  public String getRawWikiContent(String namespace, String articleName, Map<String, String> parameterMap) {
    String result = super.getRawWikiContent(namespace, articleName, parameterMap);
    if (result != null) {
      // found magic word template
      return result;
    }
    String name = encodeTitleToUrl(articleName, true);
    if (isTemplateNamespace(namespace)) {

      String templateName = name.toLowerCase().replaceAll("[ _-]","");
      Appendable writer = new StringBuilder();

      try {
        //
        // exceptional - we dont render the taxon boxes, but only extract the information !!!
        //
        if (TAXOBOX_TEMPLATES.contains(templateName)) {
          processTaxoBox(parameterMap);

        } else if (templateName.equalsIgnoreCase("speciesbox")){
          processSpeciesBox(Rank.Species, parameterMap);

        } else if (templateName.equalsIgnoreCase("subspeciesbox")){
          processSpeciesBox(Rank.Subspecies, parameterMap);

        } else if (templateName.equalsIgnoreCase("infraspeciesbox")){
          processSpeciesBox(Rank.Infraspecies, parameterMap);

        //
        // append to writer
        //
        } else if (templateName.equalsIgnoreCase("hybrid")){
          return " × ";

        } else if (FOSSIL_RANGE_TEMPLATES.contains(templateName)){
          processFossilRange(parameterMap, writer);

        } else if (CITATION_TEMPLATES.contains(templateName)) {
          processCitation(parameterMap, writer);

        } else if (PLAIN_LIST_TEMPLATES.contains(templateName)) {
          if (parameterMap.containsKey("1")) {
            return parameterMap.get("1").replaceAll("#", "*");
          }
          return "";

        } else if (templateName.equalsIgnoreCase("collapsiblelist")) {
          processCollapsibleList(parameterMap, writer);

        } else if (SPECIES_LIST_TEMPLATES.contains(templateName)) {
          processSpeciesList(parameterMap, writer);


        } else{
          // log all other templates
          if (!unknownTemplates.containsKey(templateName)){
            unknownTemplates.put(templateName, parameterMap==null ? "" : parameterMap.toString());
          }
          // remove all other templates
          return "";
        }

        return writer.toString();

      } catch (IOException e) {
      }
    }
    return null;
  }

  private void processSpeciesBox(Rank rank, Map<String,String> parameterMap) {
    if (info != null) {
      multipleTaxa = true;
      return;
    }
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

  private void processTaxoBox(Map<String, String> parameterMap) {
    if (info != null) {
      multipleTaxa = true;
      return;
    }
    info = new TaxonInfo();
    info.setRawParams(ImmutableMap.copyOf(parameterMap));
    for (String param : parameterMap.keySet()){
      String key = StringUtils.trimToEmpty(param.toLowerCase().replaceAll(" ", "_"));

      if (key.equals("status_ref")){
        // ignore for now

      }else if (key.equalsIgnoreCase("synonyms")){
        String[] synonyms = null;

        /**
         * We are aware of 4 formats synonyms are given:
         * 1) single name string
         * 2) names concatenated with <br/> (and variations of it)
         * 3) names concatenated with *
         * 4) names given with nested {{species list}} template
         *
         **/

        String rawSynonyms = parameterMap.get(param).trim();
        if (SPECIES_LIST_PATTERN.matcher(rawSynonyms).find()) {
          // process {{species/taxon list}} template if exists and convert into <BR>
          rawSynonyms = cleanRawValue(rawSynonyms);
        }

        if (rawSynonyms.contains("*")) {
          // * seperator, for example:
          // * ''[[Ardea (genus)|Ardea]] paradisea'' <small>Lichtenstein, AAH, 1793</small>
          // * '''''Tetrapteryx capensis''''' <small>Thunberg, 1818</small>
          // * ''Anthropoides '''stanleyanus''''' <small>Vigors, 1826</small>
          // * ''[[Grus (genus)|Grus]] '''caffra''''' <small>Fritsch, 1868</small>
          synonyms = StringUtils.split(rawSynonyms, "*");

        } else if (BR_PATTERN.matcher(rawSynonyms).find()) {
          // <br/> seperator
          synonyms = BR_PATTERN.split(rawSynonyms);

        } else {
          synonyms = new String[]{rawSynonyms};
        }

         // clean and test names before adding them
        for (String synRaw : synonyms) {
          String syn = cleanNameValue(synRaw);
          if (syn != null) {
            info.addSynonym(syn);
          }
        }

      }else{
        // not all properties are names, but most are
        String value = cleanNameValue(parameterMap.get(param));
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


  private void processCollapsibleList(Map<String,String> parameterMap, Appendable writer) throws IOException {
    int max = 10;
    int idx = 1;
    boolean started = false;
    while (idx <= max) {
      if (parameterMap.containsKey(String.valueOf(idx))) {
        if (started) {
          writer.append(" <br/>");
        }
        writer.append(parameterMap.get(String.valueOf(idx)));
        started = true;
        if (idx == max) {
          // we have reached the end and still found items, check next 10 indices
          max += 10;
        }
      }
      idx++;
    }
  }

  private void processSpeciesList(Map<String, String> parameterMap, Appendable writer) throws IOException {
    boolean startName = true;
    for (Map.Entry<String, String> tax : parameterMap.entrySet()) {
      writer.append( tax.getValue() );
      startName = !startName;
      if (startName) {
        writer.append("<br/>");
      } else {
        writer.append(" ");
      }
    }
  }

  /**
   * {{fossil range|Late Devonian|present}}
   */
  private void processFossilRange(Map<String, String> parameterMap, Appendable writer) throws IOException {
    // parse fossil range
    boolean closeBrackets = false;
    if (parameterMap.containsKey("3")) {
      writer.append(parameterMap.get("3"));
      writer.append(" (");
      closeBrackets = true;
    }

    writer.append(parameterMap.get("1"));

    if (parameterMap.containsKey("2")) {
      boolean number = false;
      try {
        Double.parseDouble(parameterMap.get("2").trim());
        writer.append("-");
        number = true;
      } catch (NumberFormatException e) {
        writer.append(" to ");
      }
      writer.append(parameterMap.get("2"));
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
    String title = parameterMap.get("title");
    if (Strings.isNullOrEmpty(title)){
      return;
    }

    boolean started = false;
    // authors
    StringBuilder authorSB = new StringBuilder();
    for (String sfx : CITE_suffix) {
      for (String l : CITE_last_aliases) {
        String last = parameterMap.get(l+sfx);
        if (last != null){
          appendAuthor(last, parameterMap.get("first"+sfx), authorSB);
        }
      }
    }
    if (authorSB.length() > 0){
      writer.append(authorSB.toString());
      started = true;
    }

    String date = getFirstParameter(parameterMap, "date", "year");
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

    String work = getFirstParameter(parameterMap, "work", "journal", "journal", "journal");
    if (work != null){
      writer.append(", ");
      writer.append(work);
    }

    String vol = parameterMap.get("volume");
    if (vol != null){
      writer.append(", ");
      writer.append(vol);
    }

    String issue = parameterMap.get("issue");
    if (issue != null){
      writer.append("(");
      writer.append(issue);
      writer.append(")");
    }

    if (work == null) {
      String edition = parameterMap.get("edition");
      if (edition != null){
        writer.append("(");
        writer.append(edition);
        writer.append(")");
      }
    }

    String pages = getFirstParameter(parameterMap, "page", "pages");
    if (pages!= null){
      writer.append(": pp. ");
      writer.append(pages);
    }

    String publisher = parameterMap.get("publisher");
    if (publisher != null){
      if (title != null || work != null){
        writer.append(". ");
      }
      writer.append(publisher);
    }
  }

  private String getFirstParameter(Map<String, String> parameterMap, String... aliases) {
    for (String param : aliases) {
      String x = parameterMap.get(param);
      if (!Strings.isNullOrEmpty(x)) {
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

  private String internalRender(String wikiText){
    if (wikiText==null) return null;
    if (internalWiki != null) {
      return internalWiki.render(converter, wikiText);
    }
    return render(converter, wikiText);
  }

  private String removeTemplateRemains(String x){
    if (x==null) return null;
    return REMOVE_TEMPLATES.matcher(x).replaceAll(" ");
  }

  private String cleanRawValue(String val){
    if (val==null) return null;
    return Strings.emptyToNull(StringUtils.normalizeSpace(internalRender(val)));
  }

  private String cleanNameValue(String val){
    if (val==null) return null;
    String cleaned = REPL_REF_TAG.matcher(val).replaceAll(" ");

    Matcher m = IS_EXTINCT.matcher(cleaned);
    info.extinctTmp = m.find();

    cleaned = internalRender(cleaned);
    cleaned = CLEAN_NAMES.matcher(cleaned).replaceAll(" ");
    cleaned = REPL_BRACKET_REMARKS.matcher(cleaned).replaceAll(" ");
    cleaned = REMOVE_QUESTION_MARK.matcher(cleaned).replaceAll(" ");
    String name = Strings.emptyToNull(StringUtils.normalizeSpace(cleaned));

    if (name != null && name.equalsIgnoreCase("incertae sedis")) {
      return null;
    }

    return name;
  }

  public void reset() {
    info = null;
    multipleTaxa = false;
  }

  public boolean isSpeciesPage() {
    return !multipleTaxa && info != null;
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
