package com.squareup.testing.acceptance.model;

public class Money {
  public int amount;
  public String currencyCode;

  public static Money of(int amount, String currencyCode) {
    Money money = new Money();
    money.amount = amount;
    money.currencyCode = currencyCode;
    return money;
  }
}
