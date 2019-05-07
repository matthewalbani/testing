package com.squareup.testing.acceptance.api;

import com.squareup.common.values.Money;
import com.squareup.testing.acceptance.model.Itemization;
import com.squareup.testing.acceptance.model.PaymentCard;
import java.util.List;

public class Payments {
  private Payments() {
  }

  public static class Authorize extends SquareApiBase<Authorize.Response> {
    public Authorize(Money amount, PaymentCard paymentCard) {
      super("POST", "https://api.squareup.com/1.0/payments/authorize");

      setParam("track_2", paymentCard.track2());
      setParam("amount_cents", amount.cents());
      setParam("currency", amount.currency().getAlpha3Code());
    }

    @Override public Class<Response> getResponseClass() {
      return Response.class;
    }

    public static class Response extends SquareApiBase.Response {
      public String authorization_id;
      public Payer payer;
    }
  }

  public static class Payer {
    public String name;
    public String email_id;
    public String obfuscated_email;
    public String phone_id;
    public String obfuscated_phone;
    public String payer_image_url;
    public String auto_send_receipt;
  }

  static class LineItems {
    List<Itemization> itemizations;
    String payment_id;
  }

  public static class Capture extends SquareApiBase<Capture.Response> {
    public Capture(String authorizationId, Money subtotalAmount, List<Itemization> itemizations) {
      super("POST", "https://api.squareup.com/1.0/payments/capture");
      setParam("authorization_id", authorizationId);
      setParam("amount_cents", subtotalAmount.cents());
      setParam("currency", subtotalAmount.currency());

      if (itemizations != null && itemizations.size() > 0) {
        LineItems lineItems = new LineItems();
        lineItems.itemizations = itemizations;
        lineItems.payment_id = authorizationId;
        setParam("line_items", lineItems);
      }
    }

    @Override public Class<Response> getResponseClass() {
      return Response.class;
    }

    public class Response extends SquareApiBase.Response {
      String payment_id;
    }
  }

  public static class ListPayments extends SquareApiBase<ListPayments.Response> {
    public ListPayments(String role) {
      super("POST", "https://api.squareup.com/1.0/payments");
      setParam("as", role);
    }

    @Override public Class<Response> getResponseClass() {
      return Response.class;
    }

    public class Response extends SquareApiBase.Response {
      public java.util.List entities;
    }
  }

  public static class Receipt extends SquareApiBase<SquareApiBase.Response>  {
    public Receipt(String paymentId) {
      super("POST", "https://api.squareup.com/1.0/payments/receipt");
      setParam("payment_id", paymentId);
    }

    public void declined() {
      setParam("declined", "true");
    }

    public void withEmailId(String emailId) {
      setParam("email_id", emailId);
    }

    public void withEmail(String email) {
      setParam("email", email);
    }
  }
}
