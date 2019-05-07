package com.squareup.testing.acceptance;

import com.squareup.logging.Logger;
import com.squareup.testing.acceptance.device.Register;
import com.squareup.testing.acceptance.model.PaymentCard;
import com.squareup.testing.acceptance.model.User;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore("For some reason this is flaky in CI for finding the square root directory")
public class IntegrationHelperSampleTest {
  static {
    Logger logger = Logger.getRootLogger();
    logger.setLevel(Level.ALL);
    logger.addConsoleAppender(Logger.SIMPLE_LAYOUT_PATTERN);
  }

  @Rule public IntegrationRule integrationRule = new IntegrationRule();
  public RemoteApp webRemoteApp;

  @IntegrateApps({ "web", "fidelius", "payments", "multipass", "roster", "beemo" })
  @Test public void takeAPayment() throws Exception {
    User cherylUser = createUser("Cheryl", "merchant");
    Register register = new Register("iPad", "3.0", integrationRule.getIntegrationHelper());
    register.logIn(cherylUser.email, "password");
    register.gotManualPriceEntry(10, 0);
    PaymentCard paymentCard = PaymentCard.valid();
    register.gotSwipe(paymentCard);
    register.gotPaymentConfirmation();

    //register.showPaymentList();
    //List<Payment> visiblePayments = register.getVisiblePayments();
    //assertThat(visiblePayments).hasSize(1);
    //Payment payment = visiblePayments.get(0);
    //assertThat(payment.amount).isEqualTo(Money.of(CurrencyUnit.USD, 10))

    //Actor cheryl = Actor.newBuilder().setName("Cheryl").setRole("customer").build();
  }

  private User createUser(String name, String role) {
    List<String> permissionNames = new ArrayList<String>();
    String businessType = "individual_use";

    permissionNames.add("create_payment_feed_entries");
    if (role.equals("merchant")) {
      permissionNames.add("accept_payment_cards");

      businessType = "caterer";
    }

    User.FactoryRequest prototype = new User.FactoryRequest();
    prototype.name = name;
    prototype.email = name.toLowerCase().replaceAll(" ", ".") + "@example.com";
    prototype.password = "password";
    //prototype.business_type = businessType;
    prototype.permission_names = permissionNames.toArray(new String[permissionNames.size()]);
    return integrationRule.getRemoteApp("web").build("user_with_images", prototype, User.class);
  }

  /*
    describe "Paying with a QR code" do
      it "I link a payment card, and pay using a charge token" do
        cheryl = Actor.new("Cheryl", :role => :customer).reify!
        cheryl.device = Device::PayWithSquare.new("iPhone", "2.4")
        cheryl.log_in
        cheryl.view_account_screen
        cheryl.enter_payment_card_info
        cheryl.device_qr_code.should_not be_nil

        siren = Actor.new("Siren", :role => :merchant).reify!
        larry = Actor.new("Larry").reify!
        larry.works_for siren
        larry.device = Device::SirenPOS.new
        larry.log_in
        larry.manually_enter_amount Money.new(300, "USD")
        cheryl.present_qr_code_to larry
        larry.confirm_payment

        card_activity = IntegrationHelper.remote_app(:payments).call(:get, "payments")
        last_transaction = card_activity.select { |tx| tx["account_number"] == cheryl.card_info.pan }.last
        last_transaction["status"].should == "AUTHORIZED"
        last_transaction["amount"]["total"].should == 300
        last_transaction["amount"]["currency_code"].should == "USD"

        cheryl.open_purchase_history
        cheryl.visible_payments.should have(1).payment
        cheryl.visible_payments[0][:amount].should == Money.new(300, "USD")
        cheryl.visible_payments[0][:tax_amount].should == Money.new(0, "USD")
        cheryl.visible_payments[0][:tip_amount].should == Money.new(0, "USD")

        claudette = Actor.new("Claudette", :role => :customer)
        claudette.works_for siren
        claudette.device = Device::Register.new("iPad", "1.0")
        claudette.log_in
        claudette.open_sales_history
        claudette.visible_payments.should have(1).payment
        cheryl.visible_payments[0][:amount].should == Money.new(300, "USD")
        cheryl.visible_payments[0][:tax_amount].should == Money.new(0, "USD")
        cheryl.visible_payments[0][:tip_amount].should == Money.new(0, "USD")
      end
    end
   */
}
