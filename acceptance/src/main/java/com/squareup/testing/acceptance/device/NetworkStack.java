package com.squareup.testing.acceptance.device;

import com.squareup.testing.acceptance.IntegrationHelper;
import com.squareup.testing.acceptance.api.ApiBase;
import com.squareup.webservice.SimpleHttpClient;
import java.util.Map;

public interface NetworkStack {
  public <V, API extends ApiBase<V>> SimpleHttpClient.Response perform(API api,
      IntegrationHelper integrationHelper, Map<String, String> headers);
}
