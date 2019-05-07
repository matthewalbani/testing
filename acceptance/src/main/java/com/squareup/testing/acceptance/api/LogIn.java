package com.squareup.testing.acceptance.api;

public class LogIn extends SquareApiBase<LogIn.Response> {
  public LogIn(String email, String password) {
    super("POST", "https://api.squareup.com/1.0/login");
    setParam("email", email);
    setParam("password", password);
  }

  @Override public Class<Response> getResponseClass() {
    return Response.class;
  }

  @Override public int[] getExpectedStatusCodes() {
    return new int[] {200, 201};
  }

  public static class Response extends SquareApiBase.Response {
    public String session_token;
    public String user_token;
  }
}
