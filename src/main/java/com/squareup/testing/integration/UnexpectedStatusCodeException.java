package com.squareup.testing.integration;

import org.apache.http.HttpResponse;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class UnexpectedStatusCodeException extends RuntimeException {
  private final HttpResponse response;
  private final String content;

  public UnexpectedStatusCodeException(HttpResponse response, String content) {
    this.response = response;
    this.content = content.replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");
  }

  public int getStatusCode() {
    return response.getStatusLine().getStatusCode();
  }

  public String getContent() {
    return content;
  }

  @Override
  public String getMessage() {
    return "Status: " + response.getStatusLine().getStatusCode() + " Response: " + content;
  }

  public static Matcher<?> matcher(final int status) {
    return new BaseMatcher() {
      @Override public boolean matches(Object o) {
        if (UnexpectedStatusCodeException.class.isAssignableFrom(o.getClass())) {
          return ((UnexpectedStatusCodeException) o).getStatusCode() == status;
        }
        return false;
      }

      @Override public void describeTo(Description description) {
        description.appendText("A status equal to " + status + ".");
      }
    };
  }

  public static Matcher<?> matcher(final int status, final String message) {
    return new BaseMatcher() {
      @Override public boolean matches(Object o) {
        if (UnexpectedStatusCodeException.class.isAssignableFrom(o.getClass())) {
          UnexpectedStatusCodeException usce = (UnexpectedStatusCodeException) o;
          return usce.getStatusCode() == status && usce.getContent().contains(message);
        }
        return false;
      }

      @Override public void describeTo(Description description) {
        description.appendText("A status equal to " + status + ".");
      }
    };
  }
}
