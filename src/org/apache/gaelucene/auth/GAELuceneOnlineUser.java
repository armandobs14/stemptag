package org.apache.gaelucene.auth;

import javax.servlet.http.HttpServletRequest;

public class GAELuceneOnlineUser {

  boolean isGuest;
  
  String email;
  
  String authDomain;
  
  String nickName;

  GAELucenePermission permission = null;

  GAELuceneOnlineUser(HttpServletRequest request, boolean isGuest) {
  }

  public boolean isGuest() {
    return isGuest;
  }

  public void setGuest(boolean isGuest) {
    this.isGuest = isGuest;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAuthDomain() {
    return authDomain;
  }

  public void setAuthDomain(String authDomain) {
    this.authDomain = authDomain;
  }

  public String getNickName() {
    return nickName;
  }

  public void setNickName(String nickName) {
    this.nickName = nickName;
  }

  public GAELucenePermission getPermission() {
    return permission;
  }

  public void setPermission(GAELucenePermission permission) {
    this.permission = permission;
  }

}