package org.apache.gaelucene.auth;

public interface GAELucenePermission {

  public static final int PERMISSION_AUTHENTICATED = 1;
  
  public static final int PERMISSION_SYSTEM_ADMIN = 100;

  public boolean isAuthenticated();

  public boolean canAdminSystem();

}