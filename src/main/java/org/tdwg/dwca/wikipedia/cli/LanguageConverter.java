package org.tdwg.dwca.wikipedia.cli;

import org.gbif.api.vocabulary.Language;

import com.beust.jcommander.IStringConverter;

/**
 *
 */
public class LanguageConverter implements IStringConverter<Language> {

  @Override
  public Language convert(String value) {
    return Language.fromIsoCode(value);
  }
}