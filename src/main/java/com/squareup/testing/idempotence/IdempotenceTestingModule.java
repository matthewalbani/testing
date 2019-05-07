package com.squareup.testing.idempotence;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.squareup.common.IdempotentMethod;

/**
 * Install this module to have methods annotated with {@link IdempotentMethod} invoked twice in
 * integration tests.
 */
public class IdempotenceTestingModule extends AbstractModule {
  @Override
  public void configure() {
    bindInterceptor(Matchers.any(), Matchers.annotatedWith(IdempotentMethod.class),
        new IdempotentMethodInterceptor());
  }
}
