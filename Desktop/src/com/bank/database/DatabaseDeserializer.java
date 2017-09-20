package com.bank.database;

import com.bank.databasehelper.DatabaseInsertHelper;
import com.bank.databasehelper.DatabaseUpdateHelper;
import com.bank.exceptions.ConnectionFailedException;
import com.bank.generics.AccountTypesMap;
import com.bank.generics.RolesMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class DatabaseDeserializer implements Serializable {

  private static final long serialVersionUID = 9215842159911054688L;
  private Map<String, HashMap<Integer, ArrayList<String>>> database = new HashMap<String, 
      HashMap<Integer, ArrayList<String>>>();
  
  
  /**
   * A constructor for the DatabaseDeserializer class. 
   */
  public DatabaseDeserializer() throws ConnectionFailedException {
    DatabaseDriver.reInitialize();
  }
  
  /**
   * If an exception arises, this.databas will be set to null by default and the database will be
   *  populated in a different way.
   * @throws IOException if any issue occurs
   * @throws ClassNotFoundException if we fail to read the serialized object
   */
  @SuppressWarnings("unchecked")
  public void deSerialize() {
    try {
      // Get the file containing the copy of the database and create an objecto ut of it
      FileInputStream fileName = new FileInputStream("database_copy.ser");
      ObjectInputStream serializedDatabase = new ObjectInputStream(fileName);
      // Attempt to read the object as a Hash Map where the key is the table name and the value
      // is another Hash Map where the key is the ID and the value is an array list of its
      // corresponding row entries
      this.database = (HashMap<String, HashMap<Integer, ArrayList<String>>>)
          serializedDatabase.readObject();
      this.readRoles();
      this.readAccountTypes();
      this.readAccounts();
      this.readUsers();
      this.readUserAccounts();
      this.readUserMessages();
      // Be sure that the connection is closed
      fileName.close();
      serializedDatabase.close();
      // If we ever get an error, the database will be null by default.
    } catch (IOException error1) {
      this.database = null;
    } catch (ClassNotFoundException error2) {
      this.database = null;
    } 
  }
  
  
  /**
   * A method that will get the roles hash map in the deserialized database hash map and add it 
   * into the database.
   */
  public void readRoles() {
    // If the database is a valid hash map...
    if (!(this.database == null)) {
      // Get the Roles table from the Hash Map
      HashMap<Integer, ArrayList<String>> roleTypes = database.get("Roles");
      
      // Iterate over each row entry in the Hash Map
      for (Entry<Integer, ArrayList<String>> entry : roleTypes.entrySet()) {
        
        // Get the array list containing the row entries
        ArrayList<String> roleEntry = entry.getValue();
        
        // Since we're dealing with the roles table, we know that the only piece of information
        // in the row that's important to us (aside from its ID) is its corresponding name.
        String role = roleEntry.get(0);
        
        // ------------------------STILL NEED TO ACCOUNT FOR NONEXISTENT ROLES--------------------
        DatabaseInsertHelper.insertRole(role);
      }
      // If the hash map is invalid, we will insert the 3 default roles into the database.
    } else {
      DatabaseInsertHelper.insertRole("ADMIN");     
      DatabaseInsertHelper.insertRole("TELLER");    
      DatabaseInsertHelper.insertRole("CUSTOMER");  
    }
  }
  
  
  /**
   * A method that will read the account types hash map from the deserialized database hash map and
   *  add it into the database.
   */
  public void readAccountTypes() {
    // If the database is a valid Hash Map...
    if (!(this.database == null)) {
      // Get the Account Types table from the Hash Map
      HashMap<Integer, ArrayList<String>> accountTypes = database.get("AccountType");
      
      // Iterate over each row entry in the Hash Map
      for (Entry<Integer, ArrayList<String>> entry : accountTypes.entrySet()) {
        
        // Get the array list containing the row entries
        ArrayList<String> accTypeEntry = entry.getValue();
        
        // We will need the account type's name and its interest rate
        String accType = accTypeEntry.get(0);
        String accTypeInterest = accTypeEntry.get(1);
        BigDecimal accTypeDecimal = new BigDecimal(accTypeInterest);
        DatabaseInsertHelper.insertAccountType(accType, accTypeDecimal);
      }
    } else {
      // Define the Account type Interest Rates:
      BigDecimal chequingInterest = new BigDecimal("0.03");
      BigDecimal savingsInterest = new BigDecimal("0.05");
      BigDecimal tfsaInterest = new BigDecimal("0.07");
      
      // Define the Account Types with their interest rates
      DatabaseInsertHelper.insertAccountType("CHEQUING", chequingInterest);  
      DatabaseInsertHelper.insertAccountType("SAVINGS", savingsInterest);
      DatabaseInsertHelper.insertAccountType("TFSA", tfsaInterest);
      DatabaseInsertHelper.insertAccountType("RESTRICTED SAVINGS", savingsInterest);
      DatabaseInsertHelper.insertAccountType("BALANCE OWING", chequingInterest);  
    }
  }
  
  
  /**
   * A method that will read the users hash map from the deserialized database hash map and insert 
   * it into the database.
   */
  public void readUsers() {
    // If the database is a valid Hash Map...
    if (!(this.database == null)) {
      RolesMap roles = new RolesMap();
      HashMap<Integer, ArrayList<String>> users = database.get("Users");
      
      // Iterate over each row entry in the Hash Map
      for (Entry<Integer, ArrayList<String>> entry : users.entrySet()) {
        
        // Get the array list containing the row entries
        ArrayList<String> userDataEntries = entry.getValue();
        
        // Get the user's name, agem address, role, and password (as a hashed entry)
        String userName = userDataEntries.get(0);
        String userAge = userDataEntries.get(1);
        Integer userAgeInt = new Integer(userAge);
        String userAddress = userDataEntries.get(2);
        String userRole = userDataEntries.get(3);
        Integer userRoleInt = roles.getRoleId(userRole);
        String userPass = userDataEntries.get(4);
        
        // Insert the info into the database using a dummy password
        int userId = DatabaseInsertHelper.insertNewUser(userName, userAgeInt, userAddress,
            userRoleInt, "temp");
        // Update the newly created user with their hashed password from the deserialized 
        // database.
        DatabaseUpdateHelper.updateUserPassword(userPass, userId);     
      }
    }
  }
  
  
  /**
   * A method that will read the accounts hash map from the deserialized database hash map and
   * insert it into the database.
   */
  public void readAccounts() {
    // If the database is a valid Hash Map...
    if (!(this.database == null)) {
      AccountTypesMap accTypes = new AccountTypesMap();
      HashMap<Integer, ArrayList<String>> accounts = database.get("Accounts");
      
      // Iterate over each row entry in the Hash Map
      for (Entry<Integer, ArrayList<String>> entry : accounts.entrySet()) {
        
        // Get the array list containing the row entries.
        ArrayList<String> accountDataEntries = entry.getValue();
        
        // Get the account's name, balance, and type
        String accName = accountDataEntries.get(0);
        String accBalance = accountDataEntries.get(1);
        BigDecimal accBalanceDecimal = new BigDecimal(accBalance);
        String accType = accountDataEntries.get(2);
        Integer accTypeInt = accTypes.getAccTypeId(accType);
        
        // Insert the account into the database;
        DatabaseInsertHelper.insertAccount(accName, accBalanceDecimal, accTypeInt);     
      }
    }
  }
  
  
  /**
   * A method that will read the user accounts hash map from the deserialized database hash map and
   * insert it into the database.
   */
  public void readUserAccounts() {
    // If the database is a valid Hash Map...
    if (!(this.database == null)) {
      HashMap<Integer, ArrayList<String>> userAccounts = database.get("UserAccounts");
      
      // Iterate over each row entry in the Hash Map
      for (Entry<Integer, ArrayList<String>> entry : userAccounts.entrySet()) {
        
        // Get the array list containing the row entries.
        ArrayList<String> accountDataEntries = entry.getValue();
        
        // Get the user's ID
        Integer userId = entry.getKey();
        
        // Iterate over each account that belongs to the user and create the user/account
        // relationship within the database.
        for (String usAcc : accountDataEntries) {
          Integer accId = new Integer(usAcc);
          DatabaseInsertHelper.insertUserAccount(userId, accId);
        } 
      }
    }
  }
  
  
  /**
   * A method that will read the user messages hash map from the deserialized database hash map and
   * insert it into the database.
   */
  public void readUserMessages() {
    // If the database is a valid Hash Map...
    if (!(this.database == null)) {
      HashMap<Integer, ArrayList<String>> userMessages = database.get("UserMessages");
      
      // Iterate over each row entry in the Hash Map
      for (Entry<Integer, ArrayList<String>> entry : userMessages.entrySet()) {
        // Get the array list containing the row entries.
        ArrayList<String> userMsgEntries = entry.getValue();
        
        // Get the message's target, value, and status
        String targetId = userMsgEntries.get(0);
        Integer targetIdInt = new Integer(targetId);
        String msg = userMsgEntries.get(1);
        String viewStatus = userMsgEntries.get(2);
        
        int newMsgId = DatabaseInsertHelper.insertMessage(targetIdInt, msg);
        if (viewStatus.equals("1")) {
          DatabaseUpdateHelper.updateUserMessageState(newMsgId);
        }
      }
    }
  }
}
