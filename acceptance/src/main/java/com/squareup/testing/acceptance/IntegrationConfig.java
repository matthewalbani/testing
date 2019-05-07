package com.squareup.testing.acceptance;

import java.util.HashMap;
import java.util.Map;

public class IntegrationConfig {
  public Map<String, IntegrationConfig.App> apps = new HashMap<>();
  public Map<String, IntegrationConfig.Endpoint> endpoints = new HashMap<>();
  public String ca_cert;
  public String sq_wrapper;
  public String sq_root;

  public static class App {
    public String app_installed;
    public String status_uri;
    public String test_support_uri;
  }

  public static class Endpoint {
    public String uri;
    public String server_certificate;
  }
}
