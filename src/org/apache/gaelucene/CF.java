package org.apache.gaelucene;

import java.util.HashMap;
import java.util.logging.Logger;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;

public class CF {

  private static Logger log = Logger.getLogger(CF.class.getName());

  private static CacheFactory cacheFactory = null;

  private static Cache cache = null;
  
  static {
    try {
      cacheFactory = CacheManager.getInstance().getCacheFactory();
      HashMap props = new HashMap();
      props.put(GCacheFactory.EXPIRATION_DELTA, 3600 * 24 * 7);
      cache = cacheFactory.createCache(props);
    } catch (CacheException e) {
      log.warning("FAILED TO CREATE CACHE INSTANCE!, BECAUSE:" + e);
      e.printStackTrace();
    }
  }

  private CF() { }

  public static CacheFactory get() {
    return cacheFactory;
  }

  public static Cache getCache() {
    return cache;
  }
  
}