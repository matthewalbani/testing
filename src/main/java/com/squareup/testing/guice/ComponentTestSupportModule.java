/*
 * Copyright 2015, Square, Inc.
 */

package com.squareup.testing.guice;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * Installs a {@code Set<ComponentTestSupport>}.
 */
public class ComponentTestSupportModule extends AbstractModule {

  private final ImmutableList.Builder<ComponentTestSupport> supports = ImmutableList.builder();

  @Override protected void configure() {
    Multibinder<ComponentTestSupport> supportBinder
        = Multibinder.newSetBinder(binder(), ComponentTestSupport.class);
    for (ComponentTestSupport support : supports.build()) {
      supportBinder.addBinding().toInstance(support);
    }
  }

  /**
   * Add a {@link ComponentTestSupport} to the binding set.
   */
  public ComponentTestSupportModule addSupport(ComponentTestSupport support) {
    supports.add(support);
    return this;
  }
}
