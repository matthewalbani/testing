package com.squareup.testing.acceptance.device;

import com.squareup.common.locale.ISOCurrency;
import com.squareup.testing.acceptance.IntegrationHelper;
import com.squareup.testing.acceptance.api.Payments;
import com.squareup.testing.acceptance.model.Itemization;
import com.squareup.testing.acceptance.model.Money;
import com.squareup.testing.acceptance.model.Payment;
import com.squareup.testing.acceptance.model.PaymentCard;
import java.util.ArrayList;
import java.util.List;

public class Register extends SquareClient {
  private ISOCurrency isoCurrency = ISOCurrency.USD;
  private PaymentCard lastSwipedCard;
  private long currentSubtotalAmount;

  private String authorizationId;
  private List<Payment> visiblePayments;
  private List<Itemization> itemizations;
  private Payments.Receipt receipt;

  public Register(String platform, String version, IntegrationHelper integrationHelper) {
    super("register", platform, version, integrationHelper);
    itemizations = new ArrayList<Itemization>();
  }

  public void tapItem(String itemName) {
    int amountCents = 1000;
    Itemization itemization = new Itemization();
    itemization.itemName = itemName;
    itemization.priceCents = amountCents;
    itemization.currencyCode = "USD";
    itemization.priceMoney = Money.of(amountCents, "USD");
    itemization.totalMoney = Money.of(amountCents, "USD");
    itemization.quantity = 1;
    itemizations.add(itemization);

    currentSubtotalAmount += amountCents;
  }

  public void gotManualPriceEntry(int dollars, int cents) {
    int minorUnit = isoCurrency.getMinorUnit();
    long amountCents = dollars;
    for (int i = 0; i < minorUnit; i++) {
      amountCents *= 10;
    }
    amountCents += cents;

    currentSubtotalAmount += amountCents;
  }

  public void gotSwipe(PaymentCard paymentCard) {
    assertOnScreen("Payment Pad");

    lastSwipedCard = paymentCard;
    authorize();

    nowOnScreen("Confirm Payment");
  }

  public void gotPaymentConfirmation() {
    assertOnScreen("Confirm Payment");

    if (authorizationId == null) {
      throw new RuntimeException("unknown payment method or authorization failed");
    }

    capture();
  }

  public void gotEmailAddressForReceipt(String email) {
    assertOnScreen("Receive Receipt");

    receipt = new Payments.Receipt(authorizationId);
    receipt.withEmail(email);
    requestReceipt();
  }

  public void showPaymentList() {
    assertOnScreen("Payment Pad");

    loadPayments();
  }

  public List<Payment> getVisiblePayments() {
    return visiblePayments;
  }

  private void authorize() {
    Payments.Authorize.Response response = perform(new Payments.Authorize(subtotal(), lastSwipedCard));
    authorizationId = response.authorization_id;
    //scannedQrCode = null;
    //tenderApproved = false;
  }

  private void capture() {
    Payments.Capture.Response response = perform(new Payments.Capture(authorizationId, subtotal(), itemizations));
  }

  private void requestReceipt() {
    Payments.Receipt.Response response = perform(receipt);
  }

  private void loadPayments() {
    Payments.ListPayments.Response response = perform(new Payments.ListPayments("merchant"));
    visiblePayments = response.entities;
  }

  private com.squareup.common.values.Money subtotal() {
    return com.squareup.common.values.Money.of(isoCurrency, currentSubtotalAmount);
  }

  private void assertOnScreen(String... name) {
    // todo
  }

  private void nowOnScreen(String name) {
    // todo
  }
}
