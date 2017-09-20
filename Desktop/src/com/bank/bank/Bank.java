package com.bank.bank;

import com.bank.accounts.Account;
import com.bank.databasehelper.DatabaseInsertHelper;
import com.bank.databasehelper.DatabaseSelectHelper;
import com.bank.databasehelper.DatabaseUpdateHelper;
import com.bank.exceptions.ConnectionFailedException;
import com.bank.exceptions.IllegalAmountException;
import com.bank.exceptions.InsufficientFundsException;
import com.bank.exceptions.InsufficientPrivilegesException;
import com.bank.generics.AccountTypesMap;
import com.bank.generics.RolesMap;
import com.bank.interaction.AdminTerminal;
import com.bank.interaction.Atm;
import com.bank.interaction.TellerTerminal;
import com.bank.messages.Message;
import com.bank.users.Admin;
import com.bank.users.Customer;
import com.bank.users.Teller;
import com.bank.users.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class Bank {
  
  /**
   * Takes user input from the keyboard.
   * @return the user input.
   */
  public static String getInputAllChars() {
    // Instantiate a buffered reader to get the input
    InputStreamReader userInput = new InputStreamReader(System.in);
    BufferedReader inputReader = new BufferedReader(userInput);
    String userSelection;
    try {
      userSelection = inputReader.readLine();
    } catch (Exception e) {
      // Exception -> return null
      userSelection = null;
    }
    return userSelection; 
  }
  
  /**
   * Takes user input from the keyboard and returns numeric string translation.
   * @return the user input if numeric, otherwise "0.00"
   */
  public static String getInputNumericChars() {
    // Instantiate a buffered reader to get the input
    InputStreamReader userInput = new InputStreamReader(System.in);
    BufferedReader inputReader = new BufferedReader(userInput);
    String userSelection; 
    try {
      // Check if the String is numeric
      userSelection = inputReader.readLine();
      new BigDecimal(userSelection); 
    } catch (Exception e) {
      // Exception -> "0.00"
      userSelection = "0.00";
    }
    return userSelection; 
  }
  
  
  /**
   * Takes user input, and converts it to an integer if the input is numeric, else returns -1.
   * @return userSelection is the integer conversion of a numeric user input, otherwise -1.
   */
  public static Integer getInputNumeric() {
    // Instantiate a buffered reader to get the input
    InputStreamReader userInput = new InputStreamReader(System.in);
    BufferedReader inputReader = new BufferedReader(userInput);
    Integer userSelection;
    try {
      userSelection = Integer.valueOf(inputReader.readLine());
    } catch (Exception e) {
      // Exception -> return -1
      userSelection = -1;
    }
    return userSelection; 
  }
  
  
  /**
   * Bank Application Program.
   * 1. Select [-1] to complete initial system setup
   * 2. Enter Administrative Mode to Enter Administrative Mode
   * 3. Access Main menu to choose run mode
   * @param argv unused.
   */
  public static void main(String[] argv) {
    Connection connection = DatabaseDriverExtender.connectOrCreateDataBase();
    Admin systemAdministrator = null;
    boolean systemSetupComplete = false;
    boolean typeRoleInstalled = false;
    boolean leaveAtmInterface;
    // Determine whether the role and type IDs have already been entered into the database.
    List<Integer> databaseRoles = DatabaseSelectHelper.getRoles();
    List<Integer> databaseAccountTypes = DatabaseSelectHelper.getAccountTypesIds();
    systemSetupComplete = ((!(databaseRoles == null)) && (!(databaseAccountTypes == null)));
    
    try { 
      // Create an InputStreamReader and BufferedReader to read user input
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader reader = new BufferedReader(isReader);
      
      // Terminal exit token
      boolean exitTerminal = false;  
      do {
        // Provide the user with an input menu
        System.out.println(" .______________________________________________.");
        System.out.println(" |            < SYSTEM STARTUP MENU >           |");
        System.out.println(" |----------------------------------------------|");
        System.out.println(" | Please select one of the following options:  |");
        System.out.println(" |- - - - - - - - - - - - - - - - - - - - - - - |");
        System.out.println(" | [-1]           Initial System Setup          |");
        System.out.println(" | [ 1]           Enter Administrative Mode     |");
        System.out.println(" |- - - - - - - - - - - - - - - - - - - - - - - |");
        System.out.println(" | [Other]        MAIN INTERFACE MENU           |");
        System.out.println(" |______________________________________________|");
        
        // Obtain the user selection
        String selection = reader.readLine();
        
        //________________________________ [-1]M: SYSTEM SETUP ________________________________
        /*
         *  (-1) Initial System Setup
         *  Only to be performed once on first run without a prior system administrator set or
         *  if the system administrator could not be added. Default values will only be installed
         *  once.
         */
        if (selection.equals("-1") && (! systemSetupComplete) && (systemAdministrator == null)) {
          
          if (! typeRoleInstalled) {
            // Connect to the database
            DatabaseDriverExtender.initialize(connection);
          
            // Define the User Roles.
            DatabaseInsertHelper.insertRole("ADMIN");
            DatabaseInsertHelper.insertRole("TELLER");
            DatabaseInsertHelper.insertRole("CUSTOMER");   
          
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
            typeRoleInstalled = true;
          }
          
          
          // Inform the user that the system is ready to create an admin account
          System.out.println("Now creating the Bank Administrator account...");
          System.out.println(" ");
          
          // Request the admin's name
          System.out.println("Please enter your name in the format [FIRST] [LAST]:");     
          String adminName = getInputAllChars(); 
          
          // Request the admin's age
          System.out.println("Please enter your age:");     
          int adminAge = getInputNumeric();
          
          // Request the admin's address
          System.out.println("Please enter your address in 100 or fewer characters:");     
          String adminAddress = getInputAllChars();
          
          // Request the admin's password
          System.out.println("Please set your user password:");     
          String adminPasscode = getInputAllChars();
          System.out.println("");
          
          // Get a mapping of the current roles in the database
          RolesMap rolesMap = new RolesMap();
         
          // Insert the user into the database and set as system admin
          int adminId  = DatabaseInsertHelper.insertNewUser(
              adminName, adminAge, adminAddress, rolesMap.getRoleId("admin"), adminPasscode); 
          
          // Get the admin
          if (!(adminId == -1)) {
            systemAdministrator = (Admin) DatabaseSelectHelper.getUserDetails(adminId);
          }
          
          // If admin was set:
          if (!(systemAdministrator == null)) {
            // Inform the user that setup is complete.
            System.out.println("System Setup is now complete. Thank you, " + adminName + ".");
            System.out.println("Your ID number is " + adminId + ".");
            // Set the completion token to true
            systemSetupComplete = true;

          // If not set:
          } else {
            System.out.println("System Setup failed. Please check your inputs and try again.");
          }
          
        //________________________________ [1]M: ADMIN TERMINAl ________________________________
          /*
           *  (1) -> Administrative Mode
           *  Allows administrators to login and create new tellers or administrators
           *  Allows administrators to list all tellers, admins and customers
           */
        } else if (selection.equals("1") && (systemSetupComplete)) {
          // Request an administrator to log in
          System.out.println("Please enter an administrative ID to access administrative mode: ");
          int adminId = getInputNumeric();
          System.out.println("Please enter your password: ");
          String adminPassword = getInputAllChars();
          
          boolean adminAuthenticated = false;  // Administrator Authentication Status
          boolean exitAdminTerminal = false;   // Stop token
          
          User potentialAdmin = DatabaseSelectHelper.getUserDetails(adminId);
          RolesMap roleMap = new RolesMap();
          // Check if the admin ID is valid:
          if (!(potentialAdmin == null) 
              && (roleMap.getRoleName(potentialAdmin.getRoleId()).equalsIgnoreCase("admin"))) {
            // Set the admin as current and attempt to authenticate
            systemAdministrator = (Admin) potentialAdmin;   
            adminAuthenticated = systemAdministrator.authenticate(adminPassword);
          }
          
          // If the administrator is set:
          if (adminAuthenticated) {
            AdminTerminal currentAdminUser = new AdminTerminal(adminId, adminPassword);
            do {
              // Welcome the user
              System.out.println("  - - - - - - - - - - - - - - - - -");
              System.out.println("  Welcome, " + systemAdministrator.getName());
              System.out.println("  - - - - - - - - - - - - - - - - -");
              
              // Provide the admin with a menu to select from
              System.out.println("");
              System.out.println(" .________________________________.");
              System.out.println(" |       Administrator Tasks      |");
              System.out.println(" |--------------------------------|");
              System.out.println(" | [1]      Add an Administrator  |");
              System.out.println(" | [2]      Add a new teller      |");
              System.out.println(" |- - - - - - - - - - - - - - - - |");
              System.out.println(" | [3]      List all Admins       |");
              System.out.println(" | [4]      List all Tellers      |");
              System.out.println(" | [5]      List all customers    |");
              System.out.println(" |- - - - - - - - - - - - - - - - |");
              System.out.println(" | [6]      View all accounts     |");
              System.out.println(" |- - - - - - - - - - - - - - - - |");
              System.out.println(" | [7]      View sum of all bank  |");
              System.out.println(" |          account balances      |");
              System.out.println(" |- - - - - - - - - - - - - - - - |");
              System.out.println(" | [8]      Promote Teller to     |");
              System.out.println(" |          System Administrator  |");
              System.out.println(" |- - - - - - - - - - - - - - - - |");
              System.out.println(" | [9]      Export Database file  |");
              System.out.println(" | [10]     Import Database file  |");
              System.out.println(" |- - - - - - - - - - - - - - - - |");
              System.out.println(" | [11]     Message Lookup by ID  |");
              System.out.println(" | [12]     View My Messages      |");
              System.out.println(" | [13]     Leave Message         |");
              System.out.println(" |- - - - - - - - - - - - - - - - |");
              System.out.println(" | [OTHER]  Exit to startup menu  |");
              System.out.println(" |________________________________|");  
              
              // Get the Administrator's decision
              String adminSelection = getInputAllChars();
              
              // [1] Make new Administrator
              if (adminSelection.equals("1")) {
                
                // Request the administrator's name
                System.out.println(
                    "Please enter the administrator's name in the format [FIRST] [LAST]:");     
                String newAdminName = getInputAllChars(); 
                
                // Request the administrator's age
                System.out.println("Please enter the administrator's age:");     
                int newAdminAge = getInputNumeric();
                
                // Request the administrator's address
                System.out.println(
                    "Please enter the administrator's address in 100 or fewer characters:");     
                String newAdminAddress = getInputAllChars(); 
                
                // Request the administrator's password
                System.out.println("Please set administrator's chosen password:");     
                String newAdminPasscode = getInputAllChars(); 
                
                // Add the administrator into the database and get an ID
                int newAdminId  =
                    currentAdminUser.makeNewUser(
                        "admin", newAdminName, newAdminAge, newAdminAddress, newAdminPasscode);
                
                // Record the administator if added
                if (!(newAdminId == -1)) {
                  System.out.println(
                      "Thank you. " + newAdminName + " has been added as an Administrator.");
                  System.out.println(newAdminName + "'s ID number is: " + newAdminId);
                } else {
                  // Report failure
                  System.out.println("The indicated administator could not be added..."); 
                  System.out.println("Please check your input and try again."); 
                }
                
                // [2] Make new teller
              } else if (adminSelection.equals("2")) {
                
                // Request the teller's name
                System.out.println(
                    "Please enter the teller's name in the format [FIRST] [LAST]:");     
                String tellerName = getInputAllChars(); 
                
                // Request the Teller's age
                System.out.println("Please enter the teller's age:");     
                int tellerAge = getInputNumeric();
                
                // Request the Teller's address
                System.out.println(
                    "Please enter the teller's address in 100 or fewer characters:");     
                String tellerAddress = getInputAllChars(); 
                
                // Request the Teller's password
                System.out.println("Please set teller's chosen password:");     
                String tellerPasscode = getInputAllChars(); 
                
                // Open up an AdminTerminal
                AdminTerminal connection1 = new AdminTerminal(adminId, adminPassword);
                // Add the teller into the database and get a teller ID
                int tellerId  =
                    connection1.makeNewUser(tellerName, tellerAge, tellerAddress, tellerPasscode);
                
                // Record the teller's ID number if added
                if (!(tellerId == -1)) {
                  // Report success
                  System.out.println("Thank you. " + tellerName + " has been added as a teller.");
                  System.out.println(tellerName + "'s ID number is: " + tellerId);
                } else {
                  // Report failure
                  System.out.println("The indicated teller could not be added..."); 
                  System.out.println("Please check your input and try again."); 
                }
              
                // [3] List all admins
              } else if (adminSelection.equals("3")) {
                // Get all the admins in a list
                List<Admin> administratorList = currentAdminUser.listAllAdmins();
                
                // If there are admins:
                if ((!(administratorList == null)) && (!(administratorList.isEmpty()))) {
                  System.out.println("- - - - - - - - - - - - - - ");
                  System.out.println("All System Administrators: "); 
                  System.out.println(" ID  |  NAME ");  
                  // Iterate through the list printing ID numbers and names
                  for (Admin currentAdmin : administratorList) {
                    String adminName = currentAdmin.getName();
                    System.out.println("  " + currentAdmin.getId() + "    " + adminName);   
                  } 
                  System.out.println("- - - - - - - - - - - - - - ");
                  // If no admins -> report
                } else {
                  System.out.println("No System Administrators have been set."); 
                }
                            
                // [4] List all tellers
              } else if (adminSelection.equals("4")) {
                // Get all the tellers in a list
                List<Teller> tellerList = currentAdminUser.listAllTellers();
                
                // If there are tellers:
                if ((!(tellerList == null)) && (!(tellerList.isEmpty()))) {
                  System.out.println("- - - - - - - - - - - - - - ");
                  System.out.println("All Tellers: "); 
                  System.out.println(" ID  |  NAME "); 
                  // Iterate through the list printing ID numbers and names
                  for (Teller currentTeller : tellerList) {
                    String tellerName = currentTeller.getName();
                    System.out.println("  " + currentTeller.getId() + "    " + tellerName);   
                  } 
                  System.out.println("- - - - - - - - - - - - - - ");
                  // If no tellers -> report
                } else {
                  System.out.println("No tellers have been set."); 
                }
              
                // [5] List all customers
              } else if (adminSelection.equals("5")) {
                // Get all the customers in a list
                List<Customer> customerList = currentAdminUser.listAllCustomers();
                
                // If there are customers:
                if ((!(customerList == null)) && (!(customerList.isEmpty()))) {
                  System.out.println("- - - - - - - - - - - - - - ");
                  System.out.println("All Customers: "); 
                  System.out.println(" ID  |  NAME "); 
                  // Iterate through the list printing ID numbers and names
                  for (Customer currentCustomer : customerList) {
                    String custName = currentCustomer.getName();
                    System.out.println("  " + currentCustomer.getId() + "    " + custName);   
                  } 
                  System.out.println("- - - - - - - - - - - - - - ");
                  // If no customers -> report
                } else {
                  System.out.println("No Customers have been added."); 
                }
                              
                // If the admin wants to see the accounts of a customer
              } else if (adminSelection.equals("6")) {
                // Prompt the admin for the user ID
                System.out.println("Please enter the ID of the user:");
                Integer userSelection = getInputNumeric();
                
                // If the given selection is not invalid...
                if (!(userSelection == -1)) {
                  // Attempt to get the given user from the database
                  User user = DatabaseSelectHelper.getUserDetails(userSelection);
                  // If the ID is valid...
                  if (!(user == null)) {
                    // instantiate a roles map so that we can fetch the user's data to be displayed
                    RolesMap roles = new RolesMap();
                    
                    System.out.println("- - - - - - - - - - - - - - - - - - - - -");
                    System.out.println("User Name: " + user.getName());
                    System.out.println("User Role: " + roles.getRoleName(user.getRoleId()));
                    System.out.println("- - - - - - - - - - - - - - - ");
                    System.out.println("Accounts: ");
                    
                    // Get the user's accounts from the database
                    List<Integer> userAccounts = DatabaseSelectHelper.getAccountIds(userSelection);
                    // If the user has an account(s)...
                    if (userAccounts.size() > 0) {
                      System.out.println(" ");
                      System.out.println(" NAME  | BALANCE "); 
                      System.out.println(" ");
                      // Iterate through the user's accounts
                      for (Integer userAcc : userAccounts) {
                        // Get the details of each account in order to print out the name and their
                        // balance. 
                        Account acc = DatabaseSelectHelper.getAccountDetails(userAcc);
                        System.out.println(acc.getName() + " ..... $" + acc.getBalance());
                      }
                      // Print out the sum of all the account balances.
                      System.out.println(" ");
                      System.out.println("Total Balance: ..... $"
                          + currentAdminUser.userTotalBalance(userSelection));
                      DatabaseInsertHelper.insertMessage(user.getId(), "System Message: "
                          + "\n A system administrator has "
                          + "reviewed the balance of one or more of your accounts.");
                      // If the user has no accounts...
                    } else {
                      System.out.println("The given user has no accounts.");
                    }
                    System.out.println("- - - - - - - - - - - - - - - ");
                    
                    // If the given ID is invalid...
                  } else {
                    System.out.println("Invalid user ID. Please try again");
                    System.out.println("- - - - - - - - - - - - - - - ");
                  }
                  
                  // If the given input is simply invalid...
                } else {
                  System.out.println("Invalid input. Please try again");
                  System.out.println("- - - - - - - - - - - - - - - - - - - - -");
                }
                
                // [7] If the admin wants to view the balances of all the accounts in the bank
              } else if (adminSelection.equals("7")) {
                System.out.println("- - - - - - - - - - - - - - - - - - - - -");
                System.out.println("Sum of balances of all active bank accounts: ...... "
                    + "$" + currentAdminUser.viewTotalBalance());
                
                // [8] If the admin wants to promote a certain teller to administrator
              } else if (adminSelection.equals("8")) {
                // Prompt the admin for the teller ID
                System.out.println("Please enter the ID of the teller"
                    + " that you would like to promote:");
                Integer userSelection = getInputNumeric();
                
                // If the admin gives a valid input...
                if (!(userSelection == -1)) {
                  // An attempt will be made to promote the teller to admin status
                  boolean success = currentAdminUser.promoteTellerToAdmin(userSelection);
                  // If it was a success, the admin will be told
                  if (success) {
                    User chosenTeller = DatabaseSelectHelper.getUserDetails(userSelection);
                    System.out.println("Success. " + chosenTeller.getName() 
                        + " is now a system administrator.");
                    // Otherwise, the admin will be told that they've given an invalid teller ID
                  } else {
                    System.out.println("Invalid teller ID. Please try again");
                  }
                  // If the admin gives invalid input...
                } else {
                  System.out.println("Invalid Selection. Please try again");
                }
                
                // [9] If the admin wants to serialize the database
              } else if (adminSelection.equals("9")) {
                currentAdminUser.serializeDatabase();
                System.out.println("Database has been written to the file 'database_copy.ser'.");
                
                // [10] If the admin wants to deserialize a given copy of another database
              } else if (adminSelection.equals("10")) {
                try {
                  connection.close();
                } catch (SQLException e) {
                  System.out.println("Unable to deserialize the file 'database_copy.ser'.");
                }
                currentAdminUser.deserializeDatabase();
                connection = DatabaseDriverExtender.connectOrCreateDataBase();
                System.out.println("Database has deserialized the file 'database_copy.ser'.");

                
                // [11] If the admin wants to view someone's message
              } else if (adminSelection.equals("11")) {
                System.out.println("Please enter the ID of the message that you wish to view: ");
                Integer msgId = getInputNumeric();
                if (!(msgId == null)) {
                  String msg = currentAdminUser.viewMessage(msgId);
                  if (msg == "") {
                    System.out.println("Message " + msgId + " does not exist.");
                  } else {
                    System.out.println(msg);
                  }
                } else {
                  System.out.println("Invalid Message ID");
                }
                
                
                // [12] If the admin wants to view all of his/her own messages
              } else if (adminSelection.equals("12")) {
                // Get all of the current admin's messages and print them out (if any)
                List<Message> messages = currentAdminUser.viewOwnMessages();
             
                if (messages.size() == 0) {
                  System.out.println("You have no messages to view.");
                } else {
                  for (Message msg : messages) {
                    System.out.println(msg.getUserMessage());
                    int msgStat = msg.getViewedStatus();
                    if (msgStat == 1) {
                      System.out.println("Status: Read");
                    } else {
                      System.out.println("Status: Unread");
                    }
                    DatabaseUpdateHelper.updateUserMessageState(msg.getMessageId());
                    System.out.println("- - - - - - - - - - - - - - -");
                  }
                }

                
                // [13] If the admin wants to leave someone a message
              } else if (adminSelection.equals("13")) {
                // Prompt the admin for destination of the message.
                System.out.println("Please enter the ID of the person you wish to leave a message"
                    + " for: ");
                Integer userId = getInputNumeric();
                if (!(userId == null)) {
                  User targetUser = DatabaseSelectHelper.getUserDetails(userId);
                  if (!(targetUser == null)) {
                    System.out.println("Please type your message below: ");
                    String msg = getInputAllChars();
                    if (!(msg == null)) {
                      int messageId = currentAdminUser.leaveMessage(msg, userId);
                      System.out.println("MESSAGE ID: " + messageId);
                      if (messageId == -1) {
                        System.out.println("The above message failed to send. Please try again.");
                      } else {
                        System.out.println("Message has been successfully sent.");
                      }
                    } else {
                      System.out.println("Invalid message. Please check your input and try again.");
                    }
                  } else {
                    System.out.println("Invalid user ID. PLease check your input and try again.");
                  }
                }
                
              } else {
                // Exit the input loop
                exitAdminTerminal = true;
              }
            } while (!(exitAdminTerminal));
            
          }
          // If system setup is not complete, ask the user to go back and complete it
        } else if (selection.equals("1") && (!(systemSetupComplete))) {
          System.out.println(
              "Please complete the initial system setup before entering Administrative Mode.");
          
          
          //____________________________ [OTHER INPUT]M: INTFC MENU ____________________________
          /*
           * If the user inputs anything other than -1 or 1 --> Context Menu
           */
        } else if (systemSetupComplete && selection.equals("-1")) {
          System.out.println(
                "Initial Setup can only be completed once. Please select another option.");
        } else if (systemSetupComplete) {
          boolean leaveInterfaceSelect = false;
          do {
            // Provide the user with a menu to select from
            System.out.println(" .__________________________________________________.");
            System.out.println(" |                     MAIN MENU                    |");
            System.out.println(" |--------------------------------------------------|");
            System.out.println(" | Please select one of the following options:      |");
            System.out.println(" |- - - - - - - - - - - - - - - - - - - - - - - - - |");
            System.out.println(" | [1]    Run as Teller Interface                   |");
            System.out.println(" | [2]    Run as ATM Interface                      |");
            System.out.println(" | [0]    Exit*                                     |");
            System.out.println(" |                                                  |");
            System.out.println(" | *Exit requires Administrator Authentication.     |");
            System.out.println(" |__________________________________________________|");
          
            // Get the user selection
            String userSelection = getInputAllChars();

            //________________________________ O1: TELLER INTERFACE ________________________________
            // If the user types 1 -> Teller Interface
            if (userSelection.equals("1")) {
              
              //______________ Records ________________
              Teller currentTeller = null;             // Current teller operating terminal
              TellerTerminal tellerTerminal = null;    // Teller Terminal connection
              boolean tellerAuthenticated = false;     // Teller Authentication status
              
              Customer currentCustomer = null;         // Current customer
              boolean customerAuthenticated = false;   // Customer Authentication status
              String currentCustomerPassword = null;   // Current Customer's password              
              //_______________________________________
              
              // Prompt for user ID
              System.out.println("Please enter your ID number:");
              int tellerId = getInputNumeric();
              
              // Prompt for user Password
              System.out.println("Please enter your password:");
              String tellerPassword = getInputAllChars();
                
              RolesMap rolesMap = new RolesMap();
              
              // Verify that the user is indeed defined as a teller in the database
              Boolean isTeller = (DatabaseSelectHelper.getUserRole(tellerId)
                  == rolesMap.getRoleId("teller"));
              // Teller object will never be null in this [if] block
              if (isTeller) {
                // Open the Teller Terminal and authenticate the teller
                tellerTerminal = new TellerTerminal(tellerId, tellerPassword);
                  
                // Obtain the teller object and set to current user
                currentTeller = (Teller) DatabaseSelectHelper.getUserDetails(tellerId);
                // Redundant authentication to check if password matches and set attribute
                tellerAuthenticated = currentTeller.authenticate(tellerPassword);  
              }
              
              // Token to exit teller terminal
              boolean leaveTellerTerminal = false;
              // Token to give the teller select menu
              boolean giveMenu = false;
              // Give the menu iff the teller is set and authenticated
              if ((!(currentTeller == null)) && tellerAuthenticated) {
                giveMenu = true;
              }
              
              // If the permission is set
              if (giveMenu) {
                
                // WHILE EXIT IS FALSE
                do {
                  // Welcome the user upon initial entry and refresh
                  System.out.println("- - - - - - - - - - - - - - - - - - - - -");
                  System.out.println("  Welcome, " + currentTeller.getName());
                  System.out.println("- - - - - - - - - - - - - - - - - - - - -");
                  
                  if (!(currentCustomer == null)) {
                    // Customer info
                    System.out.println("  Your Customer: " + currentCustomer.getName());
                    System.out.println("  Age: " + currentCustomer.getAge());
                    System.out.println("  Address: " + currentCustomer.getAddress());
                    System.out.println("- - - - - - - - - - - - - - - - - - - - -");
                    System.out.println("  Active Customer Bank Accounts:");
                    // Get the list of all accounts from the ATM
                    List<Account> allAccounts = tellerTerminal.listAccounts();
                    
                    // If the list is empty
                    if (allAccounts == null || allAccounts.isEmpty()) {
                      System.out.println("The customer not have any active accounts.");
                      System.out.println("Select [3] to open a bank account ");
                      System.out.println("- - - - - - - - - - - - - - - - - - - - -");
                      
                      // If the list is non-empty
                    } else {
                      // Iterate through each account
                      for (Account currentAccount: allAccounts) {
                        // Obtain the account name
                        String accountName = currentAccount.getName();
                        // Obtain the account balance
                        BigDecimal accountBalance = (currentAccount.getBalance());
                        // Ensure the balance is rounded to 2 decimal places
                        String finalBalance = accountBalance.setScale(
                            2, RoundingMode.CEILING).toString();
                        // Get the account Type Name
                        String accountTypeName = 
                            DatabaseSelectHelper.getAccountTypeName(currentAccount.getType());
                        
                        // Final return statement
                        System.out.println("ID: " + currentAccount.getId()
                            + ".  Type: " + accountTypeName + ": ");
                        System.out.println("   " + accountName + " .......  $ " + finalBalance);
                      }
                      System.out.println("- - - - - - - - - - - - - - - - - - - - -");
                    }
                    
                  } else {
                    System.out.println("  No active customer session.");
                    System.out.println("  You can Authenticate an existing user,");
                    System.out.println("  Or open a new user account.");
                    System.out.println("- - - - - - - - - - - - - - - - - - - - -");
                  }
                  // Provide the user with a menu to select from
                  System.out.println("");
                  System.out.println(" .________________________________.");
                  System.out.println(" |       Teller Select Menu       |");
                  System.out.println(" |--------------------------------|");
                  System.out.println(" | [1]    Authenticate a client   |");
                  System.out.println(" |--------------------------------|");
                  System.out.println(" | [2]    Create a new User       |");
                  System.out.println(" | [3]    Create a new Account    |");
                  System.out.println(" |--------------------------------|");
                  System.out.println(" | [4]    Give interest           |");
                  System.out.println(" | [5]    Make a deposit          |");
                  System.out.println(" | [6]    Make a withdrawal       |");
                  System.out.println(" | [7]    Check balance           |");
                  System.out.println(" |--------------------------------|");
                  System.out.println(" | [8]    Close customer session  |");
                  System.out.println(" |--------------------------------|");
                  System.out.println(" | [9]    View all accounts       |");
                  System.out.println(" | [10]   Update Customer Profile |");
                  System.out.println(" |--------------------------------|");
                  System.out.println(" | [11]   View my messages        |");
                  System.out.println(" | [12]   View customer messages  |");
                  System.out.println(" | [13]   Leave message           |");
                  System.out.println(" |--------------------------------|");
                  System.out.println(" | [0]    Exit                    |");
                  System.out.println(" |________________________________|");   
                  
                  // Instantiate a buffered reader to get the user selection
                  InputStreamReader tiSelect = new InputStreamReader(System.in);
                  BufferedReader tellerTerminalReader = new BufferedReader(tiSelect);
                  String tellerSelection = tellerTerminalReader.readLine();
                
                  // [INPUT 1] and Teller Authenticated -> Attempt to authenticate Customer
                  if (tellerSelection.equals("1") && tellerAuthenticated) {
                    // Prompt for user ID
                    System.out.println("Please enter the Customer's ID number:");
                    int customerId = getInputNumeric();
                
                    // Prompt for user Password
                    System.out.println("Please enter the Customer's password:");
                    String customerPassword = getInputAllChars();
                  
                    // Verify that the user is indeed defined as a Customer in the database
                    Boolean isCustomer = (DatabaseSelectHelper.getUserRole(customerId)
                        == rolesMap.getRoleId("customer"));
                    
                    // Customer object will never be null in this [if] block
                    if (isCustomer) {
                      // Get the Customer object
                      currentCustomer = (Customer) DatabaseSelectHelper.getUserDetails(customerId);
                    
                      // Set the customer as the current in the teller terminal
                      tellerTerminal.setCurrentCustomer(currentCustomer);
                      // Authenticate the customer through the teller terminal
                      tellerTerminal.authenticateCurrentCustomer(customerPassword);
                    
                      // Redundant authentication to check if password matches and set attribute
                      customerAuthenticated = currentCustomer.authenticate(customerPassword);
                      // Record the password
                      currentCustomerPassword = customerPassword;
                      
                      // Report whether the customer has been authenticated.
                      if (customerAuthenticated) {
                        System.out.println(currentCustomer.getName() + " is now authenticated.");
                      } else {
                        System.out.println("Customer authentication failed.");
                        currentCustomer = null;
                      }
                    }
             
                  
                    // [INPUT 2] and Teller Authenticated -> Create a new user
                  } else if (tellerSelection.equals("2") && tellerAuthenticated) {
                    // Request the customer's name
                    System.out.println(
                        "Please enter the customer's name in the format [FIRST] [LAST]:");     
                    String customerName = getInputAllChars();
                  
                    // Request the customer's age
                    System.out.println("Please enter the customer's age:");     
                    int customerAge = getInputNumeric();
                    
                    // Request the customer's address
                    System.out.println(
                        "Please enter the customer's address in 100 or fewer characters:");     
                    String customerAddress = getInputAllChars();
                  
                    // Request the customer's password
                    System.out.println("Please enter the customer's selected password:");
                    String customerPasscode = getInputAllChars();
                 
                    // Insert the customer into the database
                    int customerId  = DatabaseInsertHelper.insertNewUser(
                        customerName, customerAge, customerAddress, rolesMap.getRoleId("customer"),
                        customerPasscode);
                  
                    // Get the customer from the database
                    Customer newCustomer = null;
                    if (!(customerId == -1)) {
                      newCustomer = (
                        Customer) DatabaseSelectHelper.getUserDetails(customerId);
                    }
                    // Insert the customer to the Teller Terminal
                    tellerTerminal.setCurrentCustomer(newCustomer);
                    // Set the new customer as the current client
                    currentCustomer = newCustomer;
                    
                    if (!(currentCustomer == null)) {
                      // Authenticate the current customer by default after opening account
                      tellerTerminal.authenticateCurrentCustomer(customerPasscode);
                      // Set the local authentication token through redundant authentication
                      customerAuthenticated = currentCustomer.authenticate(customerPasscode);
                      // Record the password
                      currentCustomerPassword = customerPasscode;
                    } else {
                      customerAuthenticated = false;
                    }
                    
                    // If added, current will be set to the new customer account and not null
                    boolean customerAdded = (
                        (!(currentCustomer == null)) && (currentCustomer.equals(newCustomer)));
                  
                    // Report success or failure from the operation.
                    if (customerAdded) {
                      System.out.println("Success.");
                      // Report the new user ID
                      System.out.println(currentCustomer.getName()
                          + "'s new customer account has been created with ID " 
                          + currentCustomer.getId());
                      // Report set as current client
                      System.out.println(currentCustomer.getName()
                          + " has been set as the current client.");
                    } else {
                      // Report failure
                      System.out.println("A customer account with the given specifications "
                          + "could not be added. Please check your input and try again.");
                    }
                 
                  
                    // [INPUT 3] and Both Authenticated -> Create a new account
                  } else if (tellerSelection.equals("3")
                      && tellerAuthenticated && customerAuthenticated) {
                    // Provide the user with a menu to select from
                    System.out.println(" .__________________________.");
                    System.out.println(" | Select Bank Account Type |");
                    System.out.println(" |--------------------------|");
                    System.out.println(" | [1]  Chequing            |");
                    System.out.println(" | [2]  Savings             |");
                    System.out.println(" | [3]  Tax Free Savings    |");
                    System.out.println(" | [4]  Restricted Savings  |");
                    System.out.println(" | [5]  Balance Owing       |");
                    System.out.println(" |__________________________|");  
                    // Get the account Type ID -> Defaults to -1 
                    int accountTypeId = getInputNumeric();
                    
                    AccountTypesMap accTypesMap = new AccountTypesMap();
                    
                    // Given the user input, get the role ID of the appropriate account type
                    int selectedAccountType = -1;
                    if (accountTypeId == 1) {
                      selectedAccountType = accTypesMap.getAccTypeId("chequing");
                    } else if (accountTypeId == 2) {
                      selectedAccountType = accTypesMap.getAccTypeId("savings");
                    } else if (accountTypeId == 3) {
                      selectedAccountType = accTypesMap.getAccTypeId("tfsa");
                    } else if (accountTypeId == 4) {
                      selectedAccountType = accTypesMap.getAccTypeId("restricted savings");
                    } else if (accountTypeId == 5) {
                      selectedAccountType = accTypesMap.getAccTypeId("balance owing");
                    }
                    
                    // Request the account name
                    System.out.println("Please enter the customer's selected account name:");     
                    String accountName = getInputAllChars();
                    System.out.println("");
                    
                    // Request the account starting balance
                    System.out.println("Please enter the customer's first deposit sum:");     
                    BigDecimal balance = new BigDecimal(getInputNumericChars());
                    System.out.println("");
                  
                    // The type ID is valid if it is no longer -1
                    boolean validType = !(selectedAccountType == -1);
                    
                    // The balance is valid if it is greater than or equal to 0
                    boolean validStartBalance = (balance.compareTo(
                        BigDecimal.ZERO) == 1 || balance.compareTo(BigDecimal.ZERO) == 0);
                    
                    // The start balance for savings accounts must be at least $1000.
                    BigDecimal savingsMin = new BigDecimal("1000");
                    boolean validStartBalanceSavings = (balance.compareTo(
                        savingsMin) == 1 || balance.compareTo(savingsMin) == 0);
                    
                    boolean isSavings = ((selectedAccountType 
                        == accTypesMap.getAccTypeId("savings")));
                    boolean isBalanceOwing = ((selectedAccountType == accTypesMap.getAccTypeId(
                        "balance owing")) && (! validStartBalance));
                    
                    // Check that the typeId and start Balance are valid
                    if (validType && validStartBalance && (! isSavings)) {
                      // Create Account through the teller terminal and link to the current customer
                      tellerTerminal.makeNewAccount(accountName, balance, selectedAccountType);
                      // Report success
                      System.out.println(
                          "Success. " + accountName + " has been activated.");
                    
                      // If the account is a savings or restricted savings
                    } else if (validType && validStartBalanceSavings && isSavings) {
                      // Create Account through the teller terminal and link to the current customer
                      tellerTerminal.makeNewAccount(accountName, balance, selectedAccountType);
                      // Report success
                      System.out.println(
                          "Success. " + accountName + " has been activated.");
                    
                    } else if (validType && (! validStartBalanceSavings) && isSavings) {
                      // Report failure
                      System.out.println("An initial deposit of at least $1000.00"
                          + " is necessary to open a savings account.");
                    
                      // Run a check to see if the account type is a balance owing account
                    } else if (isBalanceOwing) {
                      // Create Account through the teller terminal and link to the current customer
                      tellerTerminal.makeNewAccount(accountName, balance, selectedAccountType);
                      // Report success
                      System.out.println(
                          "Success. " + accountName + " has been activated.");
                      
                    } else {
                      // Report failure
                      System.out.println("An account with the indicated specifications"
                          + " could not be created. Please check your input and try again.");  
                    }  
                    
                    // [INPUT 3] and not Both Authenticated -> Fail
                  } else if (tellerSelection.equals("3")
                        && (!tellerAuthenticated || !customerAuthenticated)) {
                    // Report failure
                    System.out.println("An account with the indicated specifications"
                        + " could not be created. Please check authentication try again.");  
                  
                    // [INPUT 4] and Both Authenticated -> Give Interest
                  } else if (tellerSelection.equals("4")
                      && tellerAuthenticated && customerAuthenticated) { 
                    // Request the account ID
                    System.out.println(" .___________________________.");
                    System.out.println(" |     Give Interest to:     |");   
                    System.out.println(" |---------------------------|");
                    System.out.println(" | [1]      All Accounts     |");
                    System.out.println(" | [OTHER]  Specific account |");
                    System.out.println(" |___________________________|");    
                    int giveInterestMethod = getInputNumeric();
                    System.out.println("");
                  
                    if (giveInterestMethod == 1) {
                      tellerTerminal.giveInterest();
                    } else {
                      // Request the account ID
                      System.out.println(
                          "Please enter the ID of the account to give interest to.");  
                      int accountId = getInputNumeric();
                      // Use the teller terminal and attempt to give interest
                      tellerTerminal.giveInterest(accountId);
                    }
                    
                    // Report that attempt has been made.
                    System.out.println("Transaction complete.");
                    
                    // [INPUT 4] and not Both Authenticated -> Fail
                  } else if (tellerSelection.equals("4")
                        && (!tellerAuthenticated || !customerAuthenticated)) {
                    // Report failure
                    System.out.println("Please check authentication try again.");
                    
                    
                    // [INPUT 5] and Both Authenticated -> Make a deposit
                  } else if (tellerSelection.equals("5")
                      && tellerAuthenticated && customerAuthenticated) {
                    // Request the account ID
                    System.out.println(
                        "Please enter the ID of the customer account to deposit into:");     
                    int accountId = getInputNumeric();
                    
                    // Request the deposit amount
                    System.out.println(
                        "Please enter the amount that the customer wishes to deposit:");
                    BigDecimal amount = new BigDecimal(getInputNumericChars());
                    
                    // Attempt to deposit through TellerTerminal and get token
                    boolean success = false;
                    try {
                      success = tellerTerminal.makeDeposit(amount, accountId);
                    } catch (IllegalAmountException e) {
                      // Report Error
                      System.out.println("Invalid deposit amount.");
                      System.out.println("The minimum deposit is $0.01.");
                    } catch (InsufficientPrivilegesException e) {
                      // Report error
                      System.out.println("Insufficient Privileges");
                    }
              
                    // Report whether the request was successful.
                    if (success) {
                      // Get the account object
                      Account userAccountObj = DatabaseSelectHelper.getAccountDetails(accountId);
                      // Get the account balance
                      BigDecimal accountBalance = userAccountObj.getBalance();
                      // Report the new balance
                      System.out.println("Deposit Successful.");
                      System.out.println("New Balance: $" + accountBalance);
                      
                      // Report whether the request was successful.
                    } else {
                      System.out.println("Deposit failed. Please try again.");
                    }
                    
                    // [INPUT 5] and not Both Authenticated -> Fail
                  } else if (tellerSelection.equals("5")
                        && (!tellerAuthenticated || !customerAuthenticated)) {
                    // Report failure
                    System.out.println("Please check authentication try again.");
                    
                    
                    // [INPUT 6] and Both Authenticated -> Make a withdrawal
                  } else if (tellerSelection.equals("6")
                      && tellerAuthenticated && customerAuthenticated) {
                    // Request the account ID
                    System.out.println(
                        "Please enter the ID of the customer account to withdraw from:");     
                    int accountId = getInputNumeric();
                    
                    // Request the withdrawal amount
                    System.out.println(
                        "Please enter the amount that the customer wishes to withdraw:");
                    BigDecimal amount = new BigDecimal(getInputNumericChars());
                    
                    // Attempt to withdraw through TellerTerminal and get token
                    boolean success = false;
                    try {
                      success = tellerTerminal.makeWithdrawal(amount, accountId);
                    } catch (InsufficientPrivilegesException ipe) {
                      System.out.println("Insufficient Privileges.");
                    } catch (InsufficientFundsException ise) {
                      System.out.println("Insufficient Funds to withdraw from.");
                    } catch (IllegalAmountException iae) {
                      System.out.println("Invalid withdrawal amount.");
                      System.out.println("The minimum withdrawal amount is $0.01.");
                    }
                    
                    // Report whether the request was successful.
                    if (success) {
                      // Get the account object
                      Account userAccountObj = DatabaseSelectHelper.getAccountDetails(accountId);
                      // Get the account balance
                      BigDecimal accountBalance = userAccountObj.getBalance();
                      // Report the new balance
                      System.out.println("Withdrawal Successful.");
                      System.out.println("Remaining Balance: $" + accountBalance);
                      
                      // Report whether the request was successful.
                    } else {
                      System.out.println("Withdrawal Failed.");
                    }
                    
                    // [INPUT 6] and not Both Authenticated -> Fail
                  } else if (tellerSelection.equals("6")
                          && (!tellerAuthenticated || !customerAuthenticated)) {
                    // Report failure
                    System.out.println("Please check authentication try again.");
                   
                    
                    // [INPUT 7] and Both Authenticated -> Check Balance
                  } else if (tellerSelection.equals("7")
                      && tellerAuthenticated && customerAuthenticated) {
                    
                    // Ensure the customer is set and authenticated in teller terminal
                    tellerTerminal.setCurrentCustomer(currentCustomer);
                    tellerTerminal.authenticateCurrentCustomer(currentCustomerPassword);
                    
                    // Request the account ID
                    System.out.println(
                        "Please enter the ID of the account that you would like to check:");     
                    int accountId = getInputNumeric();
                    
                    System.out.println("");
                    System.out.println("Retrieving balance for account " + accountId + " ...");
                    System.out.println("");
                    
                    // Get the balance through the Teller Terminal
                    BigDecimal accountBalance = null;
                    try {
                      accountBalance = tellerTerminal.checkBalance(accountId);
                    } catch (InsufficientPrivilegesException e) {
                      System.out.println("Insufficient Privileges...");
                      // Report failure
                      System.out.println("Please check account ID or authentication status.");
                    } catch (ConnectionFailedException e) {
                      // Report failure
                      System.out.println("Connection Failed...");
                      System.out.println("Please check account ID or authentication status.");
                    }
                    
                    // If balance retrieved
                    if (!(accountBalance == null)) {
                      // Get the balance as a string rounded to two decimal places
                      String balance = (accountBalance.setScale(
                          2, RoundingMode.CEILING)).toString();
                      // Print statement
                      System.out.println("There is $" + balance + " remaining in the account.");
                    
                    // If balance was null
                    } else {
                      // Report error
                      System.out.println("Unable to retrieve balance. Please try again.");
                    }
                    
                    // [INPUT 7] and not Both Authenticated -> Fail
                  } else if (tellerSelection.equals("7")
                          && (!tellerAuthenticated || !customerAuthenticated)) {
                    // Report failure
                    System.out.println("Please check authentication try again.");
                  
                    
                    // [INPUT 8] and Teller Authenticated -> End Customer session
                  } else if (tellerSelection.equals("8") && tellerAuthenticated) {
                    // If a customer is set:
                    if (!(currentCustomer == null)) {
                      // Deauthenticate and remove the current customer through the teller terminal
                      tellerTerminal.deAuthenticateCustomer();
                      // Deauthenticate the current customer in the terminal
                      customerAuthenticated = false;
                      // Remove the current customer from the terminal
                      currentCustomer = null;
                      // Output success token
                      System.out.println("The customer has been successfully signed out.");
                   
                      // If there is no customer set:
                    } else {
                      // Output that there is no session to close
                      System.out.println("There is currently no active customer session to close.");
                    }
                 
                    
                    // [INPUT 9] and Teller Authenticated -> View all of a given user's 
                    // accounts
                  } else if (tellerSelection.equals("9") && tellerAuthenticated) {
                    // Prompt the teller for the user ID
                    System.out.println("Please enter the ID of the user:");
                    Integer tellerChoice = getInputNumeric();
                    
                    // If the given selection is not invalid...
                    if (!(tellerChoice == -1)) {
                      // Attempt to get the given user from the database
                      User user = DatabaseSelectHelper.getUserDetails(tellerChoice);
                      // If the ID is valid...
                      if (!(user == null)) {
                        // instantiate a roles map so that we can fetch the user's data to be
                        // displayed
                        RolesMap roles = new RolesMap();
                        
                        System.out.println("- - - - - - - - - - - - - - - - - - - - -");
                        System.out.println("User Name: " + user.getName());
                        System.out.println("User Role: " + roles.getRoleName(user.getRoleId()));
                        System.out.println("- - - - - - - - - - - - - - - ");
                        System.out.println("Accounts: ");
                        
                        // Get the user's accounts from the database
                        List<Integer> userAccounts = DatabaseSelectHelper.getAccountIds(
                            tellerChoice);
                        // If the user has an account(s)...
                        if (userAccounts.size() > 0) {
                          System.out.println(" ");
                          System.out.println(" NAME  | BALANCE "); 
                          System.out.println(" ");
                          // Iterate through the user's accounts
                          for (Integer userAcc : userAccounts) {
                            // Get the details of each account in order to print out the name and 
                            // their balance. 
                            Account acc = DatabaseSelectHelper.getAccountDetails(userAcc);
                            System.out.println(acc.getName() + " ..... $" + acc.getBalance());
                          }
                          // Print out the sum of all the account balances.
                          System.out.println(" ");
                          System.out.println("Total Balance: ..... $" 
                              + tellerTerminal.userTotalBalance(tellerChoice));
                          
                          // If the user has no accounts...
                        } else {
                          System.out.println("The given user has no accounts.");
                        }
                        System.out.println("- - - - - - - - - - - - - - - ");
                        
                        // If the given ID is invalid...
                      } else {
                        System.out.println("Invalid user ID. Please try again");
                        System.out.println("- - - - - - - - - - - - - - - ");
                      }
                      
                      // If the given input is simply invalid...
                    } else {
                      System.out.println("Invalid input. Please try again");
                      System.out.println("- - - - - - - - - - - - - - - - - - - - -");
                    }
                    
                    // [INPUT 10] and teller authenticated -> Update user information;
                  } else if (tellerSelection.equals("10") && (tellerAuthenticated)) {
                    // Prompt the teller for the user ID
                    System.out.println("Please enter the ID of the user:");
                    Integer tellerChoice = getInputNumeric();
                    // Check if the choice was valid
                    if (!(tellerChoice == -1)) {
                      RolesMap roleMap = new RolesMap();
                      User potentialUser = DatabaseSelectHelper.getUserDetails(tellerChoice);
                      // Check if the user ID and is customer Role ID
                      if (!(potentialUser == null) && ((roleMap.getRoleName(
                          potentialUser.getRoleId()).equalsIgnoreCase("customer")))) {
                        System.out.println(" .___________________________.");
                        System.out.println(" | Choose a field to Update: |");   
                        System.out.println(" |---------------------------|");
                        System.out.println(" | [1]     Update Password   |");
                        System.out.println(" | [2]     Update Address    |");
                        System.out.println(" | [3]     Update Name       |");
                        System.out.println(" | [Other] Update All Fields |");
                        System.out.println(" |___________________________|");    
                        String updateSelection = getInputAllChars();
                        
                        // [1] -> Update the password of the user
                        if (updateSelection.equals("1")) {
                          System.out.println("Please enter the customer's new password: ");
                          String passInput = getInputAllChars();
                          
                          // If the password is valid, make an attempt to update it
                          if (!(passInput == null)) {
                            boolean succ = tellerTerminal.updateUserPassword(passInput,
                                tellerChoice);
                            if (succ) {
                              System.out.println("Password update successful.");
                              System.out.println(
                                  "Please reauthenticate the customer.");
                              System.out.println(". . . . . . . . . . . . . .");
                              tellerTerminal.deAuthenticateCustomer();
                              // Deauthenticate the current customer in the terminal
                              customerAuthenticated = false;
                              // Remove the current customer from the terminal
                              currentCustomer = null;
                            } else {
                              System.out.println("Password update unsuccessful.");
                              System.out.println("Please check your input before trying again.");
                            }
                          } else {
                            System.out.println("Invalid password.");
                            System.out.println("Please check your input before trying again.");
                          }
                          
                          // [2] -> Update the address of the user
                        } else if (updateSelection.equals("2")) {
                          System.out.println("Please enter the customer's updated address: ");
                          String addressInput = getInputAllChars();
                          
                          if (!(addressInput == null)) {
                            boolean succ = tellerTerminal.updateUserAddress(addressInput,
                                tellerChoice);
                            if (succ) {
                              System.out.println("Address update successful.");
                              currentCustomer.setAddress(addressInput);
                            } else {
                              System.out.println("Address update unsuccessful.");
                              System.out.println("Please check your input before trying again.");
                            }
                          } else {
                            System.out.println("Invalid address.");
                            System.out.println("Please check your input before trying again.");
                          }
                          
                          // [3] -> Update the name of the user
                        } else if (updateSelection.equals("3")) {
                          System.out.println("Please enter customer's updated name: ");
                          String nameInput = getInputAllChars();
                          
                          if (!(nameInput == null)) {
                            boolean succ = tellerTerminal.updateUserName(nameInput, tellerChoice);
                            if (succ) {
                              System.out.println("Name update successful.");
                              System.out.println(
                                  "Please reauthenticate the customer.");
                              System.out.println(". . . . . . . . . . . . . .");
                              tellerTerminal.deAuthenticateCustomer();
                              // Deauthenticate the current customer in the terminal
                              customerAuthenticated = false;
                              // Remove the current customer from the terminal
                              currentCustomer = null;
                            } else {
                              System.out.println("Name update unsuccessful.");
                              System.out.println("Please check your input before trying again.");
                            }
                          } else {
                            System.out.println("Invalid name.");
                            System.out.println("Please check your input before trying again.");
                          }
                          
                          // [Other input] -> Update all the fields
                        } else {
                          System.out.println("Please enter the customer's name: ");
                          String nameInput = getInputAllChars();
                          System.out.println("Please enter the updated address: ");
                          String addressInput = getInputAllChars();
                          System.out.println("Please enter a new user password: ");
                          String passInput = getInputAllChars();
                          
                          if (!(passInput == null) && !(addressInput == null) 
                              && !(nameInput == null)) {
                            boolean succ = tellerTerminal.updateAllFields(passInput, addressInput, 
                                nameInput, tellerChoice);
                            if (succ) {
                              System.out.println("Update successful.");
                              System.out.println(
                                  "Please reauthenticate the customer.");
                              System.out.println(". . . . . . . . . . . . . .");
                              tellerTerminal.deAuthenticateCustomer();
                              // Deauthenticate the current customer in the terminal
                              customerAuthenticated = false;
                              // Remove the current customer from the terminal
                              currentCustomer = null;
                            } else {
                              System.out.println("Update unsuccessful.");
                              System.out.println("Please check your input before trying again.");
                            }                         
                          } else {
                            System.out.println("Invalid input. Please try again.");
                          }
                          
                        }
                      } else {
                        System.out.println("Insufficient Permissions."
                            + " You can only update details on valid customer profiles");
                      }
                    } else {
                      System.out.println("Invalid selection.");
                    }
                    
                    // [INPUT 11] or teller authentication -> View own messages
                  } else if (tellerSelection.equals("11") &&  tellerAuthenticated) {
                    List<Message> tellerMessages = tellerTerminal.viewOwnMessages();
                    for (Message tellerMsg : tellerMessages) {
                      System.out.println(tellerMsg.getUserMessage());
                      int msgStat = tellerMsg.getViewedStatus();
                      if (msgStat == 1) {
                        System.out.println("Status: Read");
                      } else {
                        System.out.println("Status: Unread");
                      }
                      DatabaseUpdateHelper.updateUserMessageState(tellerMsg.getMessageId());
                      System.out.println("- - - - - - - - - - - - - - ");
                    }
                    System.out.println("If your inbox is empty, no messages will be displayed.");
                    
                    // [INPUT 12] or teller authentication -> View customer's messages
                  } else if (tellerSelection.equals("12") && tellerAuthenticated) {
                    List<Message> customerMessages = tellerTerminal.viewCustomerMessages();
                    if (! (customerMessages == null)) {
                      for (Message custMsg : customerMessages) {
                        System.out.println(custMsg.getUserMessage());
                        int msgStat = custMsg.getViewedStatus();
                        if (msgStat == 1) {
                          System.out.println("Status: Read");
                        } else {
                          System.out.println("Status: Unread");
                        }
                        DatabaseUpdateHelper.updateUserMessageState(custMsg.getMessageId());
                        System.out.println("- - - - - - - - - - - - - - ");
                      }
                    } else {
                      System.out.println("No messages to view.");
                    }
                    
                    // [INPUT 13] or teller authentication -> Leave message for customer
                  } else if (tellerSelection.equals("13") && tellerAuthenticated) {
                    // Prompt the admin for the target user and make an attempt to send that user a
                    // message
                    System.out.println(
                        "Please enter the ID of the person you wish to leave a message"
                        + " for: ");
                    Integer userId = getInputNumeric();
                    if (!(userId == null)) {
                      User targetUser = DatabaseSelectHelper.getUserDetails(userId);
                      if (!(targetUser == null)) {
                        System.out.println("Please type your message below: ");
                        String msg = getInputAllChars();
                        if (!(msg == null)) {
                          int messageId = tellerTerminal.leaveMessage(msg, userId);
                          System.out.println("Recipient: " + targetUser.getName());
                          System.out.println("MESSAGE: " + "\n" + msg);
                          System.out.println("MESSAGE ID: " + messageId);
                          if (messageId == -1) {
                            System.out.println(
                                "The above message failed to send. Please try again.");
                          } else {
                            System.out.println("Message has been successfully sent.");
                          }
                        } else {
                          System.out.println("Failed. Invalid message");
                        }
                      } else {
                        System.out.println("Failed. Invalid user ID");
                      }
                    }
                    
                  } else if (tellerSelection.equals("0") || (! tellerAuthenticated)) {
                    // Log the teller out
                    currentTeller = null;
                    tellerAuthenticated = false;
                    
                    // Log out any customers
                    currentCustomer = null;
                    customerAuthenticated = false;
                    
                    // Deauthenticate the customer in the teller terminal
                    tellerTerminal.deAuthenticateCustomer();
                    // Close the teller Terminal
                    tellerTerminal = null;
                    // Exit the Teller Terminal Interface
                    leaveTellerTerminal = true;
                  
                    
                    // [OTHER INPUT] or unauthorized -> Try again
                  } else {
                    // Report unable to process
                    System.out.println("Unable to process your request at this time. "
                        + "Please verify your input and authentication "
                        + "settings before trying again.");
                    System.out.println("");
                  }  
                } while ((!(leaveTellerTerminal)));
              // If the user is not authorized
              } else {
                // Report.
                System.out.println("You are not authorized to access the teller terminal.");
                // If failed login, give option to exit
                System.out.println("Please press [0] to EXIT or any key to TRY AGAIN.");
                String failedInput = getInputAllChars();
                if (failedInput.equals("0")) {
                  leaveTellerTerminal = true;
                }  
              }
              
              
              //________________________________ O2: ATM INTERFACE ________________________________
              // If the user types 2 -> ATM Interface
            } else if (userSelection.equals("2")) {
              // Set the leave token
              leaveAtmInterface = false;
              
              Customer currentCustomer = null;
              boolean customerAuthenticated = false;
              Atm atmConnection = null;
              
              // Continue to loop in sign-in stage until the customer is authenticated
              do {
                // Prompt for user ID
                System.out.println("Please enter your ID number:");
                int userId = getInputNumeric();
              
                // Prompt for user Password
                System.out.println("Please enter your password:");
                String userPassword = getInputAllChars();
               
                RolesMap rolesMap = new RolesMap();
                // Verify that the user is indeed defined as a customer in the database
                Boolean isCustomer = (DatabaseSelectHelper.getUserRole(userId) 
                    == rolesMap.getRoleId("customer"));
              
                // Customer object will never be null in this [if] block
                if (isCustomer) {
                  // Get the customer from the database
                  currentCustomer = (Customer) DatabaseSelectHelper.getUserDetails(userId);
                
                  // Check if password matches and set attribute
                  customerAuthenticated = currentCustomer.authenticate(userPassword);
                  
                  // If the password matches; and the customer is verified:
                  if (customerAuthenticated) {
                    // Create an ATM connection and authenticate the customer in the ATM
                    // Guaranteed authentication at this point
                    atmConnection = new Atm(userId, userPassword);
                  }
                  
                  // If the client is not a customer or the authentication failed:
                } else {
                  // Report failure
                  System.out.println("Authentication failed. Please try again.");
                }
              } while (!customerAuthenticated); // Exit Sign-In loop when customer is authenticated
              
              do {
                // Guaranteed that customer is both authenticated and verified at this point
                // Welcome the user upon initial entry
                System.out.println(" - - - - - - - - - - - - - - - - - - -");
                System.out.println("  Welcome, " + currentCustomer.getName());
                System.out.println("  Address: " + currentCustomer.getAddress());
                System.out.println(" - - - - - - - - - - - - - - - - - - -");
                System.out.println("  Your bank accounts: ");
                // Get the list of all accounts from the ATM
                List<Account> allAccounts = atmConnection.listAccounts();
                
                // If the list is empty
                if (allAccounts == null || allAccounts.isEmpty()) {
                  System.out.println("  You do not currently have any active accounts.");
                  
                  // If the list is non-empty
                } else {
                  // Iterate through each account
                  for (Account currentAccount: allAccounts) {
                    // Obtain the account name
                    String accountName = currentAccount.getName();
                    // Obtain the account balance
                    BigDecimal accountBalance = (currentAccount.getBalance());
                    // Ensure the balance is rounded to 2 decimal places
                    String finalBalance = accountBalance.setScale(
                        2, RoundingMode.CEILING).toString();
                    
                    // Get the account Type Name
                    String accountTypeName = 
                        DatabaseSelectHelper.getAccountTypeName(currentAccount.getType());
                    
                    // Final return statement
                    System.out.println("ID: " + currentAccount.getId()
                        + ".  Type: " + accountTypeName + ": ");
                    System.out.println("   " + accountName + " .......  $ " + finalBalance);
                  }
                  System.out.println(" - - - - - - - - - - - - - - - - - - -");
                }
                // Provide user with main menu
                System.out.println(" .________________________________.");
                System.out.println(" |        ATM Select Menu         |");
                System.out.println(" |--------------------------------|");
                System.out.println(" | [1] Make a Deposit             |");
                System.out.println(" | [2] Check an Account Balance   |");
                System.out.println(" | [3] Make a Withdrawal          |");
                System.out.println(" |--------------------------------|");
                System.out.println(" | [4] Go to My Inbox             |");
                System.out.println(" |--------------------------------|");
                System.out.println(" | [5] Exit                       |");
                System.out.println(" |________________________________|");
              
                // Get the user Selection
                String customerSelection = getInputAllChars();
                
                // [INPUT 1] -> Make deposit
                if (customerSelection.equals("1")) {
                  // Request the account ID
                  System.out.println("Please enter the ID of the account"
                      + " that you would like to deposit into:");     
                  int accountId = getInputNumeric();
                  System.out.println("");
                  
                  // Request the deposit amount
                  System.out.println(
                      "Please enter the sum to deposit:");
                  BigDecimal amount = new BigDecimal(getInputNumericChars());
                  
                  // Attempt to deposit through ATM and get token
                  boolean success = false;
                  try {
                    success = atmConnection.makeDeposit(amount, accountId);
                  } catch (IllegalAmountException e) {
                    // Report Error
                    System.out.println("Invalid deposit amount.");
                    System.out.println("The minimum deposit is $0.01.");
                  } catch (InsufficientPrivilegesException e) {
                    // Report error
                    System.out.println("Insufficient Privileges");
                  }
                  
                  // Report whether the request was successful.
                  if (success) {
                    // Get the account object
                    Account userAccountObj = DatabaseSelectHelper.getAccountDetails(accountId);
                    // Get the account balance
                    BigDecimal accountBalance = userAccountObj.getBalance();
                    // Report the new balance
                    System.out.println("Deposit Successful.");
                    System.out.println("New Balance: $" + accountBalance);
                    
                    // Report whether the request was successful.
                  } else {
                    System.out.println("Deposit failed. Please try again.");
                  }
                  
                  // [INPUT 2] -> Check balance
                } else if (customerSelection.equals("2")) {
                  // Request the account ID
                  System.out.println(
                      "Please enter the ID of the account you would like to check:");     
                  int accountId = getInputNumeric();
                  
                  // Get the balance through the ATM
                  BigDecimal accountBalance = null;
                  try {
                    accountBalance = atmConnection.checkBalance(accountId);
                  } catch (InsufficientPrivilegesException e) {
                    // Report failure
                    System.out.println("Please check account ID or authentication status.");
                  } catch (ConnectionFailedException e) {
                    // Report failure
                    System.out.println("Please check account ID or authentication status.");
                  }
                  
                  // If balance retrieved
                  if (!(accountBalance == null)) {
                    // Get the balance as a string with 2 decimal places
                    String balance = (accountBalance.setScale(2, RoundingMode.CEILING)).toString();
                    System.out.println("There is $" + balance + " remaining in your account.");
                  
                  // If balance was null
                  } else {
                    // Report error
                    System.out.println("Unable to retrieve balance. Please try again.");
                  }
                  
                  // [INPUT 3] -> Make withdrawal
                } else if (customerSelection.equals("3")) {
                  // Request the account ID
                  System.out.println(
                      "Please enter the ID of the account that you wish withdraw from:");     
                  int accountId = getInputNumeric();
                  
                  // Request the withdrawal amount
                  System.out.println(
                      "Please enter the amount that you would like to withdraw:");
                  BigDecimal amount = new BigDecimal(getInputNumericChars());
                  
                  // Attempt to withdraw through TellerTerminal and get token
                  boolean success = false;
                  try {
                    success = atmConnection.makeWithdrawal(amount, accountId);
                  } catch (InsufficientPrivilegesException ipe) {
                    System.out.println("Insufficient Privileges");
                  } catch (InsufficientFundsException ise) {
                    System.out.println("Insufficient Funds to withdraw from.");
                  } catch (IllegalAmountException iae) {
                    System.out.println("Invalid withdrawal amount.");
                    System.out.println("The minimum withdrawal amount is $0.01.");
                  }
                  
                  // Report whether the request was successful.
                  if (success) {
                    // Get the account object
                    Account userAccountObj = DatabaseSelectHelper.getAccountDetails(accountId);
                    // Get the account balance
                    BigDecimal accountBalance = userAccountObj.getBalance();
                    // Report the new balance
                    System.out.println("Withdrawal Successful.");
                    System.out.println("Remaining Balance: $" + accountBalance);
                    
                    // Report whether the request was successful.
                  } else {
                    System.out.println("Withdrawal Failed. Please try again.");
                  }
                  
                  // [INPUT 4] -> View messages
                } else if (customerSelection.equals("4")) {
                  List<Message> customerMessages = atmConnection.viewOwnMessages();
                  for (Message customerMsg : customerMessages) {
                    System.out.println(customerMsg.getUserMessage());
                    int msgStat = customerMsg.getViewedStatus();
                    if (msgStat == 1) {
                      System.out.println("Status: Read");
                    } else {
                      System.out.println("Status: Unread");
                    }
                    DatabaseUpdateHelper.updateUserMessageState(customerMsg.getMessageId());
                    System.out.println("- - - - - - - - - - - - - - ");
                  }
                  System.out.println("If your inbox is empty, no messages will be displayed.");
                  
                  // [INPUT 5] -> Exit
                } else if (customerSelection.equals("5")) {
                  // Reset all customer data from the ATM
                  customerAuthenticated = false;
                  currentCustomer = null;
                  atmConnection = null;
                  // Set the exit token to true
                  leaveAtmInterface = true;
                  
                  // [INVALID INPUT] -> Report and repeat
                } else {
                  // Report unable to process
                  System.out.println("Invalid Response. Please ensure that your "
                      + "input matches one of the predefined settings and try again.");
                  System.out.println("");
                }
              } while (! leaveAtmInterface);
            
              
            //____________________________ O0: EXIT INTERFACE SELECT ____________________________
            } else if (userSelection.equals("0")) {
              // Ask for an admin password before exiting.
              System.out.println("EXIT requires administrative authentication.");
              System.out.println("Please enter an administrator ID:");
              int administratorId = getInputNumeric();
              
              RolesMap roleMap = new RolesMap();
              User potentialAdmin = DatabaseSelectHelper.getUserDetails(administratorId);
              // Get the Administrator if the ID is valid
              if (!(potentialAdmin == null)
                  && (roleMap.getRoleName(potentialAdmin.getRoleId())).equalsIgnoreCase("admin")) {
                systemAdministrator = (Admin) potentialAdmin;
              }          
                
              // Instantiate a buffered reader to get the admin password
              System.out.println("Please enter your password:");
              String adminPassword = getInputAllChars();
              System.out.println("");
              
              // Set the default authentication to false
              boolean adminAuth = false;
              // Ensure the administrator is already set:
              if (!(systemAdministrator == null)) {
                // Attempt to authenticate the administrator
                adminAuth = systemAdministrator.authenticate(adminPassword);
              }
                
              // If the administrator is authenticated:
              if (adminAuth) {
                // leave the interface select menu.
                leaveInterfaceSelect = true;
              } else {
                System.out.println("You do not have the necessary permissions to exit.");
              }
              
              
            //_________________________ O[OTHER]: ASK FOR NEW RESPONSE _________________________
            } else {
              // Invite the user to try again.
              System.out.println("Invalid Response. Please ensure that your "
                  + "selection matches one of the preset modes and try again.");
              System.out.println("");
            }
            
            // End INTF Menu loop only if the user requests to exit Interface Select
          } while (!(leaveInterfaceSelect));
        } else {
          // Request Setup
          System.out.println(
              "Please complete the initial system setup before entering Interface Select.");
        }
        // End the Main Menu loop only if the user requests to exit the terminal
      } while (! exitTerminal);
     
      //_________________________ EXCEPTION MANAGEMENT _________________________
    } catch (IOException e) {
      System.out.println("[!!!] Encountered an IO Exception during operation.");
      e.printStackTrace();
    
    } catch (ConnectionFailedException c) {
      System.out.println("[!!!] Unable to establish connection with bank database.");
      c.printStackTrace();
    
    } finally {
      try {
        connection.close();
      } catch (Exception e) {
        System.out.println("All connections to the database have now been closed.");
        e.printStackTrace();
      }
    }
    
  }
}
