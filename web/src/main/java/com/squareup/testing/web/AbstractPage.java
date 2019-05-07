package com.squareup.testing.web;

import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class AbstractPage<P extends AbstractPage<P>> {
  protected final WebDriver driver;
  protected final String baseUrl;

  protected AbstractPage(WebDriver driver, String baseUrl) {
    this.driver = driver;
    this.baseUrl = baseUrl;
  }

  @SuppressWarnings("unchecked") // By convention, 'P' is the subclass type.
  protected P thisPage() {
    return (P) this;
  }

  protected P type(String id, String value) {
    return type(By.id(id), value);
  }

  protected P type(By by, String value) {
    waitForInteractability(by, 1, TimeUnit.MINUTES);
    driver.findElement(by).clear();
    driver.findElement(by).sendKeys(value);
    return thisPage();
  }

  protected P waitForAlert() {
    return waitForAlert(1, TimeUnit.MINUTES);
  }

  protected P waitForAlert(long timeout, TimeUnit timeUnit) {
    WebDriverWait webDriverWait = new WebDriverWait(driver, timeUnit.toSeconds(timeout));
    webDriverWait.until(ExpectedConditions.alertIsPresent());
    return thisPage();
  }

  protected P waitForElementCount(By locator, int count) {
    return waitForElementCount(locator, count, 1, TimeUnit.MINUTES);
  }

  protected P waitForElementCount(
      final By locator, final int count, long timeout, TimeUnit timeUnit) {
    WebDriverWait webDriverWait = new WebDriverWait(driver, timeUnit.toSeconds(timeout));
    webDriverWait.until(conditionCount(locator, count));
    return thisPage();
  }

  private static ExpectedCondition<List<WebElement>> conditionCount(final By locator,
      final int count) {
    return new ExpectedCondition<List<WebElement>>() {
      @Override public List<WebElement> apply(WebDriver input) {
        List<WebElement> elements = input.findElements(locator);
        if (elements.size() == count) {
          return elements;
        }
        return null;
      }
    };
  }

  protected P waitFor(By locator, long timeout, TimeUnit timeUnit) {
    WebDriverWait webDriverWait = new WebDriverWait(driver, timeUnit.toSeconds(timeout));
    webDriverWait.until(ExpectedConditions.presenceOfElementLocated(locator));
    return thisPage();
  }

  protected P waitFor(By locator) {
    return waitFor(locator, 1, TimeUnit.MINUTES);
  }

  protected P waitForVisible(By locator, long timeout, TimeUnit timeUnit) {
    WebDriverWait webDriverWait = new WebDriverWait(driver, timeUnit.toSeconds(timeout));
    webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    return thisPage();
  }

  protected P waitForVisible(By locator) {
    return waitForVisible(locator, 1, TimeUnit.MINUTES);
  }

  protected P waitForInteractability(final By locator, long timeout, TimeUnit timeUnit) {
    WebDriverWait webDriverWait = new WebDriverWait(driver, timeUnit.toSeconds(timeout));
    ExpectedCondition<WebElement> expectedCondition = new ExpectedCondition<WebElement>() {
      @Nullable @Override public WebElement apply(@Nullable WebDriver input) {
        WebElement element = findElement(locator);
        if (element == null) {
          return null;
        }

        if (!element.isDisplayed() || !element.isEnabled()) {
          return null;
        }

        return element;
      }
    };

    webDriverWait.until(expectedCondition);
    return thisPage();
  }

  protected P waitForInteractability(final By locator) {
    return waitForInteractability(locator, 1, TimeUnit.MINUTES);
  }

  protected P clickElement(By by) {
    waitForInteractability(by);
    driver.findElement(by).click();
    return thisPage();
  }

  protected String elementText(By by) {
    waitForVisible(by);
    return driver.findElement(by).getText();
  }

  protected String elementValue(By by) {
    waitForVisible(by);
    return driver.findElement(by).getAttribute("value");
  }

  private WebElement findElement(By by) {
    try {
      return driver.findElement(by);
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  public String body() {
    return driver.findElement(By.tagName("body")).getText();
  }

  public String source() {
    return driver.getPageSource();
  }
}
