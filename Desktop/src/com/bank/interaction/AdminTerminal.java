package com.bank.interaction;

import com.bank.accounts.Account;
import com.bank.database.DatabaseDeserializer;
import com.bank.database.DatabaseSerializer;
import com.bank.databasehelper.DatabaseInsertHelper;
import com.bank.databasehelper.DatabaseSelectHelper;
import com.bank.databasehelper.DatabaseUpdateHelper;
import com.bank.exceptions.ConnectionFailedException;
import com.bank.generics.Roles;
import com.bank.generics.RolesMap;
import com.bank.messages.Message;
import com.bank.users.Admin;
import com.bank.users.Customer;
import com.bank.users.Teller;
import com.bank.users.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class AdminTerminal implements Terminal {
  
  protected Admin currentUser;
  protected boolean currentUserAuthenticated;
  protected List<Admin> administrators = new ArrayList<Admin>();
  protected List<Teller> tellers = new ArrayList<Teller>();
  protected List<Customer> customers = new ArrayList<Customer>();
  
  
  /**
   * Constructor for AdminTerminal, authenticating the current Admin given their password.
   * @param adminId is the admin's Database generated unique ID.
   * @param password is the teller provided password.
   */
  public AdminTerminal(int adminId, String password) {
    // Get the current teller using their Database generated ID number
    this.currentUser = (Admin) DatabaseSelectHelper.getUserDetails(adminId);
    // Authenticate the teller using their password and set the class attribute
    boolean authenticationStatus = this.currentUser.authenticate(password);
    // This will set the authentication to true or false
    this.currentUserAuthenticated = authenticationStatus;
    
    // This will make sure that we always have an up-to-date list of all the people in the bank
    this.listAllAdmins();
    this.listAllCustomers();
    this.listAllTellers();
  }
  
  
  /**
   * Creates a new teller with the given attributes and adds them into the database.
   * 
   * @param name is the teller's name.
   * @param age is the teller's age.
   * @param address is the teller's address.
   * @param password is the teller's desired password.
   * @return the teller's database generated ID number or -1 if not added
   */
  public int makeNewUser(String name, int age, String address, String password) {
    // Set the default response to false
    int tellerId = -1;
    
    // Check that the current user is authenticated
    if (this.currentUserAuthenticated) {
      // Locate the teller role ID
      int roleId = locateRoleId("teller");
      // Create a Teller user in the database
      tellerId = DatabaseInsertHelper.insertNewUser(name, age, address, roleId, password);
    }
    // Return the teller's ID number
    return tellerId;
  }
  
  
  /**
   * Creates a new user with the given attributes and adds them into the database.
   * 
   * @param role is the type of user to be added
   * @param name is the user's name.
   * @param age is the user's age.
   * @param address is the user's address.
   * @param password is the user's desired password.
   * @return the user's database generated ID number or -1 if not added
   */
  public int makeNewUser(String role, String name, int age, String address, String password) {
    // Set the default response to false
    int userId = -1;
    int roleId = -1;
    
    // Check that the current user is authenticated
    if (this.currentUserAuthenticated) {
      // Locate the teller role ID
      roleId = locateRoleId(role);
      // Create a Teller user in the database
      userId = DatabaseInsertHelper.insertNewUser(name, age, address, roleId, password);
    }
    // Return the teller's ID number
    return userId;
  }
  
  
  /**
   * Returns a list of all the admins in the database.
   * @return names is a list of all admins in the database
   */
  public List<Admin> listAllAdmins() {
    this.administrators = new ArrayList<Admin>();
    boolean doneListing = false;
    int idNumber = 1;
    int adminRoleId = this.locateRoleId("admin");
    while (! doneListing) {
      User potentialAdmin = DatabaseSelectHelper.getUserDetails(idNumber);
      if (!(potentialAdmin == null)) {
        if (potentialAdmin.getRoleId() == adminRoleId) {
          boolean foundDuplicate = false;
          // Check if the user already exists in the list
          for (Admin admin : this.administrators) {
            if (admin.getId() == idNumber) {
              foundDuplicate = true;
            }
          }
          if (!(foundDuplicate)) {
            this.administrators.add((Admin)potentialAdmin);
          } 
        }  
      } else {
        doneListing = true;
      }
      idNumber ++;
    }
    return this.administrators;
  }
  
  
  /**
   * Returns a list of all the tellers in the database.
   * @return names is a list of all tellers in the database
   */
  public List<Teller> listAllTellers() {
    this.tellers = new ArrayList<Teller>();
    boolean doneListing = false;
    int idNumber = 1;
    int tellerRoleId = this.locateRoleId("teller");
    while (! doneListing) {
      User potentialTeller = DatabaseSelectHelper.getUserDetails(idNumber);
      if (!(potentialTeller == null)) {
        if (potentialTeller.getRoleId() == tellerRoleId) {
          boolean foundDuplicate = false;
          // Check if the user already exists in the list
          for (Teller teller : this.tellers) {
            if (teller.getId() == idNumber) {
              foundDuplicate = true;
            }
          }
          if (!(foundDuplicate)) {
            this.tellers.add((Teller) potentialTeller);
          } 
        }  
      } else {
        doneListing = true;
      }
      idNumber ++;
    }   
    return this.tellers;
  }
  
  
  /**
   * Returns a list of all the customers in the database.
   * @return names is a list of all customers in the database
   */
  public List<Customer> listAllCustomers() {
    this.customers = new ArrayList<Customer>();
    boolean doneListing = false;
    int idNumber = 1;
    int customerRoleId = this.locateRoleId("customer");
    while (! doneListing) {
      User potentialCustomer = DatabaseSelectHelper.getUserDetails(idNumber);
      if (!(potentialCustomer == null)) {
        if (potentialCustomer.getRoleId() == customerRoleId) {
          boolean foundDuplicate = false;
          // Check if the user already exists in the list
          for (Customer customer : this.customers) {
            if (customer.getId() == idNumber) {
              foundDuplicate = true;
            }
          }
          if (!(foundDuplicate)) {
            this.customers.add((Customer) potentialCustomer);
          } 
        }  
      } else {
        doneListing = true;
      }
      idNumber ++;
    } 
    return this.customers;
  }
  
  
  /**
   * Returns the total balance of all the accounts in the bank database.
   * @return the total balance of all accounts
   */
  public BigDecimal viewTotalBalance() {
    // Initialize the big decimal to be returned
    BigDecimal totalBalance = new BigDecimal(0);
    
    // Iterate through the list of administrators and get all their accounts (if any) in order
    // to add all their balances together
    for (Admin admin : this.administrators) {
      Integer adminId = admin.getId();
      List<Integer> adminAccounts = DatabaseSelectHelper.getAccountIds(adminId);
      for (Integer adminAccId : adminAccounts) {
        totalBalance = totalBalance.add(DatabaseSelectHelper.getBalance(adminAccId));
      }
      DatabaseInsertHelper.insertMessage(admin.getId(), "System Message: "
          + "\n A system administrator has "
          + "reviewed the balance of one or more of your accounts.");
    }
    
    // Iterate through the list of tellers and get all their accounts (if any) in order
    // to add all their balances together
    for (Teller teller : this.tellers) {
      Integer tellerId = teller.getId();
      List<Integer> tellerAccounts = DatabaseSelectHelper.getAccountIds(tellerId);
      for (Integer tellerAccId : tellerAccounts) {
        totalBalance = totalBalance.add(DatabaseSelectHelper.getBalance(tellerAccId));
      }
      DatabaseInsertHelper.insertMessage(teller.getId(), "System Message: "
          + "\n A system administrator has "
          + "reviewed the balance of one or more of your accounts.");
    }
    
    // Iterate through the list of customers and get all their accounts (if any) in order
    // to add all their balances together
    for (Customer customer : this.customers) {
      Integer customerId = customer.getId();
      List<Integer> customerAccounts = DatabaseSelectHelper.getAccountIds(customerId);
      for (Integer customerAccId : customerAccounts) {
        totalBalance = totalBalance.add(DatabaseSelectHelper.getBalance(customerAccId));
      }
      DatabaseInsertHelper.insertMessage(customer.getId(), "System Message: "
          + "\n A system administrator has "
          + "reviewed the balance of one or more of your accounts.");
    }
    return totalBalance;
  }
  
  
  /**
   * A method that will promote a given teller to administrator status.
   * @param tellerId the ID of the teller
   * @return true if the teller was promoted, otherwise false
   */
  public boolean promoteTellerToAdmin(int tellerId) {
    // Instantiate a roles map in order to get all of the current roles in the database as well
    // as a boolean to be returned at the end
    RolesMap roleMap = new RolesMap();
    boolean foundTeller = false;
    // Iterate through the current list of tellers and check their IDs against the given ID
    for (Teller chosenTeller : this.tellers) {
      int chosenTellerId = chosenTeller.getId();
      // If the IDs are a match, we can proceed to promoting the given teller to admin status.
      if (chosenTellerId == tellerId) {
        DatabaseUpdateHelper.updateUserRole(roleMap.getRoleId("admin"), tellerId);
        foundTeller = true;
      }
    }
    return foundTeller;
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
   * A method that will make an attempt to serialize the current database. If an IO exception is
   * caught, a message will be printed out.
   */
  public void serializeDatabase() {
    DatabaseSerializer serializeDb = new DatabaseSerializer();
    try {
      serializeDb.serialize();
    } catch (IOException e) {
      System.out.println("Something went wrong with the serialization :(");
    }
  }
  
  
  /**
   * A method that will make an attempt to deserialize a serialized database.
   */
  public void deserializeDatabase() {
    try {
      DatabaseDeserializer deserializeDb = new DatabaseDeserializer();
      deserializeDb.deSerialize();
    } catch (ConnectionFailedException e) {
      System.out.println("Unable to deserialize the database.");
    }
  }
  
  
  /**
   * A method that will fetch a message from the database given its ID.
   * @param messageId the ID of the message
   * @return the string representation of a message
   */
  public String viewMessage(int messageId) {
    String message = DatabaseSelectHelper.getSpecificMessage(messageId);
    return message;
  }
  
  
  /**
   * A method that will fetch a list of messages from the database. These messages will belong
   * to the person calling the method (in this case, the admin).
   * @return a list of the admin's messages
   */
  public List<Message> viewOwnMessages() {
    // Get a list of all the messages belonging to the admin and reinsert them into the database
    // in order to get their ID and update the state of the message
    List<Message> messages = DatabaseSelectHelper.getAllMessages(this.currentUser.getId());
    return messages;
  }
  
  
  /**
   * A method that will allow an admin to leave a message to someone.
   * @param message the message to be left
   */
  public int leaveMessage(String message, int targetId) {
    int msgId = -1;  
    if (!(message == null)) {
      User potentialTarget = DatabaseSelectHelper.getUserDetails(targetId);
      if (!(potentialTarget == null)) {
        String newMsg = "FROM: " + this.currentUser.getName() + "\n" + "TO: " 
            + potentialTarget.getName() + "\n" + "MESSAGE: " + "\n" + message;
        System.out.println(newMsg);
        msgId = DatabaseInsertHelper.insertMessage(targetId, newMsg);
      }
    }
    return msgId;
  }
  
  //________________________________ Helper Methods________________________________
  
  /**
   * Locates the id in the database that corresponds with the Teller role.
   * @return the id that reflects the Teller role
   */
  public int locateRoleId(String role) {
    // Instantiate a role map
    RolesMap rolesMap = new RolesMap();
    EnumMap<Roles, String> currentRoles = rolesMap.getExistingRoles();
    int roleId = -1;
    // Locate and return the corresponding role ID
    for (Roles roleKey : currentRoles.keySet()) {
      if (roleKey.toString().equalsIgnoreCase(role)) {
        String roleValue = currentRoles.get(roleKey);
        Integer foundRoleId = new Integer(roleValue);
        roleId = foundRoleId;
      }
    }
    return roleId;
  }

}

