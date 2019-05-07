package com.squareup.testing.rules;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ContextRule<T extends Annotation> implements TestRule {
  public static <T extends Annotation> ContextRule<T> on(Class<T> contextClass) {
    return new ContextRule<T>(contextClass);
  }

  public T with;
  public Class<T> withClass;

  public ContextRule(Class<T> withClass) {
    this.withClass = withClass;
  }

  @Override public Statement apply(final Statement base, Description description) {
    with = description.getAnnotation(withClass);
    if (with == null) {
      try {
        Method getAnnotationTypeMethod = Class.class.getDeclaredMethod("getAnnotationType");
        getAnnotationTypeMethod.setAccessible(true);
        Object annotationType = getAnnotationTypeMethod.invoke(withClass);
        Field field = annotationType.getClass().getDeclaredField("memberDefaults");
        field.setAccessible(true);
        final Map memberDefaults = (Map) field.get(annotationType);
        with = (T) Proxy.newProxyInstance(withClass.getClassLoader(),
            new Class[] {withClass},
            new InvocationHandler() {
              @Override public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                return memberDefaults.get(method.getName());
              }
            });

      } catch (Exception e) {
        throw new RuntimeException("couldn't determine default values for " + withClass.getSimpleName() + ", sorry!", e);
      }
    }
    return base;
  }
}
