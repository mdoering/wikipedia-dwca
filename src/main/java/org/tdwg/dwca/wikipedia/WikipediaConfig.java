package org.tdwg.dwca.wikipedia;

import org.gbif.api.vocabulary.Language;

import java.io.File;

import com.beust.jcommander.Parameter;

public class WikipediaConfig {
  @Parameter(names = {"-r", "--repo"}, description = "Directory to download wikipedia dumps to. If last versions are found a conditional get download will be done. Defaults to /tmp/wikipedia-data")
  public File repo = new File("/tmp/wikipedia-data");

  @Parameter(names = {"-l", "--lang"}, description = "Wikipedia language file to parse (en, es, fr, de, etc.). Defaults to english")
  public String lang= "en";

  @Parameter(names = {"-o", "--offline"}, description = "If true no wikipedia dumps will be downloaded and only local dump files will be used. Defaults to false")
  public boolean offline = false;

  @Parameter(names = {"-tmp", "--keepTmp"}, description = "If true temporay files during archive build will be kept. Defaults to false")
  public boolean keepTmp = false;

  @Parameter(names = {"-f", "--footnotes"}, description = "If true footnotes found in descriptions are expanded and kept")
  public boolean footnotes = false;

  @Parameter(names = "--help", help = true)
  private boolean help;

  public Language getLanguage() {
    return Language.fromIsoCode(lang);
  }

  public File getDwcaFile() {
    return new File(repo, "wikipedia-" + lang + "-dwca.zip");
  }

  public File getRepoFile(String name) {
    return new File(repo, name);
  }


}
