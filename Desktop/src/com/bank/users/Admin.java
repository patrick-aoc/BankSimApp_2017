package com.bank.users;

import com.bank.databasehelper.DatabaseSelectHelper;

public class Admin extends User {
  
  /**
   *  Constructor method for Admin.
   * @param id is the ID number assigned by the database
   * @param name is the user's name
   * @param age is the user's age
   * @param address is the user's address
   */
  public Admin(int id, String name, int age, String address) {
    // Obtain the user ID from the database upon instantiation
    this.id = id;
    // Obtain the user's name from the database upon instantiation
    this.name = name;
    // Obtain the user's age from the database upon instantiation
    this.age = age;
    // Obtain the user's address from the database upon instantiation
    this.address = address;
    
    // Automatically obtain Admin role ID from the database
    this.roleId = DatabaseSelectHelper.getUserRole(id);
  }
  
  
  /**
   * Constructor method for Admin with option to set authentication status.
   * @param id is the ID number assigned by the database
   * @param name is the user's name
   * @param age is the user's age
   * @param address is the user's address
   * @param authenticated is the user's authentication status
   */
  public Admin(int id, String name, int age, String address, boolean authenticated) {
    // Obtain the user ID from the database upon instantiation
    this.id = id;
    // Obtain the user's name from the database upon instantiation
    this.name = name;
    // Obtain the user's age from the database upon instantiation
    this.age = age;
    // Obtain the user's address from the database upon instantiation
    this.address = address;
    this.authenticated = authenticated;
    
    // Automatically obtain Admin role ID from the database
    this.roleId = DatabaseSelectHelper.getUserRole(id);   
  }

}
