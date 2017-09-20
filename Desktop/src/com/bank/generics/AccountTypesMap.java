package com.bank.generics;

import com.bank.databasehelper.DatabaseSelectHelper;

import java.util.EnumMap;
import java.util.List;
import java.util.Set;

/**
 * A class to represent our EnumMap for the account types. The key will be the account type
 * and the value will be the account type's corresponding ID in the database.
 */
public class AccountTypesMap {
  
  private EnumMap<AccountTypes, String> accTypesMap = new EnumMap<AccountTypes, String>(
      AccountTypes.class);
  
  
  /**
   * A constructor for our account types enum map.
   */
  public AccountTypesMap() {
    // An update will be made to make sure the account type map is always updated
    this.updateAccTypesMap();
  }
  
  
  /**
   * A method that will return an enum map containing the account types in the database.
   * @return an enum map where the keys are the enum account type constants and the values are 
   *     account type IDs from the database.
   */
  public EnumMap<AccountTypes, String> getExistingAccountTypes() {
    return this.accTypesMap;
  }
  
  
  /**
   * A method that will check if the enum map has the specified account type ID value or not.
   * @param accTypeId the accType ID value to be checked
   * @return true of the accType ID is in the enum map as a value, otherwise false.
   */
  public boolean hasAccTypeValue(int accTypeId) {
    Integer potentialAccTypeId = new Integer(accTypeId);
    return this.accTypesMap.containsValue(potentialAccTypeId.toString());
  }
  
  
  /**
   * A method that will return a set containing the keys of our account types enum map.
   * @return a set of account types that act as keys for our enum map
   */
  public Set<AccountTypes> getAccTypeKeys() {
    return this.accTypesMap.keySet();
  }
  
  
  /**
   * A method that will return the account type name of its given account type value.
   * @return the key (account type name) that belongs to the given account type value
   */
  public String getAccTypeName(int accTypeValue) {
    String accTypeName = "";
    // Check to see if the given account type ID exists in the enum map.
    if (this.hasAccTypeValue(accTypeValue)) {  
      for (AccountTypes accType : this.getAccTypeKeys()) {
        // Iterate over the set of account type keys, fetching its corresponding
        // value and converting to Integer.
        String accTypeVal = this.getExistingAccountTypes().get(accType);
        Integer accTypeValIntGiven = new Integer(accTypeValue);
        // If the key's value is equal to the given account type -> return string representation
        if (accTypeVal.equals(accTypeValIntGiven.toString())) {
          accTypeName = accType.toString();
        }
      }
    }
    return accTypeName;
  }
  
  /**
   * A method that will return the value of a given String representation of an account type as
   * an int.
   * @param accTypeKey the string representing the key in the account types enum map
   * @return the value of the given key IF it exists in the enum map. -1 will be returned if
   *     said key is not found in the enum map
   */
  public int getAccTypeId(String accTypeKey) {
    Set<AccountTypes> accTypeKeys = this.getAccTypeKeys();
    int accTypeVal = -1;
    // Next, we will iterate through each of the keys and check to see if any of them match the
    // key specified in the input. If they match, get the value of the key and convert to int
    for (AccountTypes accType : accTypeKeys) {
      String accTypeName = accType.toString();
      if (accTypeKey.equalsIgnoreCase(accTypeName)) {
        Integer accTypeValString = new Integer(accTypesMap.get(accType));
        accTypeVal = accTypeValString.intValue();
      }
    }
    return accTypeVal;
  }
  
  /**
   * A method that will update the enum map based on whatever is in the database at the given
   * time.
   */
  public void updateAccTypesMap() {
    // Refresh existing mapping
    accTypesMap.clear();
    // Iterate through all of the constants within our AccountTypes enum
   
    for (AccountTypes accType : AccountTypes.values()) {
      // Get String representation of each constant
      String currAccTypeKey = accType.toString();
      // As we are iterating through each of the constants in our AccountTypes enum, we will also
      // iterate through each of the account types in the database in order to get the appropriate 
      // key-value pairing
      
      List<Integer> accTypeIds = DatabaseSelectHelper.getAccountTypesIds();
      if (!(accTypeIds == null)) {
        for (int currAccTypeNum : accTypeIds) {
          // We will get the account that has the corresponding roleID in the database
          String currAccTypeValue = DatabaseSelectHelper.getAccountTypeName(currAccTypeNum);
          
          // If that account type is equal to our current constant (currAccTypeKey), we will map
          // the current account type constant to that account type value.
          if (currAccTypeKey.equalsIgnoreCase(currAccTypeValue)) {
            Number accTypeValue = new Integer(currAccTypeNum);
            this.accTypesMap.put(accType, accTypeValue.toString());
          }
        }
      }
    }
  }
}
