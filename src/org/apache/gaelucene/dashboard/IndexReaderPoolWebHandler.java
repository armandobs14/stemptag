package org.apache.gaelucene.dashboard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.apache.gaelucene.auth.GAELuceneAuthException;
import org.apache.gaelucene.auth.GAELuceneOnlineUser;
import org.apache.gaelucene.auth.GAELuceneOnlineUserManager;
import org.apache.gaelucene.auth.GAELucenePermission;
import org.apache.lucene.index.GAEIndexReaderPool;

public class IndexReaderPoolWebHandler {

  private static GAEIndexReaderPool readerPool = GAEIndexReaderPool.getInstance();

  private GAELuceneOnlineUserManager userManager = GAELuceneOnlineUserManager.getInstance();

  public void processShowPoolStats(HttpServletRequest request) throws GAELuceneAuthException, IOException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.isAuthenticated()) throw new GAELuceneAuthException(0);
    long freeMemory = Runtime.getRuntime().freeMemory();
    long totalMemory = Runtime.getRuntime().totalMemory();
    long maxMemory = Runtime.getRuntime().maxMemory();
    request.setAttribute("Runtime.totalMemory", totalMemory);
    request.setAttribute("Runtime.maxMemory", maxMemory);
    request.setAttribute("Runtime.freeMemory", freeMemory);
    if (readerPool == null) {
      request.setAttribute("Pool.stat", "The pool HAS NOT BEEN initialized! Please check your log for detail!");
    } else {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      readerPool.showPoolStats(out);
      request.setAttribute("Pool.stat", new String(out.toByteArray(), "UTF-8"));
    }
  }
  
}