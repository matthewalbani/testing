package com.squareup.testing.annotationtests;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ParameterizedWhiteListedTest {
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { }, { }
    });
  }

  @Blacklisted
  @Test
  public void parameterizedBlackListed() {
  }

  @Whitelisted
  @Test
  public void parameterizedWhiteListed() {

  }
}
