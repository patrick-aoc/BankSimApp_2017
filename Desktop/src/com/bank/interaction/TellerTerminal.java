package com.bank.interaction;

import com.bank.accounts.Account;
import com.bank.accounts.BalanceOwing;
import com.bank.accounts.ChequingAccount;
import com.bank.accounts.RestrictedSavings;
import com.bank.accounts.SavingsAccount;
import com.bank.accounts.Tfsa;
import com.bank.databasehelper.DatabaseInsertHelper;
import com.bank.databasehelper.DatabaseSelectHelper;
import com.bank.databasehelper.DatabaseUpdateHelper;
import com.bank.generics.AccountTypes;
import com.bank.generics.AccountTypesMap;
import com.bank.generics.Roles;
import com.bank.generics.RolesMap;
import com.bank.messages.Message;
import com.bank.security.PasswordHelpers;
import com.bank.users.Customer;
import com.bank.users.Teller;
import com.bank.users.User;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;

public class TellerTerminal extends InteractionMachine implements Terminal {
  
  protected Teller currentUser;
  protected boolean currentUserAuthenticated;
  
  // protected Customer currentCustomer;
  // protected boolean currentCustomerAuthenticated;
 
  
  /**
   * Constructor for TellerTerminal, authenticating the current teller given their password.
   * @param tellerId is the teller's Database generated unique ID.
   * @param password is the teller provided password.
   */
  public TellerTerminal(int tellerId, String password) {
    // Get the current teller using their Database generated ID number
    this.currentUser = (Teller) DatabaseSelectHelper.getUserDetails(tellerId);
    // Authenticate the teller using their password and set the class attribute
    boolean authenticationStatus = this.currentUser.authenticate(password);
    // This will set the authentication to true or false
    this.currentUserAuthenticated = authenticationStatus;
    
    // Set the current customer to null
    currentCustomer = null;
    // Set the customer authentication to false
    this.customerAuthenticated = false;
  }
  
  /**
   * If both the current teller and customer have authenticated,
   *  creates a new account for the customer.
   *  
   * @param name is the account's name
   * @param balance is the account's starting balance
   * @param type is the account's Type IF
   * @return true if the account was added, false otherwise
   */
  public boolean makeNewAccount(String name, BigDecimal balance, int type) {
    // Set the default response to false
    boolean added = false;
    
    // Ensure that the current user and customer are both authenticated
    if (this.currentUserAuthenticated && this.customerAuthenticated) {
      // All Account Additions must be made through the database
      int idNumber = DatabaseInsertHelper.insertAccount(name, balance, type);
      
      // Get the customer's ID number
      int customerId = this.currentCustomer.getId();

      // Register this new account to the customer
      added = (DatabaseInsertHelper.insertUserAccount(customerId, idNumber) == idNumber);
    }
    // Return the result of the attempt 
    return added;
  }
  
  public void setCurrentCustomer(Customer customer) {
    // Set the current customer
    this.currentCustomer = customer;
  }
  
  
  /**
   * Attempts to authenticate the current customer.
   * @param password is the customer's provided password.
   */
  public void authenticateCurrentCustomer(String password) {
    // Ensure that there is a customer set
    if (!(this.currentCustomer == null)) {
      // Authenticate the user using their password and set the class attribute
      boolean authenticationStatus = this.currentCustomer.authenticate(password);
      // This will set the authentication to true or false
      this.customerAuthenticated = authenticationStatus;      
      
      // Get the current customer's ID
      int customerId = currentCustomer.getId();
      // Authenticate the customer in the ATM as well
      authenticate(customerId, password);
    }
  }
  
  
  /**
   * Creates a new customer with the given attributes and adds them into the database.
   * After the new user account is created, the customer will be set to current, and they
   * will be authenticated by default.
   * 
   * @param name is the customer's name.
   * @param age is the customer's age.
   * @param address is the customer's address.
   * @param password is the customer's desired password.
   */
  public int makeNewUser(String name, int age, String address, String password) {
    // Set the default ID number to -1
    int idNumber = -1;
    
    // Check that the current user is authenticated
    if (this.currentUserAuthenticated) {
      // Locate the customer role ID
      int roleId = locateCustomerRoleId();
      // Create a Customer user in the database
      int customerId = DatabaseInsertHelper.insertNewUser(name, age, address, roleId, password);
      
      // Get the customer object from the database
      Customer customer = (Customer) DatabaseSelectHelper.getUserDetails(customerId);
      // Set the new customer as the current
      this.currentCustomer = customer;
      
      // Have the customer authenticated by default after opening account
      this.authenticateCurrentCustomer(password);
      
      // Authenticate the customer in the ATM as well
      authenticate(customerId, password);
      
      // Get the customer's ID number
      idNumber = customerId;
    }
    // Return the ID number
    return idNumber;
  }
 
  
  /**
   * Gives interest and updates the balance of the account with the specified ID if both the 
   * teller and customer are authenticated and the specified account belongs to the customer.
   * @param accountId is the ID of the account.
   */
  public void giveInterest(int accountId) {
    // Verify that the teller is authenticated
    boolean tellerAuthenticated = this.currentUserAuthenticated;
    // Verify that the customer is authenticated
    boolean customerAuthenticated = this.customerAuthenticated;
    
    // Verify that the account belongs to the current customer
    boolean ownershipVerified = this.validateAccountOwnership(accountId);
    
    // Initialize the account types enum map for us to be able to check the keys and values
    AccountTypesMap accTypes = new AccountTypesMap();
    
    // If both are authenticated and the customer is not null
    if (tellerAuthenticated && customerAuthenticated && ownershipVerified) { 
      
      // Get the account's type
      int accountTypeId = DatabaseSelectHelper.getAccountType(accountId);
      
      // Get the name of the account type
      String accountTypeName = accTypes.getAccTypeName(accountTypeId);
      
      // Cast the Account to the corresponding type 
      // IF THE ACCOUNT IS A SAVINGS ACCOUNT
      if (accountTypeName.equalsIgnoreCase(AccountTypes.CHEQUING.toString())) {
        ChequingAccount customerAccount = 
            (ChequingAccount) DatabaseSelectHelper.getAccountDetails(accountId);
        
        // Get the account's interest rate
        customerAccount.findAndSetInterestRate(); 
        // Add interest to the account
        customerAccount.addInterest();
        
        // IF THE ACCOUNT IS A SAVINGS ACCOUNT
      } else if (accountTypeName.equalsIgnoreCase(AccountTypes.SAVING.toString())) {
        SavingsAccount customerAccount = 
            (SavingsAccount) DatabaseSelectHelper.getAccountDetails(accountId);
        
        // Get the account's interest rate
        customerAccount.findAndSetInterestRate(); 
        // Add interest to the account
        customerAccount.addInterest();
      
        // IF THE ACCOUNT IS A TAX FREE SAVINGS ACCOUNT
      } else if (accountTypeName.equalsIgnoreCase(AccountTypes.TFSA.toString())) {
        Tfsa customerAccount = (Tfsa) DatabaseSelectHelper.getAccountDetails(accountId);
        
        // Get the account's interest rate
        customerAccount.findAndSetInterestRate(); 
        // Add interest to the account
        customerAccount.addInterest();
      } else if (accountTypeName.equalsIgnoreCase(AccountTypes.RESTRICTEDSAVINGS.toString())) {
        RestrictedSavings customerAccount = (RestrictedSavings) 
            DatabaseSelectHelper.getAccountDetails(accountId);
        
        // Get the account's interest rate
        customerAccount.findAndSetInterestRate(); 
        // Add interest to the account
        customerAccount.addInterest();
      } else if (accountTypeName.equalsIgnoreCase(AccountTypes.BALANCEOWING.toString())) {
        BalanceOwing customerAccount = (BalanceOwing)
            DatabaseSelectHelper.getAccountDetails(accountId);
        
        // Get the account's interest rate
        customerAccount.findAndSetInterestRate(); 
        // Add interest to the account
        customerAccount.addInterest();
        
      }
      DatabaseInsertHelper.insertMessage(this.currentCustomer.getId(), 
          this.currentCustomer.getName() + "'s " + "Account " + accountId
          + " has been given interest");
    }
  }
  
  
  /**
   * Gives interest and updates the balance of all the current customer's accounts.
   */
  public void giveInterest() {
    // Verify that the teller is authenticated
    boolean tellerAuthenticated = this.currentUserAuthenticated;
    
    // Verify that the customer is authenticated
    boolean customerAuthenticated = this.customerAuthenticated;
    
    // If both are authenticated and the customer is not null
    if (tellerAuthenticated && customerAuthenticated) {
      
      // Obtain a list of all the customer's accounts
      List<Account> customerAccounts = this.currentCustomer.getAccounts();
      if (!(customerAccounts == null)) {
        for (Account custAccount : customerAccounts) {
          // Find and give interest to each account that the customer owns
          custAccount.findAndSetInterestRate();
          custAccount.addInterest();
          DatabaseInsertHelper.insertMessage(this.currentCustomer.getId(), 
              this.currentCustomer.getName() + "'s " + "Account " + custAccount.getId()
              + " has been given interest");
        }
      }
    }  
  }
 
  
  /**
   * A method that will return the total balance of all a given user's accounts.
   * @param userId the ID of the user we wish to inspect
   * @return the total balance of all the given user's accounts
   */
  public BigDecimal userTotalBalance(int userId) {
    // Get the user's accounts from the database
    List<Integer> userAccounts = DatabaseSelectHelper.getAccountIds(userId);
    BigDecimal totalBalance = new BigDecimal(0);
    // Iterate through the user's accounts
    for (Integer userAcc : userAccounts) {
      // Get the details of each account in order to get their balance. 
      Account acc = DatabaseSelectHelper.getAccountDetails(userAcc);
      totalBalance = totalBalance.add(acc.getBalance());
    }
    return totalBalance;
  }
  
  
  /**
   * A method that will update the password of a given user (the password given is not hashed).
   * @param password the new password
   * @param userId the ID of the user who will get the password change
   * @return true if the update is successful, otherwise false
   */
  public boolean updateUserPassword(String password, int userId) {
    // Instantiate a role map containing all the roles from the database
    RolesMap roleMap = new RolesMap();
    boolean success = false;
    // Attempt to instantiate a user in order to check if the user ID is valid
    User givenUser = DatabaseSelectHelper.getUserDetails(userId);
    if (!(givenUser == null) && !(password == null)) {
      // If the given user is not an admin, proceed with making attempt to update the user's
      // password
      if (!(roleMap.getRoleName(givenUser.getRoleId()).equalsIgnoreCase("admin"))) {
        String hashedPass = PasswordHelpers.passwordHash(password);
        success = DatabaseUpdateHelper.updateUserPassword(hashedPass, userId);
      }
    }
    return success;
  }
  
  
  /**
   * A method that will update the address of a given user.
   * @param address the new address
   * @param userId the ID of the user who will get the address change
   * @return true if the update is successful, otherwise false
   */
  public boolean updateUserAddress(String address, int userId) {
    // Instantiate a role map containing all the roles from the database
    RolesMap roleMap = new RolesMap();
    boolean success = false;
    // Attempt to instantiate a user in order to check if the user ID is valid
    User givenUser = DatabaseSelectHelper.getUserDetails(userId);
    if (!(givenUser == null) && !(address == null)) {
      // If the given user is not an admin, proceed with making attempt to update the user's
      // address
      if (!(roleMap.getRoleName(givenUser.getRoleId()).equalsIgnoreCase("admin"))) {
        success = DatabaseUpdateHelper.updateUserAddress(address, userId);
      }
    }
    return success;
  }
  
  
  /**
   * A method that will update a given user's name.
   * @param name the new name for the user
   * @param userId the ID of the user who will get a name change
   * @return true if the update was successful, otherwise false
   */
  public boolean updateUserName(String name, int userId) {
    // Instantiate a role map containing all the roles from the database
    RolesMap roleMap = new RolesMap();
    boolean success = false;
    // Attempt to instantiate a user in order to check if the user ID is valid
    User givenUser = DatabaseSelectHelper.getUserDetails(userId);
    if (!(givenUser == null) && !(name == null)) {
      // If the given user is not an admin, proceed with making attempt to update the user's
      // name
      if (!(roleMap.getRoleName(givenUser.getRoleId()).equalsIgnoreCase("admin"))) {
        success = DatabaseUpdateHelper.updateUserName(name, userId);
      }
    }
    return success;  
  }
  
  
  /** 
   * A method that will update all of the user's fields at once.
   * @param password the new desired password
   * @param address the new address
   * @param name the new name
   * @param userId the ID of the user who will be receiving all these changes
   * @return true if the update was successful, otherwise false
   */
  public boolean updateAllFields(String password, String address, String name, int userId) {
    boolean successPass = this.updateUserPassword(password, userId);
    boolean successAddress = this.updateUserAddress(address, userId);
    boolean successName = this.updateUserName(name, userId);
    return (successPass && successAddress && successName);
  }
  
  
  /**
   * A method that will fetch a message from the database given its ID.
   * @return the string representation of a message
   */
  public List<Message> viewCustomerMessages() {
    if (! (this.currentCustomer == null)) {
      // Get a list of all the messages belonging to the customer and return it
      List<Message> messages = DatabaseSelectHelper.getAllMessages(this.currentCustomer.getId());
      return messages;
    }
    return null;
  }
  
  
  /**
   * A method that will fetch a list of messages from the database. These messages will belong
   * to the person calling the method (in this case, the admin).
   * @return a list of the teller's messages
   */
  public List<Message> viewOwnMessages() {
    // Get a list of all the messages belonging to the teller and return it
    List<Message> messages = DatabaseSelectHelper.getAllMessages(this.currentUser.getId());
    return messages;
  }
  
  
  /**
   * A method that will allow an admin to leave a message to someone.
   * @param message the message to be left
   */
  public int leaveMessage(String message, int targetId) {
    // We must ensure that the message being put in is a valid message AND the given ID is a 
    // customer ID
    int msgId = -1;
    if (!(message == null)) {
      RolesMap roleMap = new RolesMap();
      User potentialTarget = DatabaseSelectHelper.getUserDetails(targetId);
      if (!(potentialTarget == null) && roleMap.getRoleName(
          potentialTarget.getRoleId()).equalsIgnoreCase("customer")) {
        msgId = DatabaseInsertHelper.insertMessage(targetId, "FROM: " 
          + this.currentUser.getName() + "\n" + "TO: " + potentialTarget.getName() 
          + "\n" + "MESSAGE: " + message);
      }
    }
    return msgId;
  }
  
  
  /**
   * De-authenticates and removes the current customer.
   */
  public void deAuthenticateCustomer() {
    // Log the current customer out
    this.customerAuthenticated = false;
    // Remove the customer
    this.currentCustomer = null;
  }
  
  //________________________________ Helper Methods________________________________
  
  /**
   * Locates the id in the database that corresponds with the Customer role.
   * @return the id that reflects the Customer role
   */
  protected static int locateCustomerRoleId() {
    // Instantiate a new role map
    RolesMap rolesMap = new RolesMap();
    EnumMap<Roles, String> currentRoles = rolesMap.getExistingRoles();
    // If we find that we cannot locate the customer's role ID in our enum map, return -1
    int customerId = -1;
    // Locate the role wit hteh customer label and return the corresponding role ID
    for (Roles roleKey : currentRoles.keySet()) {
      if (roleKey.toString().equalsIgnoreCase("customer")) {
        String custId = currentRoles.get(roleKey);
        Integer foundCustId = new Integer(custId);
        customerId = foundCustId;
      }
    }
    return customerId; 
  }
  
}
