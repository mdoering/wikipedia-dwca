package org.tdwg.dwca.wikipedia;

import org.gbif.api.vocabulary.Language;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.validation.constraints.NotNull;

import com.beust.jcommander.Parameter;
import org.tdwg.dwca.wikipedia.cli.LanguageConverter;

public class WikipediaConfig {
  @Parameter(names = {"-r", "--repo"}, description = "Directory to download wikipedia dumps to. If last versions are found a conditional get download will be done. Defaults to /tmp/wikipedia-data")
  public File repo = new File("/tmp/wikipedia-data");

  @Parameter(names = {"-l", "--lang"}, description = "Wikipedia language file to parse (en, es, fr, de).", required = true, converter = LanguageConverter.class)
  @NotNull
  public Language lang;

  @Parameter(names = {"-o", "--offline"}, description = "If true no wikipedia dumps will be downloaded and only local dump files will be used. Defaults to false")
  public boolean offline = false;

  @Parameter(names = {"-tmp", "--keepTmp"}, description = "If true temporay files during archive build will be kept. Defaults to false")
  public boolean keepTmp = false;

  @Parameter(names = {"-f", "--footnotes"}, description = "If true footnotes found in descriptions are expanded and kept")
  public boolean footnotes = false;

  @Parameter(names = "--help", help = true)
  private boolean help;

  public File getDwcaFile() {
    return getRepoFile("wikipedia-" + lang.getIso2LetterCode() + "-dwca.zip");
  }

  public File getRepoFile(String name) {
    return new File(repo, name);
  }

  public File getDumpFile() {
    return getRepoFile(lang.getIso2LetterCode()+"-wikipedia.xml.bz2");
  }

  public URL getWikipediaDumpUrl() {
    try {
      return getWikipediaDumpUri().toURL();
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Cannot build wikipedia URL", e);
    }
  }
  public URI getWikipediaDumpUri() {
    return URI.create(String.format("https://dumps.wikimedia.org/%swiki/latest/%swiki-latest-pages-articles.xml.bz2", lang, lang));
  }


  public URI getWikipediaHomepage() {
    return URI.create("https://" + lang + ".wikipedia.org");
  }
}
