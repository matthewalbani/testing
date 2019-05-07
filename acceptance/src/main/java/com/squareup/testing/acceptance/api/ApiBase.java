package com.squareup.testing.acceptance.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.webservice.SimpleHttpClient;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class ApiBase<V> {
  protected static final Gson GSON = new GsonBuilder().create();

  private final String method;
  private final String uri;
  private final HashMap<String, Object> params = new HashMap<String, Object>();

  public ApiBase() {
    method = null;
    uri = null;
  }

  public ApiBase(String method, String uri) {
    this.method = method;
    this.uri = uri;
  }

  public String getMethod() {
    if (method == null) return "GET";
    return method;
  }

  public URI getUri() {
    if (method == null) throw new RuntimeException("no url given!");
    return URI.create(uri);
  }

  public void setParam(String name, Object value) {
    params.put(name, value);
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public String describe() {
    return getUri().toString();
  }

  public V parseResponse(SimpleHttpClient.Response httpResponse) {
    V response = responseFromJson(httpResponse, getResponseClass());
    int actualStatus = httpResponse.getStatus();
    if (!statusCodeMatches(actualStatus)) {
      reportStatusProblem(response, actualStatus);
    }

    return response;
  }

  public boolean statusCodeMatches(int actualStatus) {
    boolean statusMatched = false;
    for (int expectedStatusCode : getExpectedStatusCodes()) {
      if (expectedStatusCode == actualStatus) {
        statusMatched = true;
        break;
      }
    }
    return statusMatched;
  }

  public V reportStatusProblem(V response, int actualStatus) {
    throw new RuntimeException(describe()
        + ": expected status in "
        + Arrays.toString(getExpectedStatusCodes())
        + " but got "
        + actualStatus);
  }

  public abstract Class<V> getResponseClass();

  public <VR> VR responseFromJson(SimpleHttpClient.Response response, Class<VR> responseClass) {
    String body = response.getStringEntity();
    System.out.println("body = " + body);
    return GSON.fromJson(body, responseClass);
  }

  public int[] getExpectedStatusCodes() {
    return new int[] {200};
  }
}
