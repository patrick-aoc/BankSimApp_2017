package com.bank.generics;

import com.bank.databasehelper.DatabaseSelectHelper;
import com.bank.generics.Roles;

import java.util.EnumMap;
import java.util.List;
import java.util.Set;

/**
 * A class to represent our EnumMap for the role types. The key will be the role type
 * and the value will be the role type's corresponding ID in the database.
 */
public class RolesMap {
  
  private EnumMap<Roles, String> rolesMap = new EnumMap<Roles, String>(Roles.class);
  
  /**
   * A constructor for our roles enum map.
   */
  public RolesMap() {
    // An update will be made to make sure the role map is always updated
    this.updateRolesMap();
  }
  
  
  /**
   * A method that will return an enum map containing the roles in the database.
   * @return an enum map where the keys are the enum role constants and the values are role IDs from
   *     the database.
   */
  public EnumMap<Roles, String> getExistingRoles() {
    return this.rolesMap;
  }
  
  
  /**
   * A method that will return a set containing the keys of our roles enum map.
   * @return a set of roles that act as keys for our enum map
   */
  public Set<Roles> getRoleKeys() {
    return this.rolesMap.keySet();
  }
  
  
  /**
   * A method that will check if the enum map has the specified role ID value or not.
   * @param roleId the role ID value to be checked
   * @return true of the role ID is in the enum map as a value, otherwise false.
   */
  public boolean hasRoleValue(int roleId) {
    Integer potentialRoleId = new Integer(roleId);
    return this.rolesMap.containsValue(potentialRoleId.toString());
  }
  
  /**
   * A method that will return the role type name of its given role type value.
   * @return the key (role type name) that belongs to the given role type value
   */
  public String getRoleName(int roleValue) {
    String roleName = "";
    // Check to see if the given role type ID exists in the enum map.
    if (this.hasRoleValue(roleValue)) {  
      for (Roles roleType : this.getRoleKeys()) {
        // Iterate over the set of role type keys, fetching its corresponding
        // value, while also making the given roleValue an Integer object.
        String roleTypeVal = this.getExistingRoles().get(roleType);
        Integer roleTypeValIntGiven = new Integer(roleValue);
        // If the key's value is equal to the given role type -> return string representation
        if (roleTypeVal.equals(roleTypeValIntGiven.toString())) {
          roleName = roleType.toString();
        }
      }
    }
    return roleName;
  }
  
  /**
   * A method that will return the value of a given String representation of a role type as
   * an int.
   * @param roleTypeKey the string representing the key in the role types enum map
   * @return the value of the given key IF it exists in the enum map. -1 will be returned if
   *     said key is not found in the enum map
   */
  public int getRoleId(String roleTypeKey) {
    Set<Roles> roleTypeKeys = this.getRoleKeys();
    int roleTypeVal = -1;
    // We will iterate through each of the keys and check to see if any of them match the
    // key specified in the input. If they match, get the value of the key and convert to int
    for (Roles roleType : roleTypeKeys) {
      String roleTypeName = roleType.toString();
      if (roleTypeKey.equalsIgnoreCase(roleTypeName)) {
        Integer roleTypeValString = new Integer(rolesMap.get(roleType));
        roleTypeVal = roleTypeValString.intValue();
      }
    }
    return roleTypeVal;
  }
  
  
  /**
   * A method that will update the enum map based on whatever is in the database at the given
   * time.
   */
  public void updateRolesMap() {
    // Before we remap our values, we must first get rid of the current mapping so that we can
    // start fresh.
    this.rolesMap.clear();
    // We will need to iterate through all of the constants within our Roles enum
    for (Roles role : Roles.values()) {
      // On each constant, we will get its String representation
      String currRoleKey = role.toString();
      // As we are iterating through each of the constants in our Roles enum, we will also
      // iterate through each of the roles in the database in order to get the appropriate 
      // key-value pairing
      List<Integer> roleIds = DatabaseSelectHelper.getRoles();
      // We must make sure that the list we have is not null.
      if (!(roleIds == null)) {
        for (int currRoleNum : roleIds) {
          // We will get the role that has the corresponding roleID in the database
          String currRoleValue = DatabaseSelectHelper.getRole(currRoleNum);
          // If that role is equal to our current constant (currRoleKey), we will map the current
          // role constant to that role value.
          if (currRoleKey.equalsIgnoreCase(currRoleValue)) {
            Number roleValue = new Integer(currRoleNum);
            this.rolesMap.put(role, roleValue.toString());
          }
        }
      }
    }
  }
}
