package com.squareup.testing.acceptance.api;

import com.squareup.webservice.SimpleHttpClient;
import java.util.Arrays;

public class SquareApiBase<V extends SquareApiBase.Response> extends ApiBase<V> {
  public SquareApiBase() {
  }

  public SquareApiBase(String method, String uri) {
    super(method, uri);
  }

  @Override public V parseResponse(SimpleHttpClient.Response httpResponse) {
    V response = super.parseResponse(httpResponse);
    if (!response.success) {
      if (response.debug_info != null) {
        System.err.println(response.debug_info);
      }
      throw new RuntimeException(response.message);
    }
    return response;
  }

  @Override public Class<V> getResponseClass() {
    return (Class<V>) Response.class;
  }

  @Override public V reportStatusProblem(V response, int actualStatus) {
    if (response.debug_info != null) {
      System.err.println(response.debug_info);
    }
    throw new RuntimeException(
        (response.title == null ? "" : response.title + ": ") + response.message + "\n" +
            describe() + ": expected status in " + Arrays.toString(getExpectedStatusCodes())
            + " but got " + actualStatus);
  }

  public static class Response {
    boolean success;
    String title;
    String message;
    String debug_info;
  }
}
