package com.bank.database;

import com.bank.accounts.Account;
import com.bank.databasehelper.DatabaseSelectHelper;
import com.bank.generics.AccountTypesMap;
import com.bank.generics.RolesMap;
import com.bank.messages.Message;
import com.bank.users.User;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatabaseSerializer implements Serializable {

  private static final long serialVersionUID = 6043423755827589592L;
  private Map<String, HashMap<Integer, ArrayList<String>>> database = new HashMap<String, 
      HashMap<Integer, ArrayList<String>>>();
  private RolesMap rolesMap = new RolesMap();
  private AccountTypesMap accTypesMap = new AccountTypesMap();
  
  
  /**
   * A constructor for the DatabaseSerializer class.
   */
  public DatabaseSerializer() {
    this.addAccounts();
    this.addAccountTypes();
    this.addUsers();
    this.addRoleTypes();
    this.addUserAccounts();
    this.addMessages();
  }
  
  
  /**
   * A method that will create a HashMap representing the Account Types table of the database.
   * This HashMap will have the account type IDs as keys and an array list of its row entries as
   *  values. Index 0 of the array list will be the account type's name and Index 1 of the array
   *  list will be the account type's interest rate.
   */
  public void addAccountTypes() {
    // Create the Key for the HashMap we're going to serialize
    String tableKey = "AccountType";
    
    // Initialize the HashMap that we're going to create to represent the Account Types table in the
    // database.
    HashMap<Integer, ArrayList<String>> accountTypes = new HashMap<Integer, ArrayList<String>>();
    
    // Iterate through the list of account type IDs in order to create the proper key-value 
    // pairings for the accountTypes hash map
    for (Integer accTypeId : DatabaseSelectHelper.getAccountTypesIds()) {
      
      // Initialize the ArrayList to be mapped to the current account type ID
      ArrayList<String> entries = new ArrayList<String>();
      
      // Get the value of the account type ID as a primitive int
      int accTypeIdint = accTypeId.intValue();
      
      // Add the account type name and interest rate respectively to the array list
      entries.add(accTypesMap.getAccTypeName(accTypeIdint));
      entries.add(DatabaseSelectHelper.getInterestRate(accTypeIdint).toString());
      
      // Put the array list with its respective account type ID into the account types hash map
      accountTypes.put(accTypeId, entries);
      
    }
    // Put the entries into the HashMap representing the database
    this.database.put(tableKey, accountTypes); 
  }
  
  
  /**
   * A method that will create a HashMap representing the Roles table of the database. This Hash
   * Map will have the role type IDs as keys and an array list of its row entries as values. Index
   * 0 of the array list will be the role type's name.
   */
  public void addRoleTypes() {
    // Create the Key for the HashMap we're going to serialize
    String tableKey = "Roles";
    
    // Initialize the HashMap that we're going to create to represent the Roles table in the
    // database.
    HashMap<Integer, ArrayList<String>> roleTypes = new HashMap<Integer, ArrayList<String>>();
    
    // Iterate through the list of role type IDs in order to create the proper key-value 
    // pairings for the roles hash map
    for (Integer roleTypeId : DatabaseSelectHelper.getRoles()) {
      
      // Initialize the ArrayList to be mapped to the current role type ID
      ArrayList<String> entries = new ArrayList<String>();
      
      // Get the value of the role type ID as a primitive int
      int roleTypeIdint = roleTypeId.intValue();
      
      // Add the role type name and interest rate respectively to the array list
      entries.add(rolesMap.getRoleName(roleTypeIdint));
      
      // Put the array list with its respective role ID into the roles hash map
      roleTypes.put(roleTypeId, entries);
    }
    this.database.put(tableKey, roleTypes);
  }
  
  
  /**
   * A method that will create a HashMap representing the Accounts table of the database. This Hash
   * Map will have the account IDs as keys and an array list of its row entries as values. Index 0
   * of the array list will be the name of the account. Index 1 will be the account's balance. 
   * Index 2 will be the account's type.
   */
  public void addAccounts() {
    // Create the Key for the HashMap we're going to serialize
    String tableKey = "Accounts";
    
    // Initialize the HashMap that we're going to create to represent the Accounts table in the
    // database.
    HashMap<Integer, ArrayList<String>> accounts = new HashMap<Integer, ArrayList<String>>();
    
    // Initialize a boolean to let us know once we've iterated through all the users and their
    // accounts in the database.
    boolean noMoreUsers = false;
    
    // Begin our userId count at 1 (since ID counts start at 1 in the database)
    int userId = 1;
    
    // While we still have users in the database...
    while (!(noMoreUsers)) {
      
      // Get a list of the current user's accounts
      List<Integer> accountList = DatabaseSelectHelper.getAccountIds(userId);
      
      // If the list is null, then we know that the given user is not a valid user in the database.
      // The while loop will then terminate.
      if (accountList == null) {
        noMoreUsers = true;
        
        // Otherwise, we will loop through all of the user's accounts and add them into the
        // hash map.
      } else {
        for (Integer accId : accountList) {
          
          // Get the account from the database
          Account currentAccount = DatabaseSelectHelper.getAccountDetails(accId.intValue());
          
          // If the account is invalid, we will exit the loop
          if (currentAccount == null) {
            noMoreUsers = true;
            
          } else {
            // Initialize the ArrayList to be mapped to the current account ID
            ArrayList<String> entries = new ArrayList<String>();
            
            // Get the account's name, balance and type
            String accountName = currentAccount.getName();
            String accountBalance = currentAccount.getBalance().toString();
            String accountType = accTypesMap.getAccTypeName(currentAccount.getType());
            
            // Append the 3 account fields mentioned above to the entry list
            entries.add(accountName);
            entries.add(accountBalance);
            entries.add(accountType);
            
            Integer accIdInteger = new Integer(accId);
            // Put the entry list with its respective account ID into the Accounts HashMap
            accounts.put(accIdInteger, entries);
          }
        }
        userId ++;
      }
    }  
    this.database.put(tableKey, accounts);
  }
  
  
  /**
   * A method that will create a HashMap representing the Users table of the database. This Hash 
   * Map will have the user IDs as keys and an array list of its row entries as values. Index 0 of
   * the array list will be the name of the user. Index 1 will be the age of the user. Index 2 will
   * be the address of the user. Index 3 will be the user's role ID. Index 4 will be the user's 
   * password.
   */
  public void addUsers() {
    // Create the Key for the HashMap we're going to serialize
    String tableKey = "Users";
    
    // Initialize the HashMap that we're going to create to represent the Users table in the
    // database.
    HashMap<Integer, ArrayList<String>> users = new HashMap<Integer, ArrayList<String>>();
    
    boolean noMoreUsers = false;
    int userId = 1;
    while (!(noMoreUsers)) {
      // Get the User from the database
      User currentUser = DatabaseSelectHelper.getUserDetails(userId);
      
      // If the given user is invalid, we will exit this loop
      if (currentUser == null) {
        noMoreUsers = true;
        
      } else {
        // Initialize the ArrayList to be mapped to the current user ID
        ArrayList<String> entries = new ArrayList<String>();
        
        // Get the user's name, age, address and role
        String userName = currentUser.getName();
        Integer age = new Integer(currentUser.getAge());
        String userAge = age.toString();
        String userAddress = currentUser.getAddress();
        String userRole = rolesMap.getRoleName(currentUser.getRoleId());
        String userPass = DatabaseSelectHelper.getPassword(userId);
        
        // Append the 4 data fields mentioned above to the array list
        entries.add(userName);
        entries.add(userAge);
        entries.add(userAddress);
        entries.add(userRole);
        entries.add(userPass);
        
        Integer userIdInteger = new Integer(userId);
        // Put the entry list with its respective user ID into the Users HashMap
        users.put(userIdInteger, entries);
      }
      userId ++; 
    }
    this.database.put(tableKey, users);
  }
  
  
  /**
   * A method that will create a Hash Map representing the UserAccounts table of the database. This
   * Hash Map will have the user IDs as keys and an array list of its row entries as values. 
   * Index 0 of the array list will be the user's account's name.
   */
  public void addUserAccounts() {
    // Create the Key for the HashMap we're going to serialize
    String tableKey = "UserAccounts";
    
    // Initialize the HashMap that we're going to create to represent the UserAccounts table in the
    // database.
    HashMap<Integer, ArrayList<String>> userAccounts = new HashMap<Integer, ArrayList<String>>();
    
    boolean noMoreEntries = false;
    
    // We will initialize the row entry, which will double as the user ID for our hash map
    int entry = 1;
    
    while (!(noMoreEntries)) {
      // Get a list of the current user's accounts
      List<Integer> accountList = DatabaseSelectHelper.getAccountIds(entry);
      
      // If the account list is null, then we know we got an invalid user ID. We will then exit the
      // loop if this is the case.
      if (accountList == null) {
        noMoreEntries = true;
        
        // Otherwise, we will loop through all of the user's accounts and add them to the 
        // Hash Map.
      } else {
        // Initialize the ArrayList to be mapped to the current user ID
        ArrayList<String> entries = new ArrayList<String>();
        
        for (Integer accNumber : accountList) {
          // Add the account to the list of entries
          entries.add(accNumber.toString());
          
        }
        // Put the entry list with its respective row number into the hash map.
        userAccounts.put(entry, entries);
      }
      entry ++;
    }
    this.database.put(tableKey, userAccounts);
  }
  
  
  /**
   * A method that will create a Hash Map representing the messages table of the database. This
   * Hash Map will have the message IDs as keys and an array list of its row entries as values. 
   * Index 0 of the array list will be the target user's ID, index 1 will be the message
   * represented as a string, and index 2 will be the message's viewed status.
   */
  public void addMessages() {
    // Create the Key for the HashMap we're going to serialize
    String tableKey = "UserMessages";
    
    // Initialize the HashMap that we're going to create to represent the UserAccounts table in the
    // database.
    HashMap<Integer, ArrayList<String>> userMsgs = new HashMap<Integer, ArrayList<String>>();
    
    // Get the hashmap of existing users and all of their IDs
    HashMap<Integer, ArrayList<String>> users = this.database.get("Users");
    Set<Integer> userIds = users.keySet();
    // Iterate through each ID and check if any of the users have messages 
    for (Integer userId : userIds) {
      List<Message> userMessages = DatabaseSelectHelper.getAllMessages(userId);
      if (!(userMessages == null)) {
        for (Message msg : userMessages) {
          
          // Initialize the ArrayList to be mapped to the current user ID
          ArrayList<String> entries = new ArrayList<String>();
          
          Integer targetId = msg.getMessageTarget();
          String targIdStr = targetId.toString();
          
          String msgValue = msg.getUserMessage();
          
          Integer msgStatus = msg.getViewedStatus();
          String msgStatusStr = msgStatus.toString();
            
          entries.add(targIdStr);
          entries.add(msgValue);
          entries.add(msgStatusStr);
          
          Integer msgId = msg.getMessageId();          
          userMsgs.put(msgId, entries);
        }
      }
    }
    this.database.put(tableKey, userMsgs);  
  }
  
  
  /**
   * A method that will serialize the database hash map and write it to a file called
   * "database_copy.ser".
   * @throws IOException if something goes wrong
   */
  public void serialize() throws IOException {
    FileOutputStream fileOutput = new FileOutputStream("database_copy.ser");
    ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput);
    objectOutput.writeObject(this.database);
    objectOutput.close();
  }
}
