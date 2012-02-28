package org.apache.gaelucene.auth;

public abstract class GAELuceneAbstractPermission implements GAELucenePermission {

  public static final int[] globalCombinedPermissionArray = { PERMISSION_SYSTEM_ADMIN, };

  public static String getDescription(int permission) {
    switch (permission) {
    case PERMISSION_SYSTEM_ADMIN:
      return "System Admin";
    }
    return "Unknown(" + permission + ")";
  }

}