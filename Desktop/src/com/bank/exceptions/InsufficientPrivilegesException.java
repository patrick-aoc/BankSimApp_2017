package com.bank.exceptions;

public class InsufficientPrivilegesException extends Exception  {
  
  private static final long serialVersionUID = 1L;

  public InsufficientPrivilegesException() {
    super();
  }
  
  public InsufficientPrivilegesException(String input) {
    super(input);
  }
}