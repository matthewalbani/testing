package com.squareup.testing.annotationtests;

import org.junit.Ignore;
import org.junit.Test;

public final class UnannotatedTypeTest {
  @Test public void unannotatedTypeUnannotatedMethod() throws Exception {
  }

  @Whitelisted
  @Test public void unannotatedTypeWhitelistedMethod() throws Exception {
  }

  @Blacklisted
  @Test public void unannotatedTypeBlacklistedMethod() throws Exception {
  }

  @Ignore
  @Test public void unannotatedTypeIgnoredMethod() throws Exception {
  }
}
