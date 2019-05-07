package com.squareup.testing.acceptance;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.squareup.testing.acceptance.device.HttpNetworkStack;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;

import static java.lang.String.format;

public class RemoteApp {
  public static final Gson GSON = new GsonBuilder().create();
  private final String name;
  private final URL statusUrl;
  private final URL testSupportUrl;
  private HttpNetworkStack networkStack;

  public RemoteApp(String name, URL statusUrl, URL testSupportUrl) {
    this.name = name;
    this.testSupportUrl = testSupportUrl;
    this.statusUrl = statusUrl;
    networkStack = new HttpNetworkStack();
  }

  public static void warnMissing(String appName) {
    throw new RuntimeException(
        "you need to add " + appName + " as a SquareRoot-managed app: sq add " + appName);
  }

  public String getName() {
    return name;
  }

  public boolean providesTestSupport() {
    return testSupportUrl != null;
  }

  public boolean providesStatus() {
    return statusUrl != null;
  }

  private URL getTestSupportUrl() {
    return testSupportUrl;
  }

  void checkStatus() throws IOException {
    StatusResult statusResult = GSON.fromJson(doRequest("GET", statusUrl, "status", "",
        "application/x-www-form-urlencoded"), StatusResult.class);
    if (!statusResult.ok) {
      throw new RuntimeException(name + " status check failed: " + statusResult.message);
    }
  }

  public Result beforeAll(String phase) {
    return checkJsonSuccess(request("POST", "before-all", phase), Result.class);
  }

  public Result beforeEach(String phase) {
    return checkJsonSuccess(request("POST", "before-each", phase), Result.class);
  }

  public Result pause(String phase) {
    return checkJsonSuccess(request("POST", "pause", phase), Result.class);
  }

  public Result resume(String phase) {
    return checkJsonSuccess(request("POST", "resume", phase), Result.class);
  }

  public Result afterEach(String phase) {
    return checkJsonSuccess(request("POST", "after-each", phase), Result.class);
  }

  public Result afterAll(String phase) {
    return checkJsonSuccess(request("POST", "after-all", phase), Result.class);
  }

  public SyncResult sync(String phase) {
    return checkJsonSuccess(request("POST", "sync", phase), SyncResult.class);
  }

  public String call(String method, String action) {
    return request(method, "/_test-support/" + action);
  }

  public <T> T build(String factoryName, Object prototype, Class<T> resultType) {
    FactoryRequest factoryRequest = new FactoryRequest();
    FactoryRequest.Item item = new FactoryRequest.Item();
    item.name = "item";
    item.factory_name = factoryName;
    item.prototype = prototype;
    factoryRequest.items.add(item);

    String body;
    try {
      body = "json=" + URLEncoder.encode(GSON.toJson(factoryRequest), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    FactoryResult factoryResult = checkJsonSuccess(
        request("POST", "build", body, "application/x-www-form-urlencoded"), FactoryResult.class);
    return GSON.fromJson(GSON.toJson(factoryResult.items.get("item")), resultType);
  }

  public URL getStatusUrl() {
    return statusUrl;
  }

  static class FactoryRequest {
    List<Item> items = new ArrayList<Item>();

    static class Item {
      String name;
      String factory_name;
      Object prototype;
    }
  }

  static class FactoryResult extends Result {
    Map<String, Object> items;
  }

  private String request(String method, String action, String phase) {
    return request(method, action, "phase=" + urlEncode(phase), "application/x-www-form-urlencoded");
  }

  private String urlEncode(String phase) {
    try {
      return URLEncoder.encode(phase, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private String request(String method, String action) {
    return request(method, action, "", "application/x-www-form-urlencoded");
  }

  private String request(String method, final String action, String body, String contentType) {
    URL url;
    try {
      url = new URL(getTestSupportUrl(), action);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }

    try {
      return doRequest(method, url, action, body, contentType);
    } catch (IOException e) {
      throw new RuntimeException("Couldn't perform " + action + " on " + name, e);
    }
  }

  private String doRequest(String method, URL url, final String action, String body,
      String contentType) throws IOException {
    HttpURLConnection connection = null;
    final boolean[] running = {true};
    Thread reportingThread = new Thread() {
      final long startTime = System.currentTimeMillis();

      @Override public void run() {
        while (running[0]) {
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            // ok cool
          }

          if (running[0]) {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            IntegrationHelper.LOGGER.info("[%s] waiting on %s for %ds", name, action, elapsed);
          }
        }
      }
    };
    reportingThread.start();

    try {
      IntegrationHelper.LOGGER.debug("[%s] connecting to %s", name, url);

      connection = (HttpURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.setInstanceFollowRedirects(false);
      connection.setRequestMethod(method);
      connection.setUseCaches(false);
      if (method.equals("POST")) {
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", contentType);
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(body.getBytes().length));

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(body);
        wr.flush();
        wr.close();
      }

      try {
        return IOUtils.toString(connection.getInputStream(), Charsets.UTF_8);
      } catch (IOException e) {
        InputStream errorStream = connection.getErrorStream();
        if (errorStream != null) {
          String resultString = IOUtils.toString(errorStream, Charsets.UTF_8);

          Result result = null;
          try {
            result = GSON.fromJson(resultString, Result.class);
          } catch (JsonSyntaxException e1) {
          }

          String message = format("Error fetching %s on %sw:\n%s\n%s", url, name,
              result != null ? result.message : resultString,
              result != null ? result.trace : "no trace available");
          throw new RuntimeException(message, e);
        } else {
          throw e;
        }
      }

    } finally {
      running[0] = false;

      if (connection != null) {
        connection.disconnect();
      }

      reportingThread.interrupt();
    }
  }

  private <T extends Result> T checkJsonSuccess(String resultString, Class<T> clazz) {
    T result;

    try {
      result = GSON.fromJson(resultString, clazz);
    } catch (JsonParseException e) {
      String message = format("Failed to parse as JSON:\n%s", resultString);
      throw new RuntimeException(message, e);
    }

    if (!result.success) {
      throw new RuntimeException(name + " failed: " + result.message);
    }

    return result;
  }

  public static class Result {
    public boolean success;
    public String message;
    public String trace;
    public List<String> phases = new ArrayList<String>();
  }

  private static class StatusResult extends Result {
    boolean ok;
  }

  public static class SyncResult extends Result {
    public int change_count;
  }
}
