package com.squareup.testing.web;

import com.google.inject.AbstractModule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public final class WebDriverModule extends AbstractModule {
  @Override protected void configure() {
    // In Kochiku, use FirefoxDriver. While in development, use ChromeDriver.
    // ChromeDriver has a bug that makes it behave really slowly when run in headless mode.
    // See: https://code.google.com/p/chromium/issues/detail?id=224872
    // However, FirefoxDriver steals focus when it opens a window which is too painful to be
    // the default for development.
    String environment = System.getenv("ENVIRONMENT");
    if ("test".equals(environment)) {
      System.setProperty("webdriver.gecko.driver",
          "/data/app/kochiku-worker/shared/build-partition/bin/geckodriver");
      bind(WebDriver.class).to(CustomFirefoxDriver.class);
    } else {
      bind(WebDriver.class).to(ChromeDriver.class);
    }
  }
}

class CustomFirefoxDriver extends FirefoxDriver {
  public CustomFirefoxDriver() {
    super((new FirefoxOptions()).setBinary(
        "/data/app/kochiku-worker/shared/build-partition/bin/firefox/firefox"));
  }
}
