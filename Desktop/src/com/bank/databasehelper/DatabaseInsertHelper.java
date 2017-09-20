package com.bank.databasehelper;

import com.bank.database.DatabaseInsertException;
import com.bank.database.DatabaseInserter;
import com.bank.generics.AccountTypes;
import com.bank.generics.AccountTypesMap;
import com.bank.generics.Roles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class DatabaseInsertHelper extends DatabaseInserter {
 

  /**
   * Verifies that the given Account parameters meet database expectations, and inserts
   * the account into the database if requirements are met. 
   * 
   * @param name is the Account's name
   * @param balance is the balance remaining in the account
   * @param typeId is the enumerated type of the account
   * @return the database generated Id of the inserted Account, or -1 if the operation failed
   */
  public static int insertAccount(String name, BigDecimal balance, int typeId) {
    try {
      AccountTypesMap accTypes = new AccountTypesMap();
      // Assign the default account ID to -1 for failed operation
      int newId = -1;
      // Ensure that the balance is rounded to two decimal places
      BigDecimal roundedBalance = balance.setScale(2, RoundingMode.CEILING);
      // Verify the name is non empty or null 
      boolean verifiedName = ((!(name.equals(""))) && (!(name == null)));
    
      // Set the minimum start balance to 0
      BigDecimal min = BigDecimal.ZERO;
      // The balance should be be greater than or equal to 0.
      int aboveZero = balance.compareTo(min);
      boolean balancePositive = (aboveZero == 0 || aboveZero == 1);
      boolean isBalanceOwing = accTypes.getAccTypeName(typeId).equalsIgnoreCase("balance owing");
    
      // Get a list of all the valid Account types stored in the database
      List<Integer> validTypes;
    
      validTypes = DatabaseSelectHelper.getAccountTypesIds();
      // Verify that the list is non-empty and that the given type ID is in the list
      boolean typeIdValid = !(validTypes == null) && accTypes.hasAccTypeValue(typeId);
    
      // Proceed if the type ID is valid and the start balance is positive or 0: 
      if ((typeIdValid && balancePositive && verifiedName) || isBalanceOwing) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Add the Account to the database and get its ID
        newId = DatabaseInserter.insertAccount(name, roundedBalance, typeId, connection);
        // Close the connection
        connection.close();
      }
      
      // Return the new ID 
      return newId;
      
    // If an exception is thrown --> return -1
    } catch (SQLException error) {
      return -1;
    } catch (DatabaseInsertException error) {
      return -1;
    }
  }
  
  
  /**
   * Sets an Account Type into the database if both the type is
   * valid, and the interest rate is between 0 and 1.0.
   * @param name is the Account's type name
   * @param interestRate is the account type's interest rate
   * @return the type ID of the inserted account type or -1 if failed
   */
  public static int insertAccountType(String name, BigDecimal interestRate) {
    try {   
      // Set the default type Id to -1
      int typeId = -1;
    
      // Check to see if the account type is valid
      boolean typeValid = (validateAccountType(name));
      // Check if the interest Rate is between 0 and 1.0
      boolean interestRateValid = validateInterest(interestRate);
      if (typeValid && interestRateValid) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Insert the account type and get ID
        typeId = DatabaseInserter.insertAccountType(name, interestRate, connection);
        // Close the connection
        connection.close();
      }
      // Return the result
      return typeId;
      
    // If an exception is thrown --> return -1
    } catch (SQLException error) {
      return -1;
    } catch (DatabaseInsertException error) {
      return -1;
    }
  }
  
  
  /**
   * Inserts a new User into the database and returns their database generated ID number.
   * @param name is the User's name
   * @param age is the User's age
   * @param address is the User's address
   * @param roleId is the User's role ID
   * @param password is the User's password
   * @return the database generated ID number
   */
  public static int insertNewUser(String name, int age,
      String address, int roleId, String password) {
    try {
      // Set default return ID to -1
      int newId = -1;
    
      // Get a list of valid roles
      List<Integer> validRoles = DatabaseSelectHelper.getRoles();
      // Check that the list is non-empty at that the user's roleId is valid
      boolean roleIdValid = ! (validRoles == null) && validRoles.contains(roleId);
    
      // Check that the user's address is less than 100 characters long
      boolean addressValid = ((!(address == null)) && (address.length() <= 100));
 
      // Verify the name is non empty or null 
      boolean verifiedName = ((!(name.equals(""))) && (!(name == null)));

      // Check that the use's age is valid
      boolean validAge = (age >= 0);

      // If all restricted fields are compliant:
      if (verifiedName && roleIdValid && addressValid && validAge) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Add the new user
        newId = DatabaseInserter.insertNewUser(name, age, address, roleId, password, connection);
        // Close the connection
        connection.close();
      }
      
      // Return the new id number
      return newId;
    
    // If an exception is thrown --> return -1
    } catch (SQLException error) {
      return -1;
    } catch (DatabaseInsertException error) {
      return -1;
    }
  }
  
  /**
   * Inserts a role into the database if it is an Enum type.
   * @param role to be added
   * @return role ID of the inserted role or -1 if failed
   */
  public static int insertRole(String role) {
    try {
      // Check whether the role is valid or not
      boolean valid = validateRole(role);
      // Set default response to -1
      int roleId = -1;
    
      // If the role is valid
      if (valid) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Insert the role
        roleId = DatabaseInserter.insertRole(role, connection);
        // Close the connection
        connection.close();
      }
      // Return whether the response was completed
      return roleId;
    
      // If an exception is thrown --> return -1
    } catch (SQLException error) {
      return -1; 
    } catch (DatabaseInsertException error) {
      return -1;
    }
  }
  
  /**
   * Attributes the account with accountId to the user with userId.
   * @param userId for the User Account
   * @param accountId from the accounts' table
   * @return accountIdConfirmation is the ID of the inserted account or -1 if failed
   */
  public static int insertUserAccount(int userId, int accountId) {
    try { 
      // Set the default response to false
      int accountIdConfirmation = -1;
    
      // Validate the User ID
      boolean containsAid = DatabaseSelectHelper.verifyAccountId(accountId);
      boolean containsUid = DatabaseSelectHelper.verifyUserId(userId);
 
      // If both the userIds and accountIDs exist:
      if (containsAid && containsUid) {
        // Connect to the database
        Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
        // Add the account under the user and get token
        accountIdConfirmation = DatabaseInserter.insertUserAccount(userId, accountId, connection);
        // close the connection
        connection.close();   
      }
      // Return the result
      return accountIdConfirmation;
      
      // If an exception is thrown --> return false
    } catch (SQLException error) {
      return -1;
    } catch (DatabaseInsertException error) {
      return -1;
    }
  }
  
  /**
   * A method that will insert a user message given the user's ID and the desired message.
   * @param userId the ID for whom the message is for
   * @param message the message to be given to the user
   * @return the ID of the inserted message. Will return -1 if the message insertion was 
   *     unsuccessful
   */
  public static int insertMessage(int userId, String message) {
    try {
      // Set the default response to false
      int messageId = -1;
      
      // Validate the User ID
      boolean containsUid = DatabaseSelectHelper.verifyUserId(userId);
      
      // If the user ID is valid
      if (containsUid) {
        
        // If the length is less than or equal to 512 characters
        if (message.length() <= 512) {
          // Connect to the database
          Connection connection = DatabaseDriverHelper.connectOrCreateDataBase();
          
          // Make an attempt to insert the desired message
          messageId = DatabaseInserter.insertMessage(userId, message, connection);
          
          connection.close();
        }
      }
      return messageId;
      
    } catch (SQLException error1) {
      return -1;
    } catch (DatabaseInsertException error2) {
      return -1;
    }
  }
  
  
  //________________________________ Helper Methods________________________________
  /**
   * Returns whether or not a given account type is a valid Enum type.
   * Helper method for insertAccountType.
   * 
   * @param name is the given type name
   * @return true if the type is valid, false otherwise
   */
  protected static boolean validateAccountType(String name) {
    // Check if the given type is valid
    boolean isChequing = (name.equalsIgnoreCase(AccountTypes.CHEQUING.toString()));
    boolean isSaving = (name.equalsIgnoreCase(AccountTypes.SAVING.toString()));
    boolean isTfsa = (name.equalsIgnoreCase(AccountTypes.TFSA.toString()));
    boolean isRestrictedSavings = (name.equalsIgnoreCase(
        AccountTypes.RESTRICTEDSAVINGS.toString()));
    boolean isBalanceOwing = (name.equalsIgnoreCase(AccountTypes.BALANCEOWING.toString()));
    
    // Set default value to false
    boolean valid = false;
    // If it is a valid type
    if (isChequing || isSaving || isTfsa || isRestrictedSavings || isBalanceOwing) {
      // Change the valid
      valid = true;
    }
    // Return whether or not the given type is valid
    return valid;       
  }
  
  /**
   * Returns whether or not a given role is a valid Enum type.
   * Helper method for insertRole.
   * 
   * @param name is the given type name
   * @return true if the type is valid, false otherwise
   */
  protected static boolean validateRole(String name) {
    // Check if the given role is valid
    boolean isAdmin = (name.equalsIgnoreCase(Roles.ADMIN.toString()));
    boolean isTeller = (name.equalsIgnoreCase(Roles.TELLER.toString()));
    boolean isCustomer = (name.equalsIgnoreCase(Roles.CUSTOMER.toString()));
    
    // Set default value to false
    boolean valid = false;
    // If it is a valid type
    if (isAdmin || isTeller || isCustomer) {
      // Change the valid
      valid = true;
    }
    // Return whether or not the given type is valid
    return valid;       
  }
  
  
  /**
   * Validates whether a specified interest rate is valid.
   * Helper method for insertAccountType.
   * 
   * @param interestRate is the interestRate for the account type.
   * @return true if 0.0 <= interestRate < 1.0, false otherwise
   */
  protected static boolean validateInterest(BigDecimal interestRate) {
    BigDecimal noInterest = new BigDecimal("0.00");
    BigDecimal highInterest = new BigDecimal("1.00");
    
    // Will be 1 or 0 for >= 0.0
    int aboveMin = interestRate.compareTo(noInterest);
    // Will be -1 for < 1.0
    int belowMax = interestRate.compareTo(highInterest);
    // Check if the interest is in range
    boolean inRange = ((aboveMin == 0 || aboveMin == 1) && belowMax == -1);
    // Return the result
    return inRange;
  }
 
}
