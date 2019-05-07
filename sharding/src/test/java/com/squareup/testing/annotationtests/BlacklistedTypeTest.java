package com.squareup.testing.annotationtests;

import org.junit.Ignore;
import org.junit.Test;

@Blacklisted
public final class BlacklistedTypeTest {
  @Test public void blacklistedTypeUnannotatedMethod() throws Exception {
  }

  @Whitelisted
  @Test public void blacklistedTypeWhitelistedMethod() throws Exception {
  }

  @Blacklisted
  @Test public void blacklistedTypeBlacklistedMethod() throws Exception {
  }

  @Ignore
  @Test public void blacklistedTypeIgnoredMethod() throws Exception {
  }
}
