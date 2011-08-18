package org.apache.gaelucene.auth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class GAELuceneReservedUsers {

  private static HashSet<String> reservedUsers = new HashSet<String>();

  static {
    File gaeluceneUsersFile = null;
    BufferedReader reader = null;
    try {
      gaeluceneUsersFile = new File(GAELuceneReservedUsers.class.getClassLoader().getResource("gaelucene-users.txt").getFile());
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(gaeluceneUsersFile), "UTF-8"));
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        line = line.trim();
        if (line.startsWith("#")) {
          continue;
        }
        reservedUsers.add(line.toLowerCase());
      }
    } catch (IOException ioe) {
      System.err.println("[ERROR] - GAELuceneReservedUsers - FAILED to load reserved user list from '" + gaeluceneUsersFile.getAbsolutePath() + "', because:" + ioe);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception e) { }
      }
    }
  }

  public static boolean isReservedUser(String userEmail) {
    if (userEmail == null) { return false; }
    userEmail = userEmail.toLowerCase();
    return reservedUsers.contains(userEmail);
  }

  public static List<String> getReservedUsers() {
    ArrayList<String> users = new ArrayList<String>();
    for (Iterator<String> iter = reservedUsers.iterator(); iter.hasNext();) {
      users.add(iter.next());
    }
    return users;
  }

}