package com.bank.interaction;

import com.bank.accounts.Account;
import com.bank.databasehelper.DatabaseInsertHelper;
import com.bank.databasehelper.DatabaseSelectHelper;
import com.bank.databasehelper.DatabaseUpdateHelper;
import com.bank.exceptions.ConnectionFailedException;
import com.bank.exceptions.IllegalAmountException;
import com.bank.exceptions.InsufficientFundsException;
import com.bank.exceptions.InsufficientPrivilegesException;
import com.bank.generics.AccountTypesMap;
import com.bank.users.Customer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public abstract class InteractionMachine {
  
  protected Customer currentCustomer;
  protected boolean customerAuthenticated;
  
  /**
   * Empty constructor for ATM and TellerTerminal subclass.
   */
  protected InteractionMachine() {
    // Do not set a current customer
    currentCustomer = null;
    // Do not authenticate anyone
    customerAuthenticated = false;
  }
  
  
  /**
   * Attempts to authenticate a user, given their ID number and password.
   * If a customer is already loaded into the ATM will only allow authentication
   * for that customer. If no customer is loaded, the machine will allow the specified
   * customer to be loaded and authenticated.
   * 
   * @param userId is the customer's ID number
   * @param password is the user's password
   * @return true if the operation was successful, false otherwise
   */
  public boolean authenticate(int userId, String password) {  
    // Set the default response to false
    boolean response = false;
    
    // Check if a customer is currently loaded
    boolean customerLoggedIn = !(currentCustomer == null);
    
    if (customerLoggedIn) {
      // Check if the given id is the customer's ID
      boolean idValid = (userId == currentCustomer.getId());
      
      // If the IDs match, continue
      if (idValid) {
        // Authenticate the customer using their password and set the class attribute
        response = currentCustomer.authenticate(password);
        // Set the success attribute to the token
        customerAuthenticated = response;
      }
    
      // If no customer is logged in --> Attempt to log the new customer in
    } else {
      // Get the customer by their user ID    
      Customer newCustomer = (Customer) DatabaseSelectHelper.getUserDetails(userId);
      // Set the customer as the current customer
      currentCustomer = newCustomer;
      
      // Authenticate the customer using their password and set the class attribute
      response = newCustomer.authenticate(password);
      // Set the success attribute to the token
      customerAuthenticated = response;
    }
    // Return the result
    return response;
  }
  
  
  /**
   * Returns a list of all the accounts associated with the customer that is currently loaded.
   * @return associatedAccounts is the list of accounts for the user.
   */
  public List<Account> listAccounts() {
    // Create a list to hold accounts
    List<Account> associatedAccounts = new ArrayList<Account>();
    
    // Check if a customer is currently loaded
    boolean customerLoggedIn = !(currentCustomer == null);

    // If a customer is loaded
    if (customerLoggedIn) {
      // Get the list of accounts associated with the user.
      associatedAccounts = currentCustomer.getAccounts();  
    }
    // Return the associated accounts
    return associatedAccounts;
  }
  
  
  /**
   * Makes a deposit of value amount into the account with ID accountId if both the deposit is valid
   * exists and the account is one that belongs to the current customer.
   * 
   * @param amount is the amount to deposit
   * @param accountId is the account to deposit into
   * @return true if the deposit was made, false otherwise.
   * @throws IllegalAmountException if the deposit amount is less than or equal to 0
   * @throws InsufficientPrivilegesException if a user is not authenticated
   */
  public boolean makeDeposit(BigDecimal amount, int accountId) 
      throws IllegalAmountException, InsufficientPrivilegesException {
    // Set the default response to false
    boolean depositMade = false;
    
    // Check whether a customer is loaded and that the account belongs to the current user
    boolean ownershipVerified = validateAccountOwnership(accountId);
    
    // Check whether a customer is authenticated
    boolean userAuthenticated = customerAuthenticated;
    
    // Validate the deposit amount
    boolean validDeposit = (amount.compareTo(BigDecimal.ZERO) == 1);
    
    // If the deposit is not valid, throw an exception
    if (!(validDeposit)) {
      throw new IllegalAmountException();
    }
      
    // If the user is not authenticated, throw an exception
    if (!(userAuthenticated)) {
      throw new InsufficientPrivilegesException();
    }
    
    // If the ownership has been verified, the amount is greater than 0, and authenticated:
    if (ownershipVerified && validDeposit && userAuthenticated) {
      
      // Exception is reserved for withdrawal -> T/C block to prevent misunderstanding of error
      try {
        // Calculate the new balance, setting mode to deposit
        // It's also worth noting that it won't matter if the account is of a BalanceOwing type
        // or not
        BigDecimal newBalance = calculateBalance(accountId, amount, "deposit", false);
      
        // Update the account balance through the database and get token
        depositMade = DatabaseUpdateHelper.updateAccountBalance(newBalance, accountId);
      
      // IFE error should never occur.
      } catch (InsufficientFundsException error) {
        return false;
      }
      
    }
    // Return the response
    return depositMade;
  }
  
  
  /**
   * Given an account ID, checks that account's balance if the account belongs to the currently
   * loaded user and that said user is authenticated.
   * 
   * @param accountId is the id number for the account to check
   * @return accountBalance is the account's balance
   * @throws InsufficientPrivilegesException if the user is not authenticated
   * @throws ConnectionFailedException if unable to get balance for authenticated user.
   */
  public BigDecimal checkBalance(
      int accountId) throws InsufficientPrivilegesException, ConnectionFailedException {
    // Check whether a customer is loaded and that the account belongs to the current user
    boolean ownershipVerified = validateAccountOwnership(accountId);
    
    // Check whether a customer is authenticated
    boolean userAuthenticated = customerAuthenticated;
    
    // If the user is not authenticated, throw an exception
    if (!(userAuthenticated)) {
      throw new InsufficientPrivilegesException();
    }
    

    // If the ownership has been verified and authenticated:
    if (ownershipVerified && userAuthenticated) {
      // Get the account details using the accountId
      Account currentAccount = DatabaseSelectHelper.getAccountDetails(accountId);

      // Retrieve the account balance
      BigDecimal accountBalance = currentAccount.getBalance();
      
      // Return the balance rounded to two decimal places
      return accountBalance.setScale(2, RoundingMode.CEILING);
   
    // If unable to get account balance for an authenticated user
    } else {
      // throw an exception
      throw new ConnectionFailedException();
    }
  }
  
  
  /**
   * Given an amount to withdraw and ID of the account from which to withdraw from, updates
   * the balance of the account after the withdrawal if the account belongs to the currently
   * authenticated user. If a withdraw to a savings account leaves the balance under $100,
   * the said account is automatically converted to a chequing account.
   * 
   * @param amount is the amount to withdraw
   * @param accountId is the ID of the account from which to withdraw
   * @return true if the withdrawal was completed, false otherwise
   * @throws IllegalAmountException if the withdrawal amount is less than or equal to 0
   * @throws InsufficientFundsException if there is not enough money to withdraw
   * @throws InsufficientPrivilegesException if the user is not authenticated.
   */
  public boolean makeWithdrawal(BigDecimal amount, int accountId) 
      throws IllegalAmountException, InsufficientFundsException, InsufficientPrivilegesException {
    
    AccountTypesMap accTypes = new AccountTypesMap();
    // Set the default response to false
    boolean withdrawalMade = false;
    
    // Check whether a customer is loaded and that the account belongs to the current user
    boolean ownershipVerified = validateAccountOwnership(accountId);
    
    // Check whether a customer is authenticated
    boolean userAuthenticated = customerAuthenticated;
    
    // Validate the withdrawal amount is positive
    boolean validWithdrawal = (amount.compareTo(BigDecimal.ZERO) == 1);
    
    // Check if the account is a "balance owing" account
    boolean isBalanceOwing = accTypes.getAccTypeName(DatabaseSelectHelper.getAccountType(
        accountId)).equals("balance owing") ;
    
    // If the withdrawal is not valid AND it isn't a balance owing account, throw an exception
    if (!(validWithdrawal) && !(isBalanceOwing)) {
      throw new IllegalAmountException();
    }
      
    // If the user is not authenticated, throw an exception
    if (!(userAuthenticated)) {
      throw new InsufficientPrivilegesException();
    }
    
    // If the ownership has been verified, the amount is greater than 0, and authenticated:
    if (ownershipVerified && validWithdrawal && userAuthenticated) {
      
      // Calculate the new balance, setting mode to withdraw
      BigDecimal newBalance = calculateBalance(accountId, amount, "withdraw", isBalanceOwing);
      
      // Update the account balance through the database and get token
      withdrawalMade = DatabaseUpdateHelper.updateAccountBalance(newBalance, accountId);
      
      AccountTypesMap accTypeMap = new AccountTypesMap();
      // Check if the given account is a Savings account.
      int accountType = DatabaseSelectHelper.getAccountType(accountId);
      String accType = accTypeMap.getAccTypeName(accountType);
      // If it is, determine if the balance is less than $1000
      if ((accType.equalsIgnoreCase("savings"))) {
        
        // If less than $1000, convert to cheqing account
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
   * A method that will convert a Savings account into a chequing account iff the balance of 
   * a Savings account is less than $1000.00.
   * @param accId the ID of the Savings account
   * @return true if the account was successfully converted, otherwise false
   */
  protected boolean convertSavingsToChequing(int accId) {
    // Set the default response to false
    boolean conversionSuccess = false;
    if (!(accId == 0)) {
      if (this.validateAccountOwnership(accId)) {
        // Get the enum map containing all the account types.
        AccountTypesMap accTypes = new AccountTypesMap();
        // Get the account type ID for a Chequing account and update the database as necessary.
        int chequingId = accTypes.getAccTypeId("chequing");
        DatabaseUpdateHelper.updateAccountType(chequingId, accId);
        // Set return token and leave a message
        DatabaseInsertHelper.insertMessage(currentCustomer.getId(), "Account " + accId + " has"
            + " been converted from a Savings account to a Chequing account");
        conversionSuccess = true;
      }
    } 
    // Return response
    return conversionSuccess;
  }
  
  //________________________________ Helper Methods________________________________
  
  /**
   * Given an account ID, verifies that the account exists and belongs to the current customer.
   * @param accountId is the account ID to be verified
   * @return true if the account can be attributed to the current user, false otherwise
   */
  protected boolean validateAccountOwnership(int accountId) {
    // Set the default response to false
    boolean ownershipValidated = false;
    
    // Ensure that there is a current customer
    if (!(currentCustomer == null)) {
      // Create an array list to hold the valid account IDs
      List<Integer> validIds = new ArrayList<Integer>();

      // Populate the list with the IDs of accounts that the customer owns
      validIds = DatabaseSelectHelper.getAccountIds(currentCustomer.getId());
      
      // Ensure that the list of valid IDs is non-empty
      if (!(validIds == null)) {
        // Check if the given ID is one of these valid IDs and set token
        ownershipValidated = validIds.contains(accountId);
      }
    }
    // Return the search result
    return ownershipValidated;
  }
  
  
  /**
   * Given a valid account ID, a fixed amount of funding, and mode,
   * updates the account's balance.
   * 
   * @param accountId is the validated ID of an account
   * @param funding is the funding to deposit or withdraw
   * @param mode must be one of "deposit" or "withdraw"
   * @return the updated account balance
   * @throws InsufficientFundsException if funding > balance in withdraw mode
   */
  protected BigDecimal calculateBalance(
      int accountId, BigDecimal funding, String mode, boolean balanceOwing)
          throws InsufficientFundsException {
    // Get the account using its ID number
    Account currentAccount = DatabaseSelectHelper.getAccountDetails(accountId);
    
    // Get the current balance
    BigDecimal currentBalance = currentAccount.getBalance();
    // Set the default new balance to the current
    BigDecimal newBalance = currentBalance;
    
    // If making a deposit
    if (mode.equals("deposit")) {
      // Add the funds
      newBalance = currentBalance.add(funding);
    }
    
    // If making a withdrawal
    if (mode.equals("withdraw")) {
      
      // Check if the funding to subtract is greater than the balance and if it's a balance owing
      // account
      boolean outOfBounds = ((funding.compareTo(currentBalance) == 1) && !(balanceOwing));
      
      // If the funding is out of bounds and is not a balance owing account:
      if (outOfBounds) {
        throw new InsufficientFundsException();
      
      // If the funding is in bounds:
      } else {
        // Calculate the new balance
        newBalance = currentBalance.subtract(funding);
      }
    }
    // Return the new balance set to two decimal places
    return newBalance.setScale(2, RoundingMode.CEILING);
  }
  
}
