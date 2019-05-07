package com.squareup.testing.rules;

import java.util.HashMap;
import java.util.Map;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SystemPropertiesRule implements TestRule {
  private Map<String, String> originalProperties = new HashMap<String, String>();

  public void setProperty(String name, String value) {
    saveProperty(name);
    System.setProperty(name, value);
  }

  public void clearProperty(String name) {
    saveProperty(name);
    System.clearProperty(name);
  }

  public void saveProperty(String name) {
    if (!originalProperties.containsKey(name)) {
      originalProperties.put(name, System.getProperty(name));
    }
  }

  @Override public Statement apply(final Statement base, Description description) {
    return new Statement() {
      @Override public void evaluate() throws Throwable {
        try {
          base.evaluate();
        } finally {
          for (Map.Entry<String, String> entry : originalProperties.entrySet()) {
            if (entry.getValue() == null) {
              System.clearProperty(entry.getKey());
            } else {
              System.setProperty(entry.getKey(), entry.getValue());
            }
          }
        }
      }
    };
  }
}
