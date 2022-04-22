package org.tdwg.dwca.wikipedia.cli;

import com.beust.jcommander.IStringConverter;
import org.gbif.api.vocabulary.Language;

/**
 *
 */
public class LanguageConverter implements IStringConverter<Language> {

  @Override
  public Language convert(String value) {
    return Language.fromIsoCode(value);
  }
}