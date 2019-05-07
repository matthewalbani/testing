package com.squareup.testing.guice;

import java.lang.reflect.Field;
import javax.inject.Inject;

public final class Uninject {
  /**
   * Set null into the injected members of {@code target}. Useful to prevent memory from being
   * retained after a test has completed.
   */
  public static void uninject(Object target) {
    try {
      for (Class<?> c = target.getClass(); c != Object.class; c = c.getSuperclass()) {
        for (Field f : c.getDeclaredFields()) {
          if (f.isAnnotationPresent(Inject.class)) {
            f.setAccessible(true);
            if (!f.getType().isPrimitive()) f.set(target, null);
          }
          if (f.isAnnotationPresent(com.google.inject.Inject.class)) {
            throw new AssertionError("prefer @javax.inject.Inject for " + target.getClass());
          }
        }
      }
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }
}
