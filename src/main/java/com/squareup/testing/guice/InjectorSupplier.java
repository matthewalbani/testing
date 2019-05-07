// Copyright 2013, Square, Inc.

package com.squareup.testing.guice;

import com.google.inject.Injector;

/**
 * Supplier of injector for {@link InjectionTestRunner}.
 *
 * @see TestInjector
 */
public interface InjectorSupplier {
  Injector getInjector();
}
