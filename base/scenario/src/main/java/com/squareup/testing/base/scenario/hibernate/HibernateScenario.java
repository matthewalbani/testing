/*
 * Copyright 2015, Square, Inc.
 */

package com.squareup.testing.base.scenario.hibernate;

import com.google.common.reflect.TypeToken;
import com.squareup.integration.persistence.PersistentEntity;
import com.squareup.integration.persistence.Transacter;
import com.squareup.testing.base.scenario.Scenario;
import com.squareup.testing.persistence.TestUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link Scenario} that provides additional functionality for Hibernate {@link PersistentEntity}s.
 */
public class HibernateScenario implements Scenario {

  private final Scenario baseScenario;
  private final Transacter trasacter;

  HibernateScenario(Scenario baseScenario, Transacter trasacter) {
    this.baseScenario = checkNotNull(baseScenario);
    this.trasacter = checkNotNull(trasacter);
  }

  /**
   * Additionally reloads any hibernate entities from the persistence layer.
   */
  @Override public <T> T get(String name, TypeToken<T> type) {
    T entity = baseScenario.get(name, type);
    if (entity instanceof PersistentEntity) {
      return (T) TestUtils.reload(trasacter, (PersistentEntity) entity);
    }
    return entity;
  }
}
