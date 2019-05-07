package com.squareup.testing.acceptance.model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatterBuilder;

public class PaymentCard {
  private static final String[] PANS = {
      "4221784841888534", "4226731724262951", "4211451818528988", "4299755242091933",
      "4216293947362524", "4229643892758461", "4241102779251110", "4212046567664213",
      "4231227802065914", "4271254160459613", "4260436351510774", "4277173104988590"
  };

  public String pan;
  String expiration;

  private static String validPan() {
    return PANS[((int) (Math.random() * PANS.length))];
  }

  private static String validExpiration() {
    return new DateTime(System.currentTimeMillis() + 60 * 60 * 24 * 356).toString(
        new DateTimeFormatterBuilder().appendYearOfCentury(2, 2).appendMonthOfYear(2).toFormatter());
  }

  public static PaymentCard valid() {
    return new PaymentCard(validPan(), validExpiration());
  }

  public PaymentCard(String pan, String expiration) {
    this.pan = pan;
    this.expiration = expiration;
  }

  public String track2() {
    if (expiration.length() != 4) {
      throw new RuntimeException("invalid expiration " + expiration + ", should be YYMM");
    }
    return String.format(";%s=%s999SECRETS?", pan, expiration);
  }
}
