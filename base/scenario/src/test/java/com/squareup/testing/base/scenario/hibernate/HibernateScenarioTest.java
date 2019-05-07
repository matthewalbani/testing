/*
 * Copyright 2015, Square, Inc.
 */

package com.squareup.testing.base.scenario.hibernate;

import com.squareup.integration.persistence.Id;
import com.squareup.integration.persistence.PersistentEntity;
import com.squareup.integration.persistence.Transacter;
import com.squareup.testing.base.scenario.Scenario;
import com.squareup.testing.persistence.PersistenceTestRunner;
import com.squareup.testing.persistence.TestDatasource;
import com.squareup.testing.persistence.TestPersistence;
import com.squareup.testing.persistence.TestUtils;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PersistenceTestRunner.class)
@TestPersistence(@TestDatasource(entities = HibernateScenarioTest.TestEntity.class))
public class HibernateScenarioTest {

  @Entity
  public static class TestEntity implements PersistentEntity {
    @javax.persistence.Id @GeneratedValue Id<TestEntity> id;
    @Column String name;

    @Override public Id<? extends PersistentEntity> getId() {
      return id;
    }
  }

  private Transacter transacter;
  private HibernateScenario scenario;
  @Mock private Scenario baseScenario;
  private TestEntity testEntity = new TestEntity();
  private Integer testInt = Integer.valueOf(1);

  @Before public void before(Transacter transacter) {
    this.transacter = transacter;
    scenario = new HibernateScenario(baseScenario, transacter);
  }

  @Test public void get_persistentEntity() {
    testEntity.name = "myTestEntity";
    TestUtils.save(transacter, testEntity);
    Mockito.when(scenario.get("testEntity", TestEntity.class)).thenReturn(testEntity);

    TestEntity updated = TestUtils.loadById(transacter, TestEntity.class, testEntity.id).get();
    updated.name = "updatedName";
    TestUtils.update(transacter, updated);

    TestEntity actual = scenario.get("testEntity", TestEntity.class);
    assertThat(actual.name).isEqualTo(updated.name);
  }

  @Test public void get_notPersistentEntity() {
    Mockito.when(scenario.get("testInt", Integer.class)).thenReturn(testInt);
    Integer actual = scenario.get("testInt", Integer.class);
    assertThat(actual).isEqualTo(testInt);
  }
}
