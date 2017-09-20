package com.bank.generics;

public enum Roles {
  ADMIN("admin"), TELLER("teller"), CUSTOMER("customer");
  
  // Store the name
  private String name;
  
  /**
   * Sets each ENUM type with a role name.
   * @param name is the role name
   */
  private Roles(String name) {
    // Set the name
    this.name = name;
  }
  
  
  /**
   * toString override defaulting to lower-case type.
   */
  @Override
  public String toString() {
    // Return the name
    return this.name;
  }
}
