package com.squareup.testing;

import java.util.Collections;
import java.util.Set;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import static com.google.common.collect.Sets.newHashSet;

/** Collects the status of test runs, for later inspection */
public class TestRunStatus extends RunListener {
  private final Set<Description> testsRun = newHashSet();
  private final Set<Failure> testFailures = newHashSet();
  private final Set<Failure> testAssumptionFailures = newHashSet();

  @Override public void testFinished(Description description) {
    testsRun.add(description);
  }

  @Override public void testFailure(Failure failure) {
    testFailures.add(failure);
  }

  @Override public void testAssumptionFailure(Failure failure) {
    testAssumptionFailures.add(failure);
  }

  public Set<Description> getTestsRun() {
    return Collections.unmodifiableSet(testsRun);
  }

  public Set<Failure> getTestFailures() {
    return Collections.unmodifiableSet(testFailures);
  }

  public Set<Failure> getTestAssumptionFailures() {
    return Collections.unmodifiableSet(testAssumptionFailures);
  }
}
