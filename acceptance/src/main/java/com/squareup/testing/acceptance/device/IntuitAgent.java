package com.squareup.testing.acceptance.device;

import com.squareup.testing.acceptance.IntegrationHelper;
import com.squareup.testing.acceptance.api.Reporting;
import com.squareup.testing.acceptance.model.User;

public class IntuitAgent extends Device {
  private User currentUser;

  public IntuitAgent(IntegrationHelper integrationHelper) {
    super(new HttpNetworkStack(), integrationHelper);
  }

  public void become(User user) {
    currentUser = user;
  }

  public Reporting.GetTransactions.Response getTransactionsReport() {
    return perform(new Reporting.GetTransactions(currentUser.token));
  }
}
