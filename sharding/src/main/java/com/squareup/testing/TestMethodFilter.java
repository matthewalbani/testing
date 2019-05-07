package com.squareup.testing;

import cucumber.api.junit.Cucumber;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Filter;

public class TestMethodFilter extends Filter {

  final List<Method> testMethods;

  public TestMethodFilter(List<Method> testMethods) {
    this.testMethods = testMethods;
  }

  @Override
  public boolean shouldRun(Description description) {
    if (description.isSuite()
        || testMethods == null
        || testMethods.isEmpty()
        || description.getTestClass() == null) { // cucumber tests have null test classes :-(
      return true;
    }

    return testMethods.stream().anyMatch(frameworkMethod ->
        frameworkMethod.getDeclaringClass().isAssignableFrom(description.getTestClass()) &&
            frameworkMethod.getName()
                .equals(description.getMethodName().replaceAll("\\[.+\\]$", ""))
    );
  }

  @Override
  public String describe() {
    return "method sharding filter";
  }
}
