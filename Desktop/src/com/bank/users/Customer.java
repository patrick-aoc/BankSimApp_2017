package com.bank.users;

import com.bank.accounts.Account;
import com.bank.databasehelper.DatabaseInsertHelper;
import com.bank.databasehelper.DatabaseSelectHelper;

import java.util.ArrayList;
import java.util.List;


public class Customer extends User {
  /**
   *  Constructor method for Customer.
   * @param id is the ID number assigned by the database
   * @param name is the user's name
   * @param age is the user's age
   * @param address is the user's address
   */
  public Customer(int id, String name, int age, String address) {
    // Obtain the user ID from the database upon instantiation
    this.id = id;
    // Obtain the user's name from the database upon instantiation
    this.name = name;
    // Obtain the user's age from the database upon instantiation
    this.age = age;
    // Obtain the user's address from the database upon instantiation
    this.address = address;
    
    // Automatically obtain Customer role ID from the database
    this.roleId = DatabaseSelectHelper.getUserRole(id);
  }
  
  
  /**
   * Constructor method for Customer with option to set authentication status.
   * @param id is the ID number assigned by the database
   * @param name is the user's name
   * @param age is the user's age
   * @param address is the user's address
   * @param authenticated is the user's authentication status
   */
  public Customer(int id, String name, int age, String address, boolean authenticated) {
    // Obtain the user ID from the database upon instantiation
    this.id = id;
    // Obtain the user's name from the database upon instantiation
    this.name = name;
    // Obtain the user's age from the database upon instantiation
    this.age = age;
    // Obtain the user's address from the database upon instantiation
    this.address = address;
    this.authenticated = authenticated;
    
    // Automatically obtain Customer role ID from the database
    this.roleId = DatabaseSelectHelper.getUserRole(id);   
  }
 
  
  /**
   * Returns a list of all the accounts that are associated with the customer.
   * @return associatedAccounts is the list of all the customer's accounts 
   */
  public List<Account> getAccounts() {
    // Get a list of IDs for the accounts associated with the customer
    List<Integer> allAccountIds = DatabaseSelectHelper.getAccountIds(this.id);
    
    // Create a list to hold Account Objects
    List<Account> associatedAccounts = new ArrayList<Account>();
    // If there is a list to iterate through:
    if (!(associatedAccounts == null)) {
      // Iterate through all Account IDs, converting each into an account object
      for (int accountId : allAccountIds) {
        Account retrievedAccount = DatabaseSelectHelper.getAccountDetails(accountId);
      
        // If an account object is not null
        if (!(retrievedAccount == null)) {
          // Add it to the list of the user's associated accounts
          associatedAccounts.add(retrievedAccount);
        }
      }
    }
    // Return the list of associated accounts
    return associatedAccounts;
  }
  
  
  /**
   * Given an account, attributes the account to the customer's associated accounts.
   * @param account is the account to be attributed to the customer
   */
  public void addAccount(Account account) {
    // Check that the account is not null
    boolean validAccount = (!(account == null));
    
    // If not the account is not null, proceed
    if (validAccount) {
      // Get the account's ID number
      int accountId = account.getId();
      // Attribute the account to the user in the database
      DatabaseInsertHelper.insertUserAccount(this.id, accountId);
    }
  }

}
