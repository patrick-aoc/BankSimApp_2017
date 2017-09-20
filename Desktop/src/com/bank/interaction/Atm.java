package com.bank.interaction;

import com.bank.databasehelper.DatabaseSelectHelper;
import com.bank.databasehelper.DatabaseUpdateHelper;
import com.bank.exceptions.IllegalAmountException;
import com.bank.exceptions.InsufficientFundsException;
import com.bank.exceptions.InsufficientPrivilegesException;
import com.bank.generics.AccountTypesMap;
import com.bank.messages.Message;
import com.bank.users.Customer;

import java.math.BigDecimal;
import java.util.List;

public class Atm extends InteractionMachine {

  /**
   * Constructor for ATM with the option to authenticate the current customer given their password.
   * @param customerId is the customer's Database generated unique ID.
   * @param password is the customer provided password.
   */
  public Atm(int customerId, String password) {
    // Get the current customer using their Database generated ID number
    currentCustomer = (Customer) DatabaseSelectHelper.getUserDetails(customerId);
    // Authenticate the customer using their password and set the class attribute
    customerAuthenticated = currentCustomer.authenticate(password);
  }
  
  
  /**
   * Constructor for ATM without authenticating the current customer.
   * @param customerId is the customer's Database generated unique ID.
   */
  public Atm(int customerId) {
    // Get the current customer using their Database generated ID number
    currentCustomer = (Customer) DatabaseSelectHelper.getUserDetails(customerId);
  }
  
  @Override
  /**
   * Given an amount to withdraw and ID of the account from which to withdraw from, updates
   * the balance of the account after the withdrawal if the account belongs to the currently
   * authenticated user.
   * 
   * @param amount is the amount to withdraw
   * @param accountId is the ID of the account from which to withdraw
   * @return true if the withdrawal was completed, false otherwise
   * @throws IllegalAmountException if the withdrawal amount is less than or equal to 0
   * @throws InsufficientFundsException if there is not enough money to withdraw
   * @throws InsufficientPrivilegesException if the user is not authenticated or the user is trying
   *     to withdraw from a RestrictedSavingsAccount
   */
  public boolean makeWithdrawal(BigDecimal amount, int accountId) 
      throws IllegalAmountException, InsufficientFundsException, InsufficientPrivilegesException {
    // We shall begin by getting the enum map for account types. We will also check to see what
    // kind of account we're dealing with.
    AccountTypesMap accTypeMap = new AccountTypesMap();
    int accountType = DatabaseSelectHelper.getAccountType(accountId);
    String accType = accTypeMap.getAccTypeName(accountType);
    
    // Check if the account is a restricted savings account
    boolean isRestrictedSavings = accType.equalsIgnoreCase("restricted savings");
    
    // Set the default response to false
    boolean withdrawalMade = false;
    
    // Check whether a customer is loaded and that the account belongs to the current user
    boolean ownershipVerified = validateAccountOwnership(accountId);
    
    // Check whether a customer is authenticated
    boolean userAuthenticated = customerAuthenticated;
    
    boolean isBalanceOwing = accTypeMap.getAccTypeName(DatabaseSelectHelper.getAccountType(
        accountId)).equals("balance owing") ;
    
    // Validate the withdrawal amount is positive
    boolean validWithdrawal = (amount.compareTo(BigDecimal.ZERO) == 1);
    
    // If the withdrawal is not valid, throw an exception
    if (!(validWithdrawal) && !(isBalanceOwing)) {
      throw new IllegalAmountException();
    }
      
    // If the user is not authenticated, throw an exception
    if (!(userAuthenticated) || isRestrictedSavings) {
      throw new InsufficientPrivilegesException();
    }
    
    // If the ownership has been verified, the amount is greater than 0, and authenticated:
    if (ownershipVerified && validWithdrawal && userAuthenticated) {
      
      // Calculate the new balance, setting mode to withdraw
      BigDecimal newBalance = calculateBalance(accountId, amount, "withdraw", isBalanceOwing);
      
      // Update the account balance through the database and get token
      withdrawalMade = DatabaseUpdateHelper.updateAccountBalance(newBalance, accountId);
      
      // While we are here, we must also check to see what type of account we've made a withdrawal
      // from. If it's a SavingsAccount, we have to check if the new balance is below $1000.00. If
      // it is, we must convert it into a chequing account.
      if (accType.equalsIgnoreCase("savings")) {
        BigDecimal minimumSavingsBalance = new BigDecimal(1000.00);
        if (newBalance.compareTo(minimumSavingsBalance) == -1) {
          this.convertSavingsToChequing(accountId);
        }
      }   
    }
    // Return the response
    return withdrawalMade;
  }
  
  /**
   * A method that will fetch a list of messages from the database. These messages will belong
   * to the person calling the method (in this case, the admin).
   * @return a list of the admin's messages
   */
  public List<Message> viewOwnMessages() {
    // Get a list of all the messages belonging to the customer and return it
    List<Message> messages = DatabaseSelectHelper.getAllMessages(this.currentCustomer.getId());
    return messages;
  }
}