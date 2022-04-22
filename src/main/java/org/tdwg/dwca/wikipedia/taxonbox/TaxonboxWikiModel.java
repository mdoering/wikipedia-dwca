package org.tdwg.dwca.wikipedia.taxonbox;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Functions;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import info.bliki.extensions.scribunto.ScribuntoException;
import info.bliki.htmlcleaner.TagToken;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.filter.ParsedPageName;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import info.bliki.wiki.model.WikiModelContentException;
import info.bliki.wiki.namespaces.INamespace;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdwg.dwca.wikipedia.WikipediaConfig;
import org.tdwg.dwca.wikipedia.bliki.TaxonConfiguration;
import org.tdwg.dwca.wikipedia.bliki.TaxonTag;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wiki model that is aware of most taxonomic templates plus the major citation and fossil/palaeo templates.
 * For automatic taxonboxes the classification from the Taxonomy templates are scraped.
 * See README.md for details on supported templates!
 */
public class TaxonboxWikiModel extends WikiModel {
  private final static Logger LOG = LoggerFactory.getLogger(TaxonboxWikiModel.class);

  private final static Set<String> TAXOBOX_TEMPLATES = Sets.newHashSet("taxobox", "automatictaxobox", "fichadetaxón",
    "fichadetaxon");
  private final static Set<String> CITATION_TEMPLATES = Sets.newHashSet("cite", "citeweb", "citebook", "citejournal");
  private final static Set<String> FOSSIL_RANGE_TEMPLATES = Sets.newHashSet("fossilrange", "geologicalrange",
    "longfossilrange");
  private final static Set<String> SPECIES_LIST_TEMPLATES = Sets.newHashSet("specieslist", "taxonlist");
  private final static Set<String> PLAIN_LIST_TEMPLATES = Sets.newHashSet("plainlist", "flatlist");
  private final static Set<String> QUOTE_TEMPLATES = Sets.newHashSet("quote");

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

  private static final PlainTextConverter converter = new PlainTextConverter();

  private final WikipediaConfig cfg;
  private TaxonInfo info;
  private TaxonboxWikiModel internalWiki;
  private boolean multipleTaxa = false;
  private Map<String, String> unknownProperties = Maps.newHashMap();
  private Map<String, String> unknownTemplates = Maps.newHashMap();
  @VisibleForTesting
  protected Map<String, Integer> unknownTemplatesCounter = Maps.newHashMap();

  public TaxonboxWikiModel(WikipediaConfig cfg) {
    super(TaxonConfiguration.DEFAULT_CONFIGURATION, "http://image.wikipedia.org/${image}", "http://"+cfg.lang+".wikipedia.org/${title}");
    this.cfg = cfg;
    internalWiki = new TaxonboxWikiModel(this);
    // update TaxonConfiguration with a reference to this wikimodel
    TaxonConfiguration.setWikiModel(this);
  }

  public TaxonboxWikiModel(TaxonboxWikiModel wiki) {
    super(TaxonConfiguration.DEFAULT_CONFIGURATION, wiki.getImageBaseURL(), wiki.getWikiBaseURL());
    this.cfg = wiki.cfg;
  }

  @Override
  public String getRawWikiContent(ParsedPageName parsedPagename, Map<String, String> templateParameters) throws WikiModelContentException {
    String result = super.getRawWikiContent(parsedPagename, templateParameters);
    if (result != null) {
      // found magic word template
      return result;
    }
    String name = encodeTitleToUrl(parsedPagename.pagename, true);
    if (parsedPagename.namespace.isType(INamespace.NamespaceCode.TEMPLATE_NAMESPACE_KEY)) {

      String templateName = name.toLowerCase().replaceAll("[ _-]","");
      Appendable writer = new StringBuilder();

      try {
        //
        // exceptional - we dont render the taxon boxes, but only extract the information !!!
        //
        if (TAXOBOX_TEMPLATES.contains(templateName)) {
          processTaxoBox(templateParameters);
          // check taxonomy templates for auto boxes
          if (templateName.equalsIgnoreCase("automatictaxobox")) {
            AutomaticTaxonomyScraper.updateTaxonInfo(info);
          }

        } else if (templateName.equalsIgnoreCase("speciesbox")){
          processSpeciesBox(Rank.Species, templateParameters);

        } else if (templateName.equalsIgnoreCase("subspeciesbox")){
          processSpeciesBox(Rank.Subspecies, templateParameters);

        } else if (templateName.equalsIgnoreCase("infraspeciesbox")){
          processSpeciesBox(Rank.Infraspecies, templateParameters);

          // Sound templates
        } else if (templateName.equalsIgnoreCase("listen")){
          processSoundBox(templateParameters);

          //
          // append to writer
          //
        } else if (templateName.equalsIgnoreCase("hybrid")){
          return " × ";

        } else if (FOSSIL_RANGE_TEMPLATES.contains(templateName)){
          processFossilRange(templateParameters, writer);

        } else if (CITATION_TEMPLATES.contains(templateName)) {
          if (cfg.footnotes) {
            processCitation(templateParameters, writer);
          }

        } else if (PLAIN_LIST_TEMPLATES.contains(templateName)) {
          if (templateParameters.containsKey("1")) {
            return templateParameters.get("1").replaceAll("#", "*");
          }
          return "";

        } else if (QUOTE_TEMPLATES.contains(templateName)) {
          processQuote(templateParameters, writer);

        } else if (templateName.equalsIgnoreCase("convert")) {
          processConvert(templateParameters, writer);

        } else if (templateName.equalsIgnoreCase("taxonbar")) {
          processTaxonBar(templateParameters);

        } else if (templateName.equalsIgnoreCase("dagger")) {
          return "†";

        } else if (templateName.equalsIgnoreCase("collapsiblelist")) {
          processCollapsibleList(templateParameters, writer);

        } else if (SPECIES_LIST_TEMPLATES.contains(templateName)) {
          processSpeciesList(templateParameters, writer);


        } else{
          // log all other templates found on known species pages!
          if (info != null) {
            if (!unknownTemplates.containsKey(templateName)){
              unknownTemplates.put(templateName, templateParameters==null ? "" : templateParameters.toString());
              unknownTemplatesCounter.put(templateName, 1);
            } else {
              unknownTemplatesCounter.put(templateName, unknownTemplatesCounter.get(templateName)+1);
            }
          }
          // remove all other templates
          return "";
        }

        return writer.toString();

      } catch (IOException e) {
        LOG.error("IO error parsing wiki content for article {}", parsedPagename, e);
      }
    }
    return null;
  }

  /**
   * TODO: Parses alternative ids by reading wikidata
   * taxonbar -> {from=Q161577}
   */
  private void processTaxonBar(Map<String, String> templateParameters) {

  }

  @Override
  public String render(ITextConverter converter, String rawWikiText, boolean templateTopic) throws IOException {
    try {
      String text = super.render(converter, rawWikiText, templateTopic);
      return text == null ? null : text.trim();
    } catch (Exception e) {
      LOG.error("Render problem for {}: {}", rawWikiText.substring(0, Math.min(100,rawWikiText.length())), e.getMessage());
      return null;
    }
  }

  @Override
  public boolean pushNode(TagToken tag) {
    // call taxon process interface if needed
    if (tag instanceof TaxonTag) {
      ((TaxonTag) tag).setTaxon(getTaxonInfo());
    }
    return super.pushNode(tag);
  }

  private void processConvert(Map<String, String> parameterMap, Appendable writer) {
    try {
      String val = parameterMap.get("1");
      String unit = parameterMap.get("2");
      writer.append(val);
      writer.append(" ");
      writer.append(unit);
    } catch (Exception e) {
      // ignore
    }
  }

  /**
   * quote -> {1=By the time that an animal had reached, after numberless generations, the deepest recesses, disuse will on this view have more or less perfectly obliterated its eyes, and natural selection will often have effected other changes, such as an increase in the length of antennae or palpi, as compensation for blindness., 2=Charles Darwin, 3=Origin of Species (1859)}
   */
  private void processQuote(Map<String, String> parameterMap, Appendable writer) {
    if (parameterMap.containsKey("1")) {
      try {
        String quote = parameterMap.get("1");
        String author = parameterMap.get("2");
        String work = parameterMap.get("3");
        writer.append(" \"");
        writer.append(quote);
        writer.append("\"");
        if (author != null) {
          writer.append(" (");
          writer.append(author);
          if (work != null) {
            writer.append(" in ");
            writer.append(work);
          }
          writer.append(")");
        }
      } catch (IOException e) {
        // ignore
      }
    }
  }

  private void processSoundBox(Map<String,String> parameterMap) {
    if (info != null) {
      LOG.debug("Sound box found for name {}", info.getScientificName());
      Sound sound = new Sound();
      for (String param : parameterMap.keySet()) {
        String key = param2Key(param);
        // not all properties are names, but most are
        String value = cleanNameValue(parameterMap.get(param));
        try {
          PropertyUtils.setProperty(sound, key, value);
        } catch (IllegalAccessException e) {
          LOG.error("IllegalAccessException Sound param={}", param);
        } catch (NoSuchMethodException e) {
          // expected - Sound bean doesnt cover all props
        } catch (IllegalArgumentException e) {
          // strange property names?
          LOG.warn("Illegal Sound property {} : {}", key, e.getMessage());
        } catch (InvocationTargetException e) {
          LOG.error("InvocationTargetException Sound param={}", param);
        }
      }
      // if a url exists keep it
      if (!Strings.isNullOrEmpty(sound.getUrl())) {
        info.getSounds().add(sound);
      }
    }
  }

  private String param2Key(String param) {
    return StringUtils.trimToEmpty(param.toLowerCase().replaceAll(" ", "_"));
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
            LOG.warn("{} box found with species name {}. Raw params {}", new Object[] {rank, species, parameterMap});
          }
        }
      }
    }
    info.setScientificNameAuthorship(cleanRawValue(parameterMap.get("authority")));
    info.setName(cleanRawValue(parameterMap.get("name")));
    info.setGenus(cleanNameValue(parameterMap.get("genus")));
    info.setSubgenus(cleanNameValue(parameterMap.get("subgenus")));
    info.setExtinct(cleanNameValue(parameterMap.get("extinct")));
    // check taxonomy templates
    AutomaticTaxonomyScraper.updateTaxonInfo(info);
  }

  private void processTaxoBox(Map<String, String> parameterMap) {
    if (info != null) {
      multipleTaxa = true;
      return;
    }
    info = new TaxonInfo();
    info.setRawParams(ImmutableMap.copyOf(parameterMap));
    for (String param : parameterMap.keySet()){
      String key = param2Key(param);

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
          LOG.error("IllegalAccessException param={}", param);
        } catch (NoSuchMethodException e) {
          // expected - TaxonInfo bean doesnt cover all props
          // only LOG unknown props once
          if (!unknownProperties.containsKey(key)){
            unknownProperties.put(key, parameterMap.get(param));
          }
        } catch (IllegalArgumentException e) {
          // strange property names?
          LOG.warn("Illegal property {} : {}", key, e.getMessage());
        } catch (InvocationTargetException e) {
          LOG.error("InvocationTargetException param={}", param);
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
    if (parameterMap.containsKey("3") && !StringUtils.isBlank(parameterMap.get("3"))) {
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
    if (wikiText!=null) {
      try {
        if (internalWiki != null) {
          return internalWiki.render(converter, wikiText);
        }
        return render(converter, wikiText);

      } catch (IOException e) {
        LOG.error("Error rendering wikiText: {}", wikiText, e);
      }
    }
    return null;
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

  public Map<String, Integer> getUnknownTemplatesCounter() {
    return ImmutableSortedMap.copyOf(unknownTemplatesCounter,
      Ordering.natural().reverse().nullsLast()
        .onResultOf(Functions.forMap(unknownTemplatesCounter, null))
        .compound(Ordering.natural())
    );
  }
}
