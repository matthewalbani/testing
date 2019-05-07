// Copyright 2013, Square, Inc.

package com.squareup.testing.guice;

import com.squareup.common.guice.SimpleScope;
import com.squareup.core.guice.InstallOnceModule;
import com.squareup.testing.TestScoped;

/** Module binding the {@link TestScoped} scope. */
public class TestScopeModule extends InstallOnceModule {
  @Override protected void configure() {
    bindScope(TestScoped.class, new SimpleScope());
  }
}
