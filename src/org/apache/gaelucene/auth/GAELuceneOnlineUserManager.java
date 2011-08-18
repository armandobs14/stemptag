package org.apache.gaelucene.auth;

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class GAELuceneOnlineUserManager {

  private static GAELuceneOnlineUserManager instance = new GAELuceneOnlineUserManager();

  private GAELuceneOnlineUserManager() {
  }

  public static GAELuceneOnlineUserManager getInstance() {
    return instance;
  }

  public GAELuceneOnlineUser getOnlineUser(HttpServletRequest request) throws GAELuceneAuthException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user == null) {
      throw new GAELuceneAuthException(GAELuceneAuthException.NOT_LOGIN);
    }
    GAELuceneOnlineUser onlineUser = getAuthenticatedUser(request, user, user.getEmail());
    if (onlineUser == null) {
      if (GAELuceneReservedUsers.isReservedUser(user.getEmail())) {
        onlineUser = getReservedUser(request, user, user.getEmail());
      }
    }
    if (onlineUser == null) {
        onlineUser = getGuestUser(request, user, user.getEmail());
    }
    return onlineUser;
  }

  private GAELuceneOnlineUser getAuthenticatedUser(HttpServletRequest request, User user, String email) {
    GAELuceneUser gaeuser = GAELuceneUserJDO.get(email);
    if (gaeuser == null) {
      return null;
    }
    GAELuceneOnlineUser authenticatedUser = new GAELuceneOnlineUser(request, false);
    authenticatedUser.setEmail(user.getEmail());
    authenticatedUser.setAuthDomain(user.getAuthDomain());
    authenticatedUser.setNickName(user.getNickname());
    GAELucenePermissionImpl permission = getAuthenticatedPermission(gaeuser);
    authenticatedUser.setPermission(permission);
    return authenticatedUser;
  }

  private GAELucenePermissionImpl getAuthenticatedPermission(GAELuceneUser gaeuser) {
    GAELucenePermissionImpl permission = new GAELucenePermissionImpl();
    ArrayList<Integer> permList = gaeuser.getPermissions();
    if (permList != null) {
      for (int i = 0, n = permList.size(); i < n; i++) {
        permission.setPermission(permList.get(i));
      }
    }
    permission.setPermission(GAELucenePermission.PERMISSION_AUTHENTICATED);
    return permission;
  }

  private GAELuceneOnlineUser getReservedUser(HttpServletRequest request, User user, String email) {
    GAELuceneOnlineUser reservedUser = new GAELuceneOnlineUser(request, false);
    reservedUser.setEmail(user.getEmail());
    reservedUser.setAuthDomain(user.getAuthDomain());
    reservedUser.setNickName(user.getNickname());
    GAELucenePermissionImpl permission = getResercedUserPermission();
    reservedUser.setPermission(permission);
    return reservedUser;
  }

  private GAELucenePermissionImpl getResercedUserPermission() {
    GAELucenePermissionImpl permission = new GAELucenePermissionImpl();
    permission.setPermission(GAELucenePermission.PERMISSION_SYSTEM_ADMIN);
    permission.setPermission(GAELucenePermission.PERMISSION_AUTHENTICATED);
    return permission;
  }

  private GAELuceneOnlineUser getGuestUser(HttpServletRequest request, User user, String email) {
    GAELuceneOnlineUser guestUser = new GAELuceneOnlineUser(request, true);
    guestUser.setEmail(user.getEmail());
    guestUser.setAuthDomain(user.getAuthDomain());
    guestUser.setNickName(user.getNickname());
    GAELucenePermissionImpl permission = getGuestUserPermission();
    guestUser.setPermission(permission);
    return guestUser;
  }

  private GAELucenePermissionImpl getGuestUserPermission() {
    GAELucenePermissionImpl permission = new GAELucenePermissionImpl();
    permission.setPermission(GAELucenePermission.PERMISSION_AUTHENTICATED);
    return permission;
  }

  public String createLoginURL(HttpServletRequest request) {
    return UserServiceFactory.getUserService().createLoginURL(request.getRequestURI());
  }

}