package com.squareup.testing.acceptance.model;

public class User {
  public int id;
  public String token;
  public String email;

  @Override public String toString() {
    return "User{" +
        "id=" + id +
        ", token='" + token + '\'' +
        ", email='" + email + '\'' +
        '}';
  }

  public static class FactoryRequest {
    public String name;
    public String email;
    public String password;
    //public String password_confirmation;
    public String business_type;
    public String[] permission_names;
    public String token;
  }
}
