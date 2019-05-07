package com.squareup.testing.acceptance.device;

import com.google.common.collect.ImmutableMap;
import com.squareup.testing.acceptance.IntegrationHelper;
import com.squareup.testing.acceptance.api.ApiBase;
import com.squareup.webservice.SimpleHttpClient;
import java.util.Map;

public class Device {
  private final NetworkStack networkStack;
  private final IntegrationHelper integrationHelper;

  public Device(NetworkStack networkStack, IntegrationHelper integrationHelper) {
    this.networkStack = networkStack;
    this.integrationHelper = integrationHelper;
  }

  public Map<String, String> getHeaders() {
    return ImmutableMap.of();
  }

  public <V, API extends ApiBase<V>> V perform(API api) {
    // todo: we could optimize things for certain commands that are guaranteed to have no side-effects...
    integrationHelper.syncApps();
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    SimpleHttpClient.Response response = networkStack.perform(api, integrationHelper, getHeaders());
    return api.parseResponse(response);
  }
}
