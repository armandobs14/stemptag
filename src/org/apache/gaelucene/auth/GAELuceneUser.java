package org.apache.gaelucene.auth;

import java.util.ArrayList;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class GAELuceneUser {
	
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long uId;

  @Persistent
  private String email;

  @Persistent
  private ArrayList<Integer> permissions;

  public Long getUId() {
    return uId;
  }

  public void setUId(Long uId) {
    this.uId = uId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public ArrayList<Integer> getPermissions() {
    return permissions;
  }

  public void setPermissions(ArrayList<Integer> permissions) {
    this.permissions = permissions;
  }

}