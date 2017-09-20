package com.bank.databasehelper;

import com.bank.accounts.Account;
import com.bank.accounts.BalanceOwing;
import com.bank.accounts.ChequingAccount;
import com.bank.accounts.RestrictedSavings;
import com.bank.accounts.SavingsAccount;
import com.bank.accounts.Tfsa;
import com.bank.database.DatabaseSelector;
import com.bank.generics.AccountTypes;
import com.bank.generics.Roles;
import com.bank.messages.Message;
import com.bank.users.Admin;
import com.bank.users.Customer;
import com.bank.users.Teller;
import com.bank.users.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DatabaseSelectHelper extends DatabaseSelector {
  
  /**
   * Obtains a role name from the database by its assigned role ID number.
   * @param id of the role
   * @return name of the role
   */
  public static String getRole(int id) {
    try {
      // Check to see if the given type is valid
      boolean definedRole = verifyRoleId(id);
    
      // Set the default role name to null
      String role = null;
    
      // Proceed iff the given type is defined in the database
      if (definedRole) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Get the role from the database by ID
        role = DatabaseSelector.getRole(id, connection);
        // Close the connection
        connection.close();
      }
      return role;
    
    // If an exception is thrown --> return null
    } catch (SQLException error) {
      return null;
    }
  }
  
  
  /**
   * Given a user's unique ID, returns their hashed password or null if
   * the user is not in the database.
   * 
   * @param userId is a user's unique ID
   * @return the hashed password for the user
   */
  public static String getPassword(int userId) {
    try {
      // Verify the user's ID
      boolean verified = verifyUserId(userId);
      // Set the default password to null
      String hashPassword = null;
    
      // Proceed if the user ID can be verified
      if (verified) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Get the user's password
        hashPassword = DatabaseSelector.getPassword(userId, connection);
        // Close the connection
        connection.close();
      }
      // Return the password
      return hashPassword; 
      
    // If an exception is thrown --> return null
    } catch (SQLException error) {
      return null;
    }
  }
  
  
  /**
   * Given a user's unique ID, returns a User object with their name, age, address, and role ID.
   * @param userId is a user's Unique ID
   * @return userInfo is a User Object with corresponding details
   */
  public static User getUserDetails(int userId) {
    try {
      // Set the default information to null
      User user = null;
      // Connect to the database
      Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
      // Get a ResultSet of User Details -> Might throw SQLException -> Catch -> null
      ResultSet results = DatabaseSelector.getUserDetails(userId, connection);
        
      // Fill in a name
      String name = null;
      // Fill in an age
      int age = -1;
      // Fill in an address
      String address = null;
      // Fill in a role ID
      int roleId = -1;
        
      // Read the resultSet
      while (!(results == null) && results.next()) {
        // Get the info from the result set
        name = results.getString("NAME");
        age = results.getInt("AGE");
        address = results.getString("ADDRESS");                    
        roleId = results.getInt("ROLEID");
        
        // Get the type role name associated with the roleId
        String roleName = getRole(roleId);
        
        // If the role name resolves to ADMIN
        if (roleName.equalsIgnoreCase(Roles.ADMIN.toString())) {
          // Return an Admin
          user = new Admin(userId, name, age, address);
          
          // If the role name resolves to TELLER
        } else if (roleName.equalsIgnoreCase(Roles.TELLER.toString())) {
          // Return a Teller
          user = new Teller(userId, name, age, address);
        
          // If the role name resolves to CUSTOMER
        } else if (roleName.equalsIgnoreCase(Roles.CUSTOMER.toString())) {
          // Return a Customer
          user = new Customer(userId, name, age, address);
        }
      }
      // Close the connection
      connection.close();
      // Permanently set the user ID
      user.setId(userId);
      // Return the user
      return user;
    
      // If an exception is thrown --> return null
    } catch (SQLException error1) {
      return null;
    } catch (NullPointerException error2) {
      return null;
    }
  }
  

  /**
   * Given a user's unique ID, returns a list of all the accounts associated
   * with the user or null if the user is not in the database.
   * 
   * @param userId is the User's unique ID
   * @return associatedAccounts is a list of all the accounts associated with the user
   */
  public static List<Integer> getAccountIds(int userId) {
    try {
      // Verify the user's ID
      boolean verified = verifyUserId(userId);
      // Proceed if the user ID can be verified
      if (verified) {  
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Get the associated accounts
        ResultSet results = DatabaseSelector.getAccountIds(userId, connection);
    
        // Create a list to hold the account IDs
        List<Integer> associatedAccounts = new ArrayList<>();
      
        // Iterate through the accounts, adding them to the list
        while (!(results == null) && results.next()) {
          associatedAccounts.add(results.getInt("ACCOUNTID"));
        }
        // Close the connection
        connection.close();
        // Return the list of associated accounts
        return associatedAccounts;
        
        // If not verified --> return null
      } else {
        return null;
      } 
      
    // If an exception is thrown --> return null
    } catch (SQLException error) {
      return null;
    }
  }   
  
  
  /**
   * Given an account's Unique ID, return the account's name, balance and type in an Account object.
   * @param accountId is an Account's Unique ID
   * @return accountInfo is an Account with corresponding information or null if undefined
   */
  public static Account getAccountDetails(int accountId) {
    try {
      // Set the default response to null
      Account account = null; 
      
      // Connect to the database
      Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
      // Get a ResultSet of Account Details -> Might throw SQLException -> Catch -> null
      ResultSet results = DatabaseSelector.getAccountDetails(accountId, connection);
        
      // Fill in a name
      String name = null;
      // Fill in a balance
      BigDecimal balance = null;
      // Fill in a type ID
      int typeId = -1;

      // Iterate through the ResultSet
      while (!(results == null) && results.next()) {
        // Get the information from the ResultSet
        name = (results.getString("NAME"));
        balance = new BigDecimal(results.getString("BALANCE"));                    
        typeId = (results.getInt("TYPE"));
        
        // Use the account typeId to determine instantiation
        String type = getAccountTypeName(typeId);
        // If the type is CHEQUING
        if (type.equalsIgnoreCase(AccountTypes.CHEQUING.toString())) {
          // Set up the type
          account = new ChequingAccount(accountId, name, balance);
        
          // If the type is SAVING
        } else if (type.equalsIgnoreCase(AccountTypes.SAVING.toString())) {
          // Set up the type
          account = new SavingsAccount(accountId, name, balance);
        
          // If the type is TFSA
        } else if (type.equalsIgnoreCase(AccountTypes.TFSA.toString())) {
          // Set up the type
          account = new Tfsa(accountId, name, balance);
       
          // If the type is Restricted Savings
        } else if (type.equalsIgnoreCase(AccountTypes.RESTRICTEDSAVINGS.toString())) {
          // Set up the type
          account = new RestrictedSavings(accountId, name, balance);
          
          // If the type is Balance Owing
        } else if (type.equalsIgnoreCase(AccountTypes.BALANCEOWING.toString())) {
          // Set up the type
          account = new BalanceOwing(accountId, name, balance);
          
        }
      }
      // Close the connection
      connection.close();
      // Permanently set the account's ID
      account.setId(accountId);
      // Return an account
      return account;
      
    // If an exception is thrown --> return null
    } catch (SQLException error) {
      return null;
    }
  }
  

  /**
   * Given an account's unique ID, return the balance left in the account.
   * @param accountId is an account's unique ID
   * @return balance is the remaining balance in the account or null if undefined
   */
  public static BigDecimal getBalance(int accountId) {
    try {
      // Verify the account's ID
      boolean verified = verifyAccountId(accountId);
      // Set the default balance to null
      BigDecimal balance = null;
    
      // Proceed if the account ID can be verified
      if (verified) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Obtain the balance
        balance = DatabaseSelector.getBalance(accountId, connection);
        // Close the connection
        connection.close();
      }
      // Return the balance
      return balance;
      
    // If an exception is thrown --> return null
    } catch (SQLException error) {
      return null;
    }
  }
  

  /**
   * Given a type of account by ID, returns the interest rate for that account type.
   * @param accountType is an account type ID
   * @return interestRate of the given account or null if undefined
   */
  public static BigDecimal getInterestRate(int accountType) {
    try {
      // Check to see if the given type is valid
      boolean definedType = verifyTypeId(accountType);
    
      // Set the default interest rate to null
      BigDecimal interestRate = null;
    
      // Proceed iff the given type is defined in the database
      if (definedType) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Get the account type's interest rate
        interestRate = DatabaseSelector.getInterestRate(accountType, connection);
        // Close the connection
        connection.close();
      }
      // Return the interest rate
      return interestRate;
      
    // If an exception is thrown --> return null
    } catch (SQLException error) {
      return null;
    }
  }
  
  
  /**
   * Retrieves all the Account type IDs created and stored in the database.
   * @return IDs is a list of all the Account ID Types stored in the database
   */
  public static List<Integer> getAccountTypesIds() {
    try {
      // Connect to the database
      Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
      // Get the account Type IDs from the database
      ResultSet results = DatabaseSelector.getAccountTypesId(connection);
    
      // Hold the account types in a list
      List<Integer> ids = new ArrayList<>();
      // Iterate through the result set
      while (results.next()) {
        // Add each ID to the list
        ids.add(results.getInt("ID"));
      }
      // Close the connection
      connection.close();
      
      // Return the list of IDs
      return ids;
      
    // If an exception is thrown --> return null
    } catch (SQLException error) {
      return null;
    }
  }
  
  
  /**
   * Given an account type ID, returns the type of account associated with that ID.
   * @param accountTypeId is an Account Type ID
   * @return accountType is the name of the Account Type or null if undefined
   */
  public static String getAccountTypeName(int accountTypeId) {
    try {
      // Check to see if the given type is valid
      boolean definedType = verifyTypeId(accountTypeId);
    
      // Set the default response to null
      String accountType = null;
    
      // Proceed iff the given type is defined in the database
      if (definedType) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Get the account type name from the database
        accountType = DatabaseSelector.getAccountTypeName(accountTypeId, connection);
        // Close the connection
        connection.close();
      }
      // Return the account type
      return accountType;
      
    // If an exception is thrown --> return null
    } catch (SQLException error) {
      return null;
    }
  }
  
  
  /**
   * Retrieves all the Role types created and stored in the database.
   * @return IDs is a list of the roles stored in the database
   */
  public static List<Integer> getRoles() {
    try {
      // Connect to the database
      Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
      // Get a ResultSet of the roles IDs held in the database
      ResultSet results = DatabaseSelector.getRoles(connection); 
      
      // Create an an ArrayList to hold the IDs
      List<Integer> ids = new ArrayList<>();
      
      // Iterate through the ResultSet 
      while (results.next()) {
        // Add the role IDs into the list
        ids.add(results.getInt("ID"));
      }
      // Close the connection
      connection.close();
      
      // Return the list of all Role IDs
      return ids; 
      
    // If an exception is thrown --> return null
    } catch (SQLException error) {
      return null;
    }
  }

  /**
   * Given an account's unique ID, return its type ID.
   * @param accountId is an Account's Unique ID
   * @return accountTypeId is the account's type ID or -1 if undefined
   */
  public static int getAccountType(int accountId) {
    try {
      // Set the default account type to -1
      int accountTypeId = -1;

      // Connect to the database
      Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
      // Get the account's Type ID
      accountTypeId = DatabaseSelector.getAccountType(accountId, connection);
      // Close the connection
      connection.close();
      // Return the account's Type ID
      return accountTypeId;
      
    // If an exception is thrown --> return -1
    } catch (SQLException error) {
      return -1;
    }
  }
  
  
  /**
   * Given a user's unique ID, return their type ID.
   * @param userId is a User's Unique ID
   * @return userRoleId is the user's role ID or -1 if undefined
   */
  public static int getUserRole(int userId) {
    try {
      // Set the default user type to -1
      int userRoleId = -1;
    
      // Connect to the database
      Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
      // Get the user's Role ID
      userRoleId = DatabaseSelector.getUserRole(userId, connection);
      // Close the connection
      connection.close();     
      
      // Return the user's Role ID
      return userRoleId;
      
    // If an exception is thrown --> return -1
    } catch (SQLException error) {
      return -1;
    }
  }
  
  /** 
   * Given a user's unique ID, return a list of all the messages belonging to them.
   * @param userId the ID of the user receiving the messages
   * @return a list of Strings representing the messages to be given to the user
   */
  public static List<Message> getAllMessages(int userId) {
    try {
      // Verify the user's ID
      boolean verified = verifyUserId(userId);
      
      if (verified) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Get the associated messages
        ResultSet results = DatabaseSelector.getAllMessages(userId, connection);
        
        // Create a list to hold the user messages
        List<Message> associatedMessages = new ArrayList<Message>();
        
        // Iterate through the messages, adding them to the list
        while (!(results == null) && results.next()) {
          Integer msgId = results.getInt("ID");
          Integer targetId = results.getInt("USERID");
          String msg = results.getString("MESSAGE");
          Integer viewed = results.getInt("VIEWED");
          Message newMsg = new Message(targetId, msg);
          if (viewed.equals(1)) {
            newMsg.setViewedStatus(true);
          }
          newMsg.setMessageId(msgId);
          associatedMessages.add(newMsg);
        }
        connection.close();
        return associatedMessages;
        
        // If the user isn't verified
      } else {
        return null;
      }
      // If an exception occurs...
    } catch (SQLException error) {
      return null;
    }
  }
  
  /**
   * A method that will return a specific message given its ID from the database.
   * @param messageId the ID of the message from the database
   * @return the message that corresponds to that given ID
   */
  public static String getSpecificMessage(int messageId) {
    try {
      // Connect to the database
      Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
      
      // Get the message with the given message ID and return it
      String returnMessage = DatabaseSelector.getSpecificMessage(messageId, connection);
      connection.close();
      
      return returnMessage;
      
      // If an issue arises, we will return an empty String
    } catch (SQLException error) {
      System.out.println("issue");
      return "";
    }
  }
  
  
  //________________________________ Helper Methods ______________________________________
  
  /**
   * Given a user's unique ID, check whether it is in the database.
   * @param userId is the unique User Id to be verified
   * @return true if it is in the database, false otherwise
   */
  protected static boolean verifyUserId(int userId) {
    // Obtain the User object
    User validator = getUserDetails(userId);
    // Verify the given user ID
    boolean verified = !(validator == null);
    // Return the result
    return verified;
  }
  
  
  /**
   * Given a account's unique ID, check whether it is in the database.
   * @param accountId is the unique account Id to be verified
   * @return true if it is in the database, false otherwise
   */
  protected static boolean verifyAccountId(int accountId) {
    // Obtain the Account object
    Account validator = getAccountDetails(accountId);
    // Verify the given account ID
    boolean verified = !(validator == null);
    // Return the result
    return verified;
  }
  
  
  /**
   * Given a user role ID, check whether it is in the database.
   * @param roleId is the role ID to be verified
   * @return true if it is in the database, false otherwise
   */
  protected static boolean verifyRoleId(int roleId) {
    // Get the user roles that are defined
    List<Integer> validRoles = getRoles();
    // Check to see if the given type is valid
    boolean verified = !(validRoles == null) && validRoles.contains(roleId);
    // Return the result
    return verified;
  }
  
  
  /**
   * Given an account type ID, check whether it is in the database. 
   * @param typeId is the account type ID to be verified
   * @return true if it is in the database, false otherwise
   */
  protected static boolean verifyTypeId(int typeId) {
    // Get the account types that are defined
    List<Integer> validTypes = getAccountTypesIds();
    // Check to see if the given type is valid
    boolean verified = !(validTypes == null) && validTypes.contains(typeId);
    // Return the result
    return verified;
  }
}
