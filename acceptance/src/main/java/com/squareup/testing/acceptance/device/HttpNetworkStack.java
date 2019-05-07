package com.squareup.testing.acceptance.device;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.logging.Logger;
import com.squareup.testing.acceptance.IntegrationHelper;
import com.squareup.testing.acceptance.api.ApiBase;
import com.squareup.webservice.SimpleHttpClient;
import com.squareup.webservice.SimpleHttpClientFactory;
import com.squareup.webservice.SimpleHttpClientParams;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import javax.net.ssl.HandshakeCompletedListener;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpNetworkStack implements NetworkStack {
  private static final Logger LOGGER = Logger.getLogger(HttpNetworkStack.class);
  private static final Gson GSON = new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .create();

  @Override
  public <V, API extends ApiBase<V>> SimpleHttpClient.Response perform(API api,
      IntegrationHelper integrationHelper, Map<String, String> headers) {
    final String method = api.getMethod();
    final URI overrideUri = integrationHelper.remapUri(api);
    final String json = GSON.toJson(api.getParams());
    final String action = api.getClass().getSimpleName();

    final byte[] body = json.getBytes(UTF_8);
    final ContentType contentType = ContentType.APPLICATION_JSON;

    final long startTime = System.currentTimeMillis();
    final boolean[] running = {true};
    final Thread reportingThread = new Thread() {
      @Override public void run() {
        while (running[0]) {
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            // ok cool
          }

          if (running[0]) {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            LOGGER.info("[%s] waiting on %s for %ds", HttpNetworkStack.class.getName(), action,
                elapsed);
          }
        }
      }
    };
    reportingThread.start();

    SimpleHttpClientParams httpParams = new SimpleHttpClientParams();
    httpParams.host = overrideUri.getHost();
    httpParams.scheme = overrideUri.getScheme();
    httpParams.port = portFor(overrideUri);
    httpParams.server_certificate = integrationHelper.getServerCertificate(api);

    // todo: add cert/key setting for third_party_clients e.g. for siren

    SimpleHttpClient httpClient = SimpleHttpClientFactory.create("external", httpParams, true,
        Arrays.<HandshakeCompletedListener>asList());
    LOGGER.info("%s %s", method, overrideUri);

    HttpUriRequest request;
    if (method.equals("GET")) {
      request = new HttpGet(overrideUri);
    } else if (method.equals("POST")) {
      LOGGER.info("%s", json);
      HttpPost httpPost = new HttpPost(overrideUri);
      httpPost.setEntity(new ByteArrayEntity(body, contentType));
      request = httpPost;
    } else {
      throw new RuntimeException("unsupported method " + method);
    }

    for (Map.Entry<String, String> entry : headers.entrySet()) {
      request.addHeader(entry.getKey(), entry.getValue());
    }

    try {
      SimpleHttpClient.Response response = httpClient.send(request);
      LOGGER.info("%s -> %d in %.2fs", api.describe(), response.getStatus(),
          (System.currentTimeMillis() - startTime) / 1000f);
      return response;
    } catch (IOException e) {
      throw new RuntimeException("error during " + api.describe(), e);
    } finally {
      running[0] = false;
      reportingThread.interrupt();
    }
  }

  public static int portFor(URI uri) {
    int port = uri.getPort();
    return port != -1 ? port : toUrl(uri).getDefaultPort();
  }

  private static URL toUrl(URI overrideUri) {
    try {
      return overrideUri.toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
