// Copyright 2014 by Square, Inc.
package com.squareup.testing.base.scenario.hibernate;

import com.squareup.integration.persistence.PersistentEntity;
import com.squareup.integration.persistence.Transacter;
import com.squareup.testing.base.scenario.InstanceBuilder;
import com.squareup.testing.base.scenario.Scenario;
import com.squareup.testing.base.scenario.ScenarioBuilder;
import com.squareup.testing.persistence.TestUtils;

/**
 * A {@link ScenarioBuilder} that persists all instances of type {@link PersistentEntity} after
 * creating them.
 */
public class HibernateScenarioBuilder extends ScenarioBuilder {
  private final Transacter transacter;

  public HibernateScenarioBuilder(Transacter transacter) {
    super();
    this.transacter = transacter;
  }

  @Override
  protected <T> T invokeInstanceBuilder(InstanceBuilder<T> instanceBuilder) {
    T instance = super.invokeInstanceBuilder(instanceBuilder);
    if (instance instanceof PersistentEntity) {
      TestUtils.save(transacter, (PersistentEntity)instance);
    }
    return instance;
  }

  @Override protected Scenario buildFromCache() {
    return new HibernateScenario(super.buildFromCache(), transacter);
  }
}
