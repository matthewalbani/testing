package com.squareup.testing.annotationtests;

import org.junit.Ignore;
import org.junit.Test;

@Whitelisted
public final class WhitelistedTypeTest {
  @Test public void whitelistedTypeUnannotatedMethod() throws Exception {
  }

  @Whitelisted
  @Test public void whitelistedTypeWhitelistedMethod() throws Exception {
  }

  @Blacklisted
  @Test public void whitelistedTypeBlacklistedMethod() throws Exception {
  }

  @Ignore
  @Test public void whitelistedTypeIgnoredMethod() throws Exception {
  }
}
