package org.apache.lucene.index;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.apache.gaelucene.PMF;
import org.apache.lucene.store.GAEDirectory;
import org.apache.lucene.store.GAEIndexCategory;

public class GAEIndexReaderPool {

  private static Logger log = Logger.getLogger(GAEIndexReaderPool.class.getName());

  HashMap<String, GAEIndexReader> cachedReaders;

  HashMap<String, GAEIndexCategory> cachedCategories;

  HashMap<String, GAEDirectory> cachedDirectories;

  private static GAEIndexReaderPool pool = new GAEIndexReaderPool();

  private GAEIndexReaderPool() {
    init();
  }

  private void init() {
    reinit();
  }

  public void reinit() {
    if (cachedReaders != null) {
      cachedReaders.clear();
    } else {
      cachedReaders = new HashMap<String, GAEIndexReader>();
    }
    if (cachedCategories != null) {
      cachedCategories.clear();
    } else {
      cachedCategories = new HashMap<String, GAEIndexCategory>();
    }
    if (cachedDirectories != null) {
      cachedDirectories.clear();
    } else {
      cachedDirectories = new HashMap<String, GAEDirectory>();
    }
  }

  public static GAEIndexReaderPool getInstance() {
    return pool;
  }

  private static SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public synchronized GAEIndexReader borrowReader(String category) throws IOException {
    log.info("trying to verify index '" + category + "' version");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEIndexCategory.class);
    query.setFilter("cat == category");
    query.declareParameters("String category");
    query.setOrdering("lastModified desc");
    List<GAEIndexCategory> entities = (List<GAEIndexCategory>) query.execute(category);
    if (entities.size() <= 0) { return null; }
    GAEIndexCategory dbCategory = entities.get(0);
    Long version = dbCategory.getVer();
    GAEIndexCategory cachedCategory = cachedCategories.get(category);
    if (cachedCategory == null || cachedCategory.lessThan(dbCategory)) {
      if (cachedCategory == null) {
        log.info("trying to init index reader for '" + category + "', version=" + version);
      } else {
        log.info("trying to reinit index reader for '" + category + "' version=" + version);
      }
      long timeStart = System.currentTimeMillis();
      GAEDirectory directory = cachedDirectories.get(category);
      if (directory == null) {
        log.info("trying to instance a directory for '" + category + "' version=" + version);
        directory = new GAEDirectory(category, version);
        cachedDirectories.put(category, directory);
      }
      try {
        GAEIndexReader reader = GAEIndexReader.getReader(directory);
        long timeEnd = System.currentTimeMillis();
        log.info("'" + category + "' index reader (re)inited, " + (timeEnd - timeStart) + " ms used.");
        cachedReaders.put(category, reader);
        cachedCategories.put(category, dbCategory);
      } catch (Exception e) {
        log.warning("failed to (re)init reader for '" + category + ", version=" + version + ", because:" + e);
        StackTraceElement[] stes = e.getStackTrace();
        for (int i = 0; i < stes.length; i++) {
          log.warning(stes[i].toString());
        }
        return null;
      }
    }
    GAEIndexReader reader = cachedReaders.get(category);
    reader.borrow();
    return reader;
  }

  public synchronized void returnReader(GAEIndexReader indexReader) throws IOException {
    indexReader.returnBack();
  }

  public synchronized void reloadReader(String category, long version) throws IOException {
    GAEIndexReader oldReader = cachedReaders.get(category);
    GAEDirectory directory = new GAEDirectory(category, new Long(version));
    GAEIndexReader newReader = GAEIndexReader.getReader(directory);
    cachedReaders.put(category, newReader);
    if (oldReader != null) { oldReader.destory(); }
    return;
  }

  static SimpleDateFormat lastUsedDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public void showPoolStats(OutputStream out) throws IOException {
    log.info("print pooled readers stats ...");
    if (cachedReaders.size() == 0) {
      out.write("The pool is empty!".getBytes());
      return;
    }
    int i = 0;
    for (Iterator<String> iter = cachedReaders.keySet().iterator(); iter.hasNext();) {
      i++;
      String category = iter.next();
      GAEIndexReader indexReader = cachedReaders.get(category);
      StringBuffer statsInfo = new StringBuffer(128);
      statsInfo.append("cat:").append(category).append(";");
      if (indexReader == null) {
        statsInfo.append("\tNA\n");
      } else {
        long createdTime = indexReader.getCreatedTime();
        boolean isClosed = indexReader.isClosed();
        int refCurCount = indexReader.getRefCurCount();
        int refTotalCount = indexReader.getRefTotalCount();
        long lastUsed = indexReader.getLastUsed();
        statsInfo.append("\tisClosed=").append(isClosed).append(";");
        statsInfo.append("\trefCount=").append(refTotalCount).append("/").append(refCurCount).append(";");
        statsInfo.append("\tcreatedTime=").append(lastUsedDateFormat.format(new Date(createdTime))).append(";");
        statsInfo.append("\tlastUsed=").append( (lastUsed == 0) ? "NA" : (lastUsedDateFormat.format(new Date(lastUsed))));
        statsInfo.append("\n");
      }
      out.write(statsInfo.toString().getBytes());
    }
  }

}