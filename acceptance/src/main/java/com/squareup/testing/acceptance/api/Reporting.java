package com.squareup.testing.acceptance.api;

import com.squareup.testing.acceptance.model.Money;
import java.util.List;

public class Reporting {
  private Reporting() {
  }

  public static class GetTransactions extends ApiBase<GetTransactions.Response> {
    public GetTransactions(String merchantId) {
      super("GET", "http://connect.squareup.com/1/transactions.json?merchant_id=" + merchantId);
    }

    @Override public Class<Response> getResponseClass() {
      return Response.class;
    }

    public static class Response extends SquareApiBase.Response {
      public List<Payment> payments;

      public static class Payment {
        public String payment_token;
        public String bill_token;
        public Money total_money;
        public int quantity;
      }
    }
  }
}
