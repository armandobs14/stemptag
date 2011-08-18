package org.apache.gaelucene.auth;

public class GAELucenePermissionImpl extends GAELuceneAbstractPermission implements GAELucenePermission {

  boolean isAuthenticated = false;
  
  boolean canAdminSystem = false;

  public void setPermission(int permission) {
    switch (permission) {
    case PERMISSION_AUTHENTICATED:
      isAuthenticated = true;
      break;
    case PERMISSION_SYSTEM_ADMIN:
      canAdminSystem = true;
      break;
    }
  }

  public boolean isAuthenticated() {
    return isAuthenticated;
  }

  public boolean canAdminSystem() {
    return canAdminSystem;
  }

}