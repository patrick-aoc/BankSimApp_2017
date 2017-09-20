package com.bank.messages;

import com.bank.databasehelper.DatabaseSelectHelper;
import com.bank.users.User;

// A class to represent a user message
public class Message {
  
  // Messages will have two fields:
  // A boolean to denote whether the message has been read
  private boolean viewedStatus = false;
  // A string containing the given message
  private String userMessage = "";
  private int userId = -1;
  private int msgId = -1;
  
  /**
   * A constructor for the message class.
   * @param userId the ID of the user that will receive the message
   * @param message the message that will be given to the desired user
   */
  public Message(int userId, String message) {
    // Check if the user ID being passed is a valid ID
    if (Message.verifyUserId(userId)) {
      // Check if the given message is less than or equal to 512 characters
      if (message.length() <= 512) {
        this.userId = userId;
        this.userMessage = message;
      } 
    }
  }
  
  /**
   * A method that will set the viewed status of a message.
   * @param viewed true denotes that a message has been viewed, otherwise false
   */
  public void setViewedStatus(boolean viewed) {
    this.viewedStatus = viewed;
  }
  
  /**
   * A method that will get the viewed status of a message.
   * @return 1 if the message has been viewed, otherwise 0
   */
  public int getViewedStatus() {
    int status = 0;
    if (this.viewedStatus) {
      status = 1;
    }
    return status;
  }
  
  /**
   * A method that will get the message to be delivered to a user.
   * @return the message to be given to the user
   */
  public String getUserMessage() {
    return this.userMessage;
  }
  
  /**
   * A method that will get the ID of the user receiving the message.
   * @return the ID of the user that will get the message
   */
  public int getMessageTarget() {
    return this.userId;
  }
  
  /**
   * A method that will set the ID of the message.
   */
  public void setMessageId(int msgId) {
    this.msgId = msgId;
  }
  
  /**
   * A method that will get the ID of the message.
   * @return the ID of the message
   */
  public int getMessageId() {
    return this.msgId;
  }
  
  /**
   * Given a user's unique ID, check whether it is in the database.
   * @param userId is the unique User Id to be verified
   * @return true if it is in the database, false otherwise
   */
  private static boolean verifyUserId(int userId) {
    // Obtain the User object
    User validator = DatabaseSelectHelper.getUserDetails(userId);
    // Verify the given user ID
    boolean verified = !(validator == null);
    // Return the result
    return verified;
  }
}
