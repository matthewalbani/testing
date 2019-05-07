package com.squareup.testing.acceptance;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.squareup.config.Env;
import com.squareup.config.EnvFactory;
import com.squareup.config.SquareRoot;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntegrationHelperTest {
  final SquareRoot squareRoot = mock(SquareRoot.class);
  final List<String> log = new ArrayList<String>();
  RemoteApp app1 = new RemoteApp("app1", null, null);
  RemoteApp app2 = new RemoteApp("app2", null, null);
  IntegrationHelper integrationHelper;

  @Before
  public void setUp() throws Exception {
    when(squareRoot.getTestConfig(any(Env.class), any(Class.class)))
        .thenReturn(new IntegrationConfig());
    integrationHelper = new IntegrationHelper(squareRoot, EnvFactory.getTestEnv()) {
      @Override List<RemoteApp> getInstrumentedRemoteApps() {
        return asList(app1, app2);
      }
    };
  }

  @Test public void parallelize_shouldAcceptNullPhases() throws Exception {
    integrationHelper.parallelize(asList(app1, app2), new PhaseReturner(ImmutableMap.of("", new String[0])) {
      @Override public RemoteApp.Result apply(RemoteApp remoteApp, String phase) {
        RemoteApp.Result result = super.apply(remoteApp, phase);
        result.phases = null;
        return result;
      }
    });
    assertThat(Joiner.on(", ").join(log)).matches("^app[12]:--default--, app[12]:--default--$");
  }

  @Test public void parallelize_shouldStartWithDefaultPhase() throws Exception {
    final ImmutableMap<String, String[]> answers = ImmutableMap.of(
        "app1:--default--", new String[] {},
        "app2:--default--", new String[] {}
    );
    integrationHelper.parallelize(asList(app1, app2), new PhaseReturner(answers));
    assertThat(Joiner.on(", ").join(log)).matches("^app[12]:--default--, app[12]:--default--$");
  }

  @Test public void parallelize_shouldCallBackWithAdditionalPhasesInLexOrder() throws Exception {
    final ImmutableMap<String, String[]> answers = ImmutableMap.of(
        "app1:--default--", new String[] {"phase2", "phase1", "phase4"},
        "app2:--default--", new String[] {"phase2", "phase1", "phase3"}
    );
    integrationHelper.parallelize(asList(app1, app2), new PhaseReturner(answers));
    assertThat(Joiner.on(", ").join(log)).matches(
        "^app[12]:--default--, app[12]:--default--, "
            + "app[12]:phase1, app[12]:phase1, "
            + "app[12]:phase2, app[12]:phase2, "
            + "app2:phase3, "
            + "app1:phase4$");
  }

  @Test public void parallelize_shouldAllowAnyPhaseToRequestAnotherPhase() throws Exception {
    final ImmutableMap<String, String[]> answers = ImmutableMap.of(
        "app1:--default--", new String[] {"phase1"},
        "app2:--default--", new String[] {"phase2", "phase1"},
        "app1:phase1", new String[] {"phase2", "phase3"}
    );
    integrationHelper.parallelize(asList(app1, app2), new PhaseReturner(answers));
    assertThat(Joiner.on(", ").join(log)).matches(
        "^app[12]:--default--, app[12]:--default--, "
            + "app[12]:phase1, app[12]:phase1, "
            + "app[12]:phase2, app[12]:phase2, "
            + "app1:phase3$");
  }

  @Test public void parallelize_shouldIgnoreAdditionalPhasesThatShouldHaveComeEarlier() throws Exception {
    final ImmutableMap<String, String[]> answers = ImmutableMap.of(
        "app1:--default--", new String[] {"phase2"},
        "app1:phase2", new String[] {"phase1", "phase2"}
    );
    integrationHelper.parallelize(asList(app1, app2), new PhaseReturner(answers));
    assertThat(Joiner.on(", ").join(log)).matches(
        "^app[12]:--default--, app[12]:--default--, "
            + "app1:phase2$");
  }

  @Test public void syncApps_whenRunningRequestedPhase_changeCountShouldTriggerPhaselessSync() throws Exception {
    app1 = new RemoteApp("app1", null, null) {
      boolean ranJob = false;

      @Override public SyncResult sync(String phase) {
        log(this, phase);
        SyncResult syncResult = new SyncResult();
        if (phase.isEmpty()) {
          if (!ranJob) {
            syncResult.phases.add("run_job_at_1234");
          }
        } else {
          ranJob = true;
          syncResult.change_count = 1;
        }
        return syncResult;
      }
    };
    app2 = new RemoteApp("app2", null, null) {
      @Override public SyncResult sync(String phase) {
        log(this, phase);
        return new SyncResult();
      }
    };

    integrationHelper.syncApps();
    assertThat(Joiner.on(", ").join(log)).matches(
        "^app[12]:--default--, app[12]:--default--, "
            + "app1:run_job_at_1234, "
            + "app[12]:--default--, app[12]:--default--");
  }

  private class PhaseReturner implements IntegrationHelper.Function<RemoteApp.Result> {
    private final ImmutableMap<String, String[]> answers;

    public PhaseReturner(ImmutableMap<String, String[]> answers) {
      this.answers = answers;
    }

    @Override public RemoteApp.Result apply(RemoteApp remoteApp, String phase) {
      String key = log(remoteApp, phase);
      RemoteApp.Result result = new RemoteApp.Result();
      String[] phases = answers.get(key);
      result.phases = asList(phases == null ? new String[0] : phases);
      sleep((long) (Math.random() * 10));
      return result;
    }
  }

  private String log(RemoteApp remoteApp, String phase) {
    String key = remoteApp.getName() + ":" + (phase.isEmpty() ? "--default--" : phase);
    synchronized (log) {
      log.add(key);
    }
    return key;
  }

  private static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
