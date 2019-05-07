package com.squareup.testing.methodtests;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ParameterizedTest {
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { }, { }
    });
  }

  @Test
  public void parameterized_test1() {
  }

  @Test public void parameterized_test2() {
  }
}
