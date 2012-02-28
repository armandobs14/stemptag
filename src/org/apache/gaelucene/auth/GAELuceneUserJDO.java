package org.apache.gaelucene.auth;

import java.util.ArrayList;
import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.apache.gaelucene.PMF;

public class GAELuceneUserJDO {
	
  public static GAELuceneUser get(Long uId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAELuceneUser.class);
    query.setFilter("uId == UID");
    query.declareParameters("Long UID");
    List<GAELuceneUser> users = (List<GAELuceneUser>) query.execute(uId);
    GAELuceneUser user = null;
    if (users.size() > 0) {
      user = users.get(0);
      pm.retrieve(user);
    }
    pm.close();
    return user;
  }

  public static GAELuceneUser get(String email) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAELuceneUser.class);
    query.setFilter("email == EMAIL");
    query.declareParameters("String EMAIL");
    List<GAELuceneUser> users = (List<GAELuceneUser>) query.execute(email);
    GAELuceneUser user = null;
    if (users.size() > 0) { user = users.get(0); }
    pm.close();
    return user;
  }

  public static List<GAELuceneUser> getUsers() {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAELuceneUser.class);
    List<GAELuceneUser> users = (List<GAELuceneUser>) query.execute();
    pm.detachCopyAll(users);
    pm.close();
    return users;
  }

  public static void saveOrUpdate(String email, ArrayList<Integer> permissions) {
    if (GAELuceneReservedUsers.isReservedUser(email)) {
      return;
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAELuceneUser.class);
    query.setFilter("email == EMAIL");
    query.declareParameters("String EMAIL");
    List<GAELuceneUser> users = (List<GAELuceneUser>) query.execute(email);
    if (users.size() == 0) {
      GAELuceneUser user = new GAELuceneUser();
      user.setEmail(email);
      user.setPermissions(permissions);
      pm.makePersistent(user);
    }
    pm.close();
  }

  public static void updatePermission(Long uId, ArrayList<Integer> permissions) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAELuceneUser.class);
    query.setFilter("uId == UID");
    query.declareParameters("Long UID");
    List<GAELuceneUser> users = (List<GAELuceneUser>) query.execute(uId);
    if (users.size() > 0) {
      GAELuceneUser user = users.get(0);
      user.setPermissions(permissions);
    }
    pm.close();
  }

  public static void delete(Long uId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAELuceneUser.class);
    query.setFilter("uId == UID");
    query.declareParameters("Long UID");
    List<GAELuceneUser> users = (List<GAELuceneUser>) query.execute(uId);
    GAELuceneUser user = null;
    if (users.size() > 0) {
      user = users.get(0);
      pm.deletePersistent(user);
    }
    pm.close();
  }

}