package com.bank.generics;

public enum AccountTypes {
  CHEQUING("chequing"), SAVING("savings"), TFSA("tfsa"), RESTRICTEDSAVINGS("restricted savings"),
  BALANCEOWING("balance owing");
  
  //Store the name
  private String name;
  
  /**
   * Sets each ENUM type with a type name.
   * @param name is the account type name
   */
  private AccountTypes(String name) {
    // Set the type
    this.name = name;
  }
  
  
  /**
   * toString override defaulting to lower-case type.
   */
  @Override
  public String toString() {
    // Return the type
    return this.name;
  }
}
