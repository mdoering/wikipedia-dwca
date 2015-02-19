package org.tdwg.dwca.wikipedia.taxonbox;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RankTest {

  @Test
  public void testIsLowerThan() throws Exception {
    assertTrue(Rank.Species.isLowerThan(Rank.Genus));
    assertTrue(Rank.Species.isLowerThan(Rank.Subfamily));
    assertTrue(Rank.Species.isLowerThan(Rank.Tribe));
    assertFalse(Rank.Genus.isLowerThan(Rank.Genus));
    assertTrue(Rank.Species.isLowerThan(Rank.Uninterpretable));
  }

  @Test
  public void testIsHigherThan() throws Exception {
    assertTrue(Rank.Domain.isHigherThan(Rank.Genus));
    assertTrue(Rank.Family.isHigherThan(Rank.Subfamily));
    assertTrue(Rank.Genus.isHigherThan(Rank.Uninterpretable));
    assertFalse(Rank.Species.isHigherThan(Rank.Species));
    assertTrue(Rank.Species.isHigherThan(Rank.Uninterpretable));
  }
}
