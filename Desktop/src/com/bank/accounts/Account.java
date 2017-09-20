// Autumn Jiang
// CSCB07 Assignment

package com.bank.accounts;

import com.bank.databasehelper.DatabaseSelectHelper;
import com.bank.databasehelper.DatabaseUpdateHelper;

import java.math.BigDecimal;


public abstract class Account {
  
  // Private Account attributes
  protected int id;                  // id is the ID number set by the database
  protected String name;             // name is the Account name
  protected BigDecimal balance;      // balance is the balance of the account
  protected BigDecimal interestRate; // interest rate for the account type
  protected int type;                // type is the account's Type ID
  private boolean idSet = false;     // Ensure Id can never be changed once set.
   
  
  /**
   * Returns an Account's ID number.
   * @return number of Account
   */
  public int getId() {
    return this.id;
  }
  
  
  /**
   * Sets the Account's ID number to the value assigned to it by the database
   * if and only if it has not already been assigned a value.
   * @param id number of User
   */
  public void setId(int id) {
    // If the ID hasn't previously been set:
    if (!idSet) {
      // Set it now
      this.id = id;
      // Prevent it from being reset
      idSet = true;
    }
  }
  
  
  /**
   * Returns an Account's name.
   * @return name of Account
   */
  public String getName() {
    return this.name;
  }
  
  
  /**
   * Sets an Account's name.
   * @param name of Account
   */
  public void setName(String name) {
    // All changes to account names must be verified by the database
    boolean nameSetDatabase = DatabaseUpdateHelper.updateAccountName(name, this.id);
    
    // If the name was changed in the database:
    if (nameSetDatabase) {
      // Reflect the change
      this.name = name;
    }
  }
  
  
  /**
   * Returns the balance left in an Account.
   * @return balance of the Account
   */
  public BigDecimal getBalance() {
    return this.balance;
  }
  
  
  /**
   * Sets a balance to the Account.
   * @param balance to deposit in the Account
   */
  public void setBalance(BigDecimal balance) {
    // All changes to balance must be verified by the database
    boolean balanceSetDatabase = DatabaseUpdateHelper.updateAccountBalance(balance, this.id);
    
    // If the balance was changed in the database:
    if (balanceSetDatabase) {
      // Reflect the change
      this.balance = balance;
    }
  }
  
  
  /**
   * Gets the type of the account.
   * @return type of the account
   */
  public int getType() {
    return this.type;
  }
  
  
  /**
   * Automatically finds the account's standard interest rate based on its type and
   * sets that interest rate to the account.
   */
  public void findAndSetInterestRate() {
    // Using the account's type ID, obtain its standard interest rate from the database
    this.interestRate = DatabaseSelectHelper.getInterestRate(this.type);
  }
  
  
  /**
   * Uses the interest rate and current balance to update the balance in the account.
   */
  public void addInterest() {
    // Get the current balance and interest rate
    BigDecimal balance = this.balance;
    BigDecimal interest = this.interestRate;
    
    // Ensure both are defined
    boolean bothDefined = !(balance == null && interest == null);
    
    // If both defined:
    if (bothDefined) {
      // Calculate the total interest
      BigDecimal totalInterest = balance.multiply(interest);
      // Add the total interest to the balance
      BigDecimal newBalance = balance.add(totalInterest);
      
      // Update the balance through the database and get an approval token
      boolean updated = DatabaseUpdateHelper.updateAccountBalance(newBalance, this.id);
      
      // If the update was successful:
      if (updated) {
        // Reflect the change in the class.
        this.balance = newBalance;
      }
    }
  }
  
}
