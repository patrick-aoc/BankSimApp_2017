package com.bank.databasehelper;

import com.bank.database.DatabaseUpdater;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;


public class DatabaseUpdateHelper extends DatabaseUpdater {
  
  /**
   * Given a role ID and replacement role name, updates the role type name.
   * @param name is the new new Role Name
   * @param id is the Role ID of the account type
   * @return true if the update was completed, false otherwise
   */
  public static boolean updateRoleName(String name, int id) {
    try {
      // Verify the given role ID
      boolean verifiedRid = DatabaseSelectHelper.verifyRoleId(id);
      // Verify that the name is an enumerated type
      boolean verifiedName = DatabaseInsertHelper.validateRole(name);
    
      // Set the default response to false
      boolean complete = false;
    
      // Proceed if both the user ID and new role ID can be verified
      if (verifiedRid && verifiedName) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Update the role ID with the new name
        complete = DatabaseUpdater.updateRoleName(name, id, connection);
        // Close the connection
        connection.close();
      }
      // Return the result
      return complete;
      
    // If an exception is thrown --> return false
    } catch (SQLException error) {
      return false;
    }
  }
  
  
  /**
   * Given a user's ID number and new name, updates their name in the database.
   * @param name is the user's desired name change
   * @param id is user's ID number
   * @return true if the update was completed, false otherwise
   */
  public static boolean updateUserName(String name, int id) {
    try {
      // Verify the user's ID
      boolean verifiedId = DatabaseSelectHelper.verifyUserId(id);
      // Verify the name is non empty or null 
      boolean verifiedName = ((!(name.equals(""))) && (!(name == null)));
      
      // Set the default response to false
      boolean complete = false;
    
      // Proceed if the user ID can be verified
      if (verifiedId && verifiedName) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Update the user's name and get statement
        complete = DatabaseUpdater.updateUserName(name, id, connection);
        // Close the connection
        connection.close();
      }
      // Return the result
      return complete;
      
    // If an exception is thrown --> return false
    } catch (SQLException error) {
      return false;
    }
  }
  
  
  /**
   * Given a user's ID number and new age, updates their age in the database.
   * @param age is the user's new age
   * @param id is the user's ID number
   * @return true if the update was completed, false otherwise 
   */
  public static boolean updateUserAge(int age, int id) {
    try { 
      // Verify the user's ID
      boolean verified = DatabaseSelectHelper.verifyUserId(id);
      // Verify that the user's age is positive
      boolean validAge = (age >= 0);
      // Set the default response to false
      boolean complete = false;
    
      // Proceed if the user ID can be verified and the new age is valid
      if (verified && validAge) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Update the user's age and get statement
        complete = DatabaseUpdater.updateUserAge(age, id, connection);
        // Close the connection
        connection.close();
      }
      // Return the result
      return complete;
      
    // If an exception is thrown --> return false
    } catch (SQLException error) {
      return false;
    }
  }
  
  
  /**
   * Given a user's ID number and the role ID of the new role the user is taking on,
   * replaces the user's role with the new role if it is defined.
   * 
   * @param roleId is the ID of the new role the user is taking on
   * @param id is the user's current unique ID number
   * @return true if the update was completed, false otherwise  
   */
  public static boolean updateUserRole(int roleId, int id) {
    try {
      // Verify the user's unique ID
      boolean verifiedUid = DatabaseSelectHelper.verifyUserId(id);
      // Verify the user's role ID
      boolean verifiedRid = DatabaseSelectHelper.verifyRoleId(roleId);
    
      // Set the default response to false
      boolean complete = false;
    
      // Proceed if both the user ID and new role ID can be verified
      if (verifiedUid && verifiedRid) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Update the user's role and get statement
        complete = DatabaseUpdater.updateUserRole(roleId, id, connection);
        // Close the connection
        connection.close();
      }
      // Return the result
      return complete;
      
    // If an exception is thrown --> return false
    } catch (SQLException error) {
      return false;
    }
  }
  
  
  /**
   * Given a user's ID number and their new address, update their address in the database.
   * @param address is the user's new address
   * @param id is the user's current unique ID number
   * @return true if the update was completed, false otherwise 
   */
  public static boolean updateUserAddress(String address, int id) {
    try {
      // Verify the user's ID
      boolean verifiedUid = DatabaseSelectHelper.verifyUserId(id);
      // Verify the user's new address length is under 100 characters
      boolean verifiedAddress = ((!(address == null)) && !(address.length() > 100));
    
      // Set the default response to false
      boolean complete = false;
    
      // Proceed if both the user ID and new address can be verified
      if (verifiedUid && verifiedAddress) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Update the user's address and get statement
        complete = DatabaseUpdater.updateUserAddress(address, id, connection);
        // Close the connection
        connection.close();
      }
      // Return the result
      return complete;
      
    // If an exception is thrown --> return false
    } catch (SQLException error) {
      return false;
    }
  }

  
  /**
   * Given an  account's unique ID and new name, update its name in the database.
   * @param name is the account's new name
   * @param id is the account's unique database generated ID number
   * @return true if the update was completed, false otherwise
   */
  public static boolean updateAccountName(String name, int id) {
    try {
      // Verify the account's ID
      boolean verifiedId = DatabaseSelectHelper.verifyAccountId(id);    
      // Verify the name is non empty or null 
      boolean verifiedName = ((!(name.equals(""))) && (!(name == null)));
      
      // Set the default response to false
      boolean complete = false;
    
      // Proceed if the account ID can be verified
      if (verifiedId && verifiedName) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Update the account's name and get statement
        complete = DatabaseUpdater.updateAccountName(name, id, connection);
        // CLose the connection
        connection.close();
      }
      // Return the result
      return complete;
      
    // If an exception is thrown --> return false
    } catch (SQLException error) {
      return false;
    }
  }
  
  
  /**
   * Given an  account's unique ID and balance, update the balance in the database.
   * @param balance is the new balance remaining in the account
   * @param id is the account's unique database generated ID number
   * @return true if the update was completed, false otherwise
   */
  public static boolean updateAccountBalance(BigDecimal balance, int id) {
    try {
      // Verify the account's ID
      boolean verified = DatabaseSelectHelper.verifyAccountId(id);
      // Set the default response to false
      boolean complete = false;
    
      // Proceed if the account ID can be verified
      if (verified) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Update the account's balance and get statement
        complete = DatabaseUpdater.updateAccountBalance(balance, id, connection);
        // Close the connection
        connection.close();
      }
      // Return the result
      return complete;
      
    // If an exception is thrown --> return false
    } catch (SQLException error) {
      return false;
    }
  }
    
  
  /**
   * Given an  account's unique ID and an account type ID, change the account's type.
   * @param typeId is the type ID of the new type
   * @param id is the account's unique database generated ID number
   * @return true if the update was completed, false otherwise
   */
  public static boolean updateAccountType(int typeId, int id) {
    try {
      // Verify the account's ID
      boolean verifiedUid = DatabaseSelectHelper.verifyAccountId(id);
      // Verify the prospective type ID
      boolean verifiedType = DatabaseSelectHelper.verifyTypeId(typeId);
      // Set the default response to false
      boolean complete = false;
    
      // Proceed if both the account ID and its new type can be verified
      if (verifiedUid && verifiedType) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Change the account type and get statement
        complete = DatabaseUpdater.updateAccountType(typeId, id, connection);
        // Close the connection
        connection.close();
      }
      // Return the result
      return complete;
      
    // If an exception is thrown --> return false
    } catch (SQLException error) {
      return false;
    }
  }
  
  
  /**
   * Given an account type ID and new name for that account type, update the account type
   * update the account type in the database.
   * 
   * @param name is the new name for the account type
   * @param id is an account type ID
   * @return true if the update was completed, false otherwise 
   */
  public static boolean updateAccountTypeName(String name, int id) {
    try {
      // Verify the account type ID
      boolean verifiedType = DatabaseSelectHelper.verifyTypeId(id);
      // Verify that the new name is one of the enumerated types
      boolean verifiedName = DatabaseInsertHelper.validateAccountType(name);
    
      // Set the default response to false
      boolean complete = false;
    
      // Proceed if both the account type and name are valid
      if (verifiedType && verifiedName) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Change the type name and get statement
        complete = DatabaseUpdater.updateAccountTypeName(name, id, connection);
        // Close the connection
        connection.close();
      }
      // Return the result
      return complete;
      
    // If an exception is thrown --> return false
    } catch (SQLException error) {
      return false;
    }
  }
  
  
  /**
  * Given an account type ID and new interest rate for that account type,
  * update the account type interest rate in the database.
  * 
  * @param interestRate is the new interest rate for the account type
  * @param id is an account type ID
  * @return true if the update was completed, false otherwise
  */
  public static boolean updateAccountTypeInterestRate(BigDecimal interestRate, int id) {
    try {
      // Verify the account type ID
      boolean verifiedType = DatabaseSelectHelper.verifyTypeId(id);
      // Verify that the new interest rate is valid
      boolean verifiedInterestRate = DatabaseInsertHelper.validateInterest(interestRate);
    
      // Set the default response to false
      boolean complete = false;
    
      // Proceed if both the account type and new balance rate are valid
      if (verifiedType && verifiedInterestRate) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Update the interest rate and get statement
        complete = DatabaseUpdater.updateAccountTypeInterestRate(interestRate, id, connection);
        // Close the connection
        connection.close();
      }
      // Return the result
      return complete;
      
    // If an exception is thrown --> return false
    } catch (SQLException error) {
      return false;
    }
  }
  
  /**
   * Given a hashed password and a unique (and valid) user ID, make an attempt to update the given
   * user's password in the database.
   * @param password the hashed version of a password
   * @param userId the ID of the user who will get a password update
   * @return true if the update was successful, otherwise false
   */
  public static boolean updateUserPassword(String password, int userId) {
    try {
      
      // Verify that the given user is a valid one
      boolean validUser = DatabaseSelectHelper.verifyUserId(userId);
      boolean updateSuccess = false;
      
      // If the user is valid, make an attempt to update their password
      if (validUser && !(password == null)) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        
        updateSuccess = DatabaseUpdater.updateUserPassword(password, userId, connection);
        connection.close();
      }
      
      return updateSuccess;
      
      // If an issue arises with updating the password, return false
    } catch (SQLException error) {
      return false;
    }
  }
  
  /**
   * Given the ID of a message, this method will make an attempt to update the status of said 
   * message to "Viewed".
   * @param messageId the ID of the message to have a change in view status
   * @return true if the update was successful, otherwise false
   */
  public static boolean updateUserMessageState(int messageId) {
    try {
      // Connect to the database and make the attempt to update the status of the message.
      Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
      boolean updateSuccess = DatabaseUpdater.updateUserMessageState(messageId, connection);
      connection.close();
      
      return updateSuccess;
      
      // If an issue arises with updating the state of a given message, return false
    } catch (SQLException error) {
      return false;
    }
  }
}
