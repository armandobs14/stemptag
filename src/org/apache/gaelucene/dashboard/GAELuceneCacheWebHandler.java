package org.apache.gaelucene.dashboard;

import javax.cache.CacheStatistics;
import javax.servlet.http.HttpServletRequest;
import org.apache.gaelucene.CF;
import org.apache.gaelucene.auth.GAELuceneAuthException;
import org.apache.gaelucene.auth.GAELuceneOnlineUser;
import org.apache.gaelucene.auth.GAELuceneOnlineUserManager;
import org.apache.gaelucene.auth.GAELucenePermission;

public class GAELuceneCacheWebHandler {

  private GAELuceneOnlineUserManager userManager = GAELuceneOnlineUserManager.getInstance();

  public void processShowCacheStats(HttpServletRequest request) throws GAELuceneAuthException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.isAuthenticated()) throw new GAELuceneAuthException(0);
    javax.cache.Cache cache = CF.getCache();
    CacheStatistics stats = cache.getCacheStatistics();
    int cacheSize = cache.size();
    int hits = stats.getCacheHits();
    int misses = stats.getCacheMisses();
    request.setAttribute("Cache.Size", cacheSize);
    request.setAttribute("CacheStatistics.Hits", hits);
    request.setAttribute("CacheStatistics.Misses", misses);
  }

  public void processClearCache(HttpServletRequest request) throws GAELuceneAuthException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.isAuthenticated()) throw new GAELuceneAuthException(0);
    javax.cache.Cache cache = CF.getCache();
    cache.clear();
  }

}