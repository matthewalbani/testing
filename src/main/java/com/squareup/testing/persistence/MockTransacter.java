// Copyright 2014 by Square, Inc.
package com.squareup.testing.persistence;

import com.squareup.config.DatasourceConfig;
import com.squareup.integration.persistence.AbstractTransacter;
import com.squareup.integration.persistence.CallInUnitOfWork;
import com.squareup.integration.persistence.Session;
import java.sql.Connection;
import org.hibernate.stat.Statistics;
import org.joda.time.Duration;

/**
 * Transacter that always provides a null {@link Session} or {@link Connection}.
 * Useful for cases where you need to test a class that passes a {@link Session} to some other
 * method, and you just want to verify that the method was called. This class allows you to avoid
 * using a PersistenceTestRunner.
 */
public class MockTransacter extends AbstractTransacter {
  @Override public <T> T callReadOnly(String comment, CallInUnitOfWork<T> task) {
    return task.call(null);
  }

  @Override public <T> T callAutoCommitted(String comment, CallInUnitOfWork<T> task) {
    return task.call(null);
  }

  @Override public <T> T callWithRetries(String comment, int maxAttempts, int maxJitterMs,
      CallInUnitOfWork<T> task) {
    return task.call(null);
  }

  @Override public boolean inTransaction() {
    return false;
  }

  @Override public Duration getReplicationLag() {
    return null;
  }

  @Override public Statistics getStatistics() {
    return null;
  }

  @Override public DatasourceConfig.DatabaseType getType() {
    return null;
  }
}
