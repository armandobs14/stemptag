package org.apache.gaelucene.dashboard;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.gaelucene.auth.GAELuceneAuthException;
import org.apache.gaelucene.auth.GAELuceneReservedUsers;
import org.apache.gaelucene.auth.GAELuceneOnlineUser;
import org.apache.gaelucene.auth.GAELuceneOnlineUserManager;
import org.apache.gaelucene.auth.GAELucenePermission;
import org.apache.gaelucene.auth.GAELuceneUser;
import org.apache.gaelucene.auth.GAELuceneUserJDO;

public class GAELuceneUserWebHandler {
	
  private GAELuceneOnlineUserManager userManager = GAELuceneOnlineUserManager.getInstance();

  public void processListUser(HttpServletRequest request) throws GAELuceneAuthException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.isAuthenticated()) throw new GAELuceneAuthException(0);
    List<GAELuceneUser> users = GAELuceneUserJDO.getUsers();
    request.setAttribute("users", users);
    List<String> reservedUsers = GAELuceneReservedUsers.getReservedUsers();
    request.setAttribute("reservedUsers", reservedUsers);
  }

  public void processAddUser(HttpServletRequest request) throws GAELuceneAuthException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.canAdminSystem()) throw new GAELuceneAuthException(0);
    String email = ParamUtil.getString(request, "email");
    GAELuceneUserJDO.saveOrUpdate(email, new ArrayList<Integer>());
  }

  public void prepareEditUserPermission(HttpServletRequest request) throws GAELuceneAuthException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.canAdminSystem()) throw new GAELuceneAuthException(0);
    long uId = ParamUtil.getLong(request, "uid", 0);
    GAELuceneUser oneUser = GAELuceneUserJDO.get(uId);
    request.setAttribute("user", oneUser);
  }

  public void processUpdateUserPermission(HttpServletRequest request) throws GAELuceneAuthException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.canAdminSystem()) throw new GAELuceneAuthException(0);
    long uId = ParamUtil.getLong(request, "uid", 0);
    ArrayList<Integer> permissions = (ArrayList<Integer>) ParamUtil.getIntegers(request, "perm");
    GAELuceneUserJDO.updatePermission(uId, permissions);
  }

  public void processDeleteUser(HttpServletRequest request) throws GAELuceneAuthException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.canAdminSystem()) throw new GAELuceneAuthException(0);
    long uId = ParamUtil.getLong(request, "uid", 0);
    GAELuceneUserJDO.delete(new Long(uId));
  }

}