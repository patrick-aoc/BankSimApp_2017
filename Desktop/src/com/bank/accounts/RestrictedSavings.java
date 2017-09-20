package com.bank.accounts;

import com.bank.databasehelper.DatabaseSelectHelper;

import java.math.BigDecimal;

public class RestrictedSavings extends SavingsAccount {
  
  /**
   * Constructor for a RestrictedSavingsAccount.
   * @param id is the account's unique ID as defined by the database
   * @param name is the account's name
   * @param balance is the balance remaining in the account
   */
  public RestrictedSavings(int id, String name, BigDecimal balance) {
    super(id, name, balance);
    
    // Using the account's unique ID, obtain its type ID from the database
    this.type = DatabaseSelectHelper.getAccountType(id);
  }

}
