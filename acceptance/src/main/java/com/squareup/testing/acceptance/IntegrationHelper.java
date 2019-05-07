package com.squareup.testing.acceptance;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.squareup.config.Env;
import com.squareup.config.SquareRoot;
import com.squareup.logging.Logger;
import com.squareup.testing.acceptance.api.ApiBase;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class IntegrationHelper {
  static final Logger LOGGER = Logger.getLogger(IntegrationHelper.class);

  private final Map<String, RemoteApp> remoteApps = new LinkedHashMap<>();
  private final IntegrationConfig integrationConfig;
  private final Set<String> integratedApps;

  public IntegrationHelper(SquareRoot squareRoot, Env env) {
    integrationConfig = squareRoot.getTestConfig(env, IntegrationConfig.class);
    integratedApps = new TreeSet<>();
    for (Map.Entry<String, IntegrationConfig.App> entry : integrationConfig.apps.entrySet()) {
      String appName = entry.getKey();
      IntegrationConfig.App appConfig = entry.getValue();

      URL testSupportUrl = null;
      URL statusUrl = null;

      String testSupportUriStr = appConfig.test_support_uri;
      if (testSupportUriStr != null) {
        testSupportUrl = newUrl(testSupportUriStr);
      }

      String statusUrlStr = appConfig.status_uri;
      if (statusUrlStr != null) {
        statusUrl = newUrl(statusUrlStr);
      }

      integratedApps.add(appName);
      remoteApps.put(appName, new RemoteApp(appName, statusUrl, testSupportUrl));
    }
  }

  public void checkAppConfiguration() {
    LOGGER.info("Integrating apps: %s", integratedApps);
    ensureAppsArePresent();
  }

  public void ensureAppsArePresent() {
    final long tryUntil = now() + 120 * 1000;
    final Set<String> waitingFor = new HashSet<>();
    final List<Thread> appWaiterThreads = new ArrayList<>();

    for (final RemoteApp remoteApp : remoteApps.values()) {
      if (remoteApp.providesStatus()) {
        waitingFor.add(remoteApp.getName());

        appWaiterThreads.add(new Thread() {
          @Override public void run() {
            boolean awaitingStatus = true;
            while (awaitingStatus) {
              try {
                remoteApp.checkStatus();
                awaitingStatus = false;
              } catch (IOException e) {
                if (now() < tryUntil) {
                  sleepFor(1000);
                } else {
                  throw new RuntimeException(e);
                }
              }
            }

            synchronized (waitingFor) {
              waitingFor.remove(remoteApp.getName());
            }
          }
        });
      }
    }

    final Thread reportingThread = new Thread() {
      @Override public void run() {
        while (now() < tryUntil && anyRemain()) {
          try {
            sleep(5000);
          } catch (InterruptedException e) {
            // cool...
          }

          String names = null;
          synchronized (waitingFor) {
            if (!waitingFor.isEmpty()) {
              names = Joiner.on(", ").join(waitingFor);
            }
          }
          if (names != null) LOGGER.info("Still waiting for %s to come online...", names);
        }
      }

      private boolean anyRemain() {
        synchronized (waitingFor) {
          return !waitingFor.isEmpty();
        }
      }
    };

    reportingThread.start();
    for (Thread appWaiterThread : appWaiterThreads) {
      appWaiterThread.start();
    }

    for (Thread appWaiterThread : appWaiterThreads) {
      try {
        appWaiterThread.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    reportingThread.interrupt();
  }

  public void onBeforeAll() {
    parallelize(getInstrumentedRemoteApps(), new Function<RemoteApp.Result>() {
      @Override public RemoteApp.Result apply(RemoteApp remoteApp, String phase) {
        return remoteApp.beforeAll(phase);
      }
    });
  }

  public void onBeforeEach() {
    parallelize(getInstrumentedRemoteApps(), new Function<RemoteApp.Result>() {
      @Override public RemoteApp.Result apply(RemoteApp remoteApp, String phase) {
        return remoteApp.beforeEach(phase);
      }
    });
  }

  public void onPause() {
    parallelize(getInstrumentedRemoteApps(), new Function<RemoteApp.Result>() {
      @Override public RemoteApp.Result apply(RemoteApp remoteApp, String phase) {
        return remoteApp.pause(phase);
      }
    });
  }

  public void onResume() {
    parallelize(getInstrumentedRemoteApps(), new Function<RemoteApp.Result>() {
      @Override public RemoteApp.Result apply(RemoteApp remoteApp, String phase) {
        return remoteApp.resume(phase);
      }
    });
  }

  public void onAfterEach() {
    parallelize(getInstrumentedRemoteApps(), new Function<RemoteApp.Result>() {
      @Override public RemoteApp.Result apply(RemoteApp remoteApp, String phase) {
        return remoteApp.afterEach(phase);
      }
    });
  }

  public void onAfterAll() {
    parallelize(getInstrumentedRemoteApps(), new Function<RemoteApp.Result>() {
      @Override public RemoteApp.Result apply(RemoteApp remoteApp, String phase) {
        return remoteApp.afterAll(phase);
      }
    });
  }

  public void onBefore() {
    onPause();
    onBeforeEach();
    onResume();
  }

  public void syncApps() {
    final AtomicInteger changes = new AtomicInteger(-1);
    while (changes.get() != 0) {
      changes.set(0);

      parallelize(getInstrumentedRemoteApps(), new Function<RemoteApp.Result>() {
        @Override public RemoteApp.Result apply(RemoteApp remoteApp, String phase) {
          RemoteApp.SyncResult syncResult = remoteApp.sync(phase);
          changes.addAndGet(syncResult.change_count);
          return syncResult;
        }
      });
    }
  }

  interface Function<T extends RemoteApp.Result> {
    T apply(RemoteApp remoteApp, String phase);
  }

  @VisibleForTesting
  <T extends RemoteApp.Result> void parallelize(Collection<RemoteApp> remoteApps,
      final Function<T> function) {
    final NavigableMap<String, Set<String>> phases = new TreeMap<>();
    phases.put("", null); // initial phase

    while (!phases.isEmpty()) {
      final String phase = phases.firstKey();
      final List<Thread> threads = new ArrayList<>();
      final Set<String> appsForThisPhase = phases.remove(phase);
      final List<Exception> thrownExceptions = new ArrayList<>();

      for (final RemoteApp remoteApp : remoteApps) {
        if (phase.isEmpty() || appsForThisPhase.contains(remoteApp.getName())) {
          threads.add(new Thread() {
            @Override public void run() {
              final T result;
              try {
                result = function.apply(remoteApp, phase);
              } catch (Exception e) {
                synchronized (thrownExceptions) {
                  thrownExceptions.add(e);
                }
                return;
              }

              if (result.phases != null && !result.phases.isEmpty()) {
                synchronized (phases) {
                  for (String requestedPhase : result.phases) {
                    if (requestedPhase.compareTo(phase) > 0) {
                      Set<String> appSet = phases.get(requestedPhase);
                      if (appSet == null) {
                        phases.put(requestedPhase, appSet = new HashSet<>());
                      }
                      appSet.add(remoteApp.getName());
                    }
                  }
                }
              }
            }
          });
        }
      }

      for (Thread thread : threads) {
        thread.start();
      }

      for (Thread thread : threads) {
        try {
          thread.join();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }

      if (thrownExceptions.size() > 0) {
        for (Exception thrownException : thrownExceptions) {
          thrownException.printStackTrace();
        }
        throw new RuntimeException("failed with exceptions from "
            + thrownExceptions.size() + " remote apps");
      }
    }
  }

  private long now() {
    return System.currentTimeMillis();
  }

  @VisibleForTesting List<RemoteApp> getInstrumentedRemoteApps() {
    ArrayList<RemoteApp> apps = new ArrayList<>();
    for (RemoteApp remoteApp : remoteApps.values()) {
      if (remoteApp.providesTestSupport()) {
        apps.add(remoteApp);
      }
    }
    return apps;
  }

  private static void sleepFor(int delay) {
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private URL newUrl(String testConfig) {
    URL testSupportUrl;
    try {
      testSupportUrl = new URL(
          testConfig);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    return testSupportUrl;
  }

  public URI remapUri(ApiBase apiBase) {
    URI uri = apiBase.getUri();
    try {
      URI remappedUri = new URI(getEndpointConfig(apiBase).uri);
      return new URI(remappedUri.getScheme(), remappedUri.getUserInfo(),
          remappedUri.getHost(), remappedUri.getPort(),
          uri.getPath(), uri.getQuery(), uri.getFragment());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public Set<String> getIntegratedApps() {
    return integratedApps;
  }

  public RemoteApp getRemoteApp(String appName) {
    RemoteApp remoteApp = remoteApps.get(appName);
    if (remoteApp == null) {
      RemoteApp.warnMissing(appName);
    }
    return remoteApp;
  }

  public String getServerCertificate(ApiBase apiBase) {
    return getEndpointConfig(apiBase).server_certificate;
  }

  private IntegrationConfig.Endpoint getEndpointConfig(ApiBase apiBase) {
    String host = apiBase.getUri().getHost();
    IntegrationConfig.Endpoint endpoint = integrationConfig.endpoints.get(host);
    if (endpoint == null) {
      throw new RuntimeException("no configuration for endpoints." + host + " for " + apiBase.getUri());
    }
    return endpoint;
  }
}
