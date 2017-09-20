package com.bank.interaction;


public interface Terminal {
  
  /**
   * Creates a new user with the given attributes and adds them into the database.
   * 
   * @param name is the user's name.
   * @param age is the user's age.
   * @param address is the user's address.
   * @param password is the user's desired password.
   * @return the user's database generated ID number or -1 if not added
   */
  public int makeNewUser(String name, int age, String address, String password);
  
  
}
