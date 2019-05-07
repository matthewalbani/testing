package com.squareup.testing.asserts;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MoreAssertsTest {
  static abstract class Base {
    Object oInBase;
    Base sub;
    int i;
  }
  static class One extends Base { }
  static class Two extends Base {
    byte[] bytes;
  }

  @Test
  public void deepEquivalence_deeplySame() throws Exception {
    One expected = new One();
    expected.sub = new Two();
    expected.oInBase = "value";
    One actual = new One();
    actual.sub = new Two();
    actual.oInBase = "value";
    MoreAsserts.assertDeepObjectEquivalence(expected, actual);
  }

  @Test
  public void deepEquivalence_knowsAbstractImplementationsDiffer() throws Exception {
    One expected = new One();
    expected.oInBase = "value";
    Two actual = new Two();
    actual.oInBase = "value";
    try {
      MoreAsserts.assertDeepObjectEquivalence(expected, actual);
      Assert.fail("exception expected");
    } catch (ComparisonFailure e) {
      assertTrue(e.getExpected()
          .indexOf("\"__CLASS\": \"com.squareup.testing.asserts.MoreAssertsTest.One\"") > 0);
      assertTrue(e.getActual()
          .indexOf("\"__CLASS\": \"com.squareup.testing.asserts.MoreAssertsTest.Two\"") > 0);
    }
  }

  @Test
  public void deepEquivalence_arrays() throws Exception {
    Two expected = new Two();
    expected.bytes = new byte[]{1,2,3};
    Two actual = new Two();
    actual.bytes = new byte[]{1,2,3};
    MoreAsserts.assertDeepObjectEquivalence(expected, actual);
  }

  @Test
  public void deepEquivalence_knowsPrimitiveTypesDiffer() throws Exception {
    Two expected = new Two();
    expected.oInBase = new byte[]{1,2,3};
    Two actual = new Two();
    actual.oInBase = new int[]{1,2,3};
    try {
      MoreAsserts.assertDeepObjectEquivalence(expected, actual);
      Assert.fail("exception expected");
    } catch (ComparisonFailure e) {
      // arrays print [I, etc notation as explained here:
      // http://docs.oracle.com/javase/6/docs/api/java/lang/Class.html#getName%28%29
      assertTrue(e.getExpected().indexOf("\"__CLASS\": \"class [B\"") > 0);
      assertTrue(e.getActual().indexOf("\"__CLASS\": \"class [I\"") > 0);
    }
  }

  @Test
  public void assertWithin_inRange() throws Exception {
    DateTime expected = new DateTime(2013, 11, 2, 0, 0);
    DateTime actual = new DateTime(2013, 11, 1, 23, 59, 54, 501);
    MoreAsserts.assertWithin(expected, actual, Duration.standardSeconds(50));
  }

  @Test @SuppressWarnings("AssertionFailureIgnored")
  public void assertWithin_outOfRange() throws Exception {
    DateTime expected = new DateTime(2013, 11, 2, 0, 0);
    DateTime actual = new DateTime(2013, 11, 1, 23, 59, 54, 501);
    try {
      MoreAsserts.assertWithin(expected, actual, Duration.standardSeconds(5));
      Assert.fail("exception expected");
    } catch (AssertionError e) {
      Assert.assertEquals("Expected: 2013-11-02T00:00:00.000Z. Actual: 2013-11-01T23:59:54.501Z. Should be within 5000 ms but was ahead by -5499 millis", e.getMessage());
    }
  }
}
