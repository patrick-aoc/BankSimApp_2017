package com.bank.accounts;

import com.bank.databasehelper.DatabaseSelectHelper;

import java.math.BigDecimal;


public class ChequingAccount extends Account {
 
  
  /**
   * Constructor for ChequingAccount.
   * @param id is the account's unique ID as defined by the database
   * @param name is the account's name
   * @param balance is the balance remaining in the account
   */
  public ChequingAccount(int id, String name, BigDecimal balance) {
    // Set the account's ID to the database generated ID
    this.id = id;
    // Set the account's name
    this.name = name;
    // Set the account's balance
    this.balance = balance;
    
    // Using the account's unique ID, obtain its type ID from the database
    this.type = DatabaseSelectHelper.getAccountType(id);
  }
  
}
