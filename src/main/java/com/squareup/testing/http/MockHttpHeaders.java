// Copyright 2011 Square, Inc.
package com.squareup.testing.http;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import org.mockito.Mockito;

public class MockHttpHeaders {
  private MockHttpHeaders() {}

  public static HttpHeaders create() {
    return Mockito.mock(HttpHeaders.class);
  }

  public static HttpHeaders create(String headerName, String headerValue) {
    return create(ImmutableMap.of(headerName, headerValue));
  }

  public static HttpHeaders create(Map<String, String> headers) {
    HttpHeaders httpHeaders = create();
    for (Map.Entry<String, String> header : headers.entrySet()) {
      Mockito.when(httpHeaders.getRequestHeader(header.getKey())).thenReturn(
          header.getValue() == null
              ? null
              : ImmutableList.of(header.getValue()));
    }
    return httpHeaders;
  }
}
