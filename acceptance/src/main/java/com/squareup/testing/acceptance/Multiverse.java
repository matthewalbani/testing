package com.squareup.testing.acceptance;

import com.google.common.collect.ImmutableSet;
import com.squareup.config.Env;
import com.squareup.config.EnvFactory;
import com.squareup.config.SquareRoot;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class Multiverse {
  private final Env ACCEPTANCE_ENV;
  private final SquareRoot SQUARE_ROOT;
  private IntegrationHelper integrationHelper;

  public Multiverse() {
    ACCEPTANCE_ENV = EnvFactory.getAcceptanceEnv();
    SQUARE_ROOT = SquareRoot.find();

    if (SQUARE_ROOT == null) {
      throw new RuntimeException("couldn't find your Square Root in "
          + new File(".").getAbsoluteFile());
    }
  }

  public void integrate(List<String> appNames) {
    integrate(ImmutableSet.copyOf(appNames));
  }

  public void integrate(Set<String> appNames) {
    if (integrationHelper != null && !appNames.equals(
        integrationHelper.getIntegratedApps())) {
      integrationHelper.onAfterAll();
      integrationHelper = null;
    }

    if (integrationHelper == null) {
      integrationHelper = new IntegrationHelper(SQUARE_ROOT, ACCEPTANCE_ENV);
      Set<String> currentApps = integrationHelper.getIntegratedApps();

      System.out.println("We want " + appNames);
      System.out.println("We have " + integrationHelper.getIntegratedApps());

      if (!appNames.equals(currentApps)) {
        setUpIntegration(appNames);

        integrationHelper = new IntegrationHelper(SQUARE_ROOT, ACCEPTANCE_ENV);
        currentApps = integrationHelper.getIntegratedApps();
        if (!appNames.equals(currentApps)) {
          throw new RuntimeException("wrong set of integrated apps: want " + appNames
              + " but got " + currentApps);
        }
      }

      integrationHelper.checkAppConfiguration();
      integrationHelper.onBeforeAll();
    }
  }

  private void setUpIntegration(Set<String> appNames) {
    File integrationTempFile = null;
    try {
      integrationTempFile = File.createTempFile("integration", ".yaml");
      integrationTempFile.deleteOnExit();

      MultiverseConfig multiverseConfig = new MultiverseConfig();
      for (String appName : appNames) {
        multiverseConfig.active_apps.put(appName, new MultiverseConfig.App());
      }
      FileWriter writer = new FileWriter(integrationTempFile);
      new Yaml(new Representer() {
        @Override
        protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
          MappingNode result = super.representJavaBean(properties, javaBean);
          result.setTag(Tag.MAP); // treat java beans just like maps
          return result;
        }
      }).dump(multiverseConfig, writer);

      // todo: maybe run a Square Root server on a well-known port and scrap all this...
      System.out.println("Executing sq integrate!");
      IntegrationConfig integrationConfig =
          SQUARE_ROOT.getTestConfig(ACCEPTANCE_ENV, IntegrationConfig.class);

      ProcessBuilder processBuilder = new ProcessBuilder(
          integrationConfig.sq_wrapper,
          "integrate", integrationTempFile.getPath()
      );
      processBuilder.environment().put("SQUARE_ROOT_ENVS", ACCEPTANCE_ENV.getName());
      processBuilder.directory(new File(integrationConfig.sq_root));

      Process process = processBuilder.start();
      new StreamGobbler(process.getInputStream(), System.out).start();
      new StreamGobbler(process.getErrorStream(), System.err).start();
      process.waitFor();
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      if (integrationTempFile != null) {
        integrationTempFile.delete();
      }
    }
  }

  public void onBeforeEach() {
    integrationHelper.onBefore();
  }

  public void onAfterEach() {
    integrationHelper.onAfterEach();
  }

  public void shutDown() {
    if (integrationHelper != null) {
      integrationHelper.onAfterAll();
      integrationHelper = null;
    }
  }

  public RemoteApp getRemoteApp(String appName) {
    return integrationHelper.getRemoteApp(appName);
  }

  public IntegrationHelper getIntegrationHelper() {
    return integrationHelper;
  }

  public static class MultiverseConfig {
    public Map<String, App> active_apps = new HashMap<String, App>();

    public static class App {
      public String target = "master";
    }
  }

  class StreamGobbler extends Thread {
    private final InputStream in;
    private final PrintStream out;

    // reads everything from is until empty.
    StreamGobbler(InputStream in, PrintStream out) {
      this.in = in;
      this.out = out;
    }

    @Override public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
          out.println(line);
        }
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }
}
