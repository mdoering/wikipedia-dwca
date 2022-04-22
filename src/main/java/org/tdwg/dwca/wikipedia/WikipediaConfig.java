package org.tdwg.dwca.wikipedia;

import com.beust.jcommander.Parameter;
import org.gbif.api.vocabulary.Language;
import org.tdwg.dwca.wikipedia.cli.LanguageConverter;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class WikipediaConfig {
  @Parameter(names = {"-r", "--repo"}, description = "Directory to download wikipedia dumps to. If last versions are found a conditional get download will be done. Defaults to /tmp/wikipedia-data")
  public File repo = new File("/tmp/wikipedia-data");

  @Parameter(names = {"-l", "--lang"}, description = "Wikipedia language file to parse (en, es, fr, de).", required = true, converter = LanguageConverter.class)
  @NotNull
  public Language lang;

  @Parameter(names = {"-o", "--offline"}, description = "If true no wikipedia dumps will be downloaded and only local dump files will be used. Defaults to false")
  public boolean offline = false;

  @Parameter(names = {"-tmp", "--keepTmp"}, description = "If true temporary files during archive build will be kept. Defaults to false")
  public boolean keepTmp = false;

  @Parameter(names = {"-f", "--footnotes"}, description = "If true footnotes found in descriptions are expanded and kept")
  public boolean footnotes = false;

  @Parameter(names = "--help", help = true)
  private boolean help;

  public File getDwcaFile() {
    return getRepoFile("wikipedia-" + langIso() + "-dwca.zip");
  }

  public File getRepoFile(String name) {
    return new File(repo, name);
  }

  public File getDumpFile() {
    return getRepoFile(langIso()+"-wikipedia.xml.bz2");
  }

  private String langIso() {
    return lang.getIso2LetterCode();
  }

  public URL getWikipediaDumpUrl() {
    try {
      return getWikipediaDumpUri().toURL();
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Cannot build wikipedia URL", e);
    }
  }
  public URI getWikipediaDumpUri() {
    return URI.create(String.format("https://dumps.wikimedia.org/%swiki/latest/%swiki-latest-pages-articles.xml.bz2", langIso(), langIso()));
  }


  public URI getWikipediaHomepage() {
    return URI.create("https://" + langIso() + ".wikipedia.org");
  }
}
