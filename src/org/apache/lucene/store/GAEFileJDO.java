package org.apache.lucene.store;

import java.util.List;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.apache.gaelucene.PMF;

public class GAEFileJDO {

  private static Logger log = Logger.getLogger(GAEFileJDO.class.getName());

  public static Long saveOrUpdate(String category, Long version, String fileName, Long fileLength,
    Long lastModified, Integer segmentCount) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEFile.class);
    query.setFilter("cat == category && ver == version && name == fileName");
    query.declareParameters("String category, Long version, String fileName");
    List<GAEFile> files = (List<GAEFile>) query.execute(category, version, fileName);
    GAEFile gaeFile = null;
    if (files.size() > 0) {
      gaeFile = files.get(0);
      gaeFile.setLength(fileLength);
      gaeFile.setLastModified(lastModified);
      gaeFile.setSegmentCount(segmentCount);
      gaeFile.setDeleted(new Boolean(false));
    } else {
      gaeFile = new GAEFile();
      gaeFile.setCat(category);
      gaeFile.setVer(version);
      gaeFile.setName(fileName);
      gaeFile.setLength(fileLength);
      gaeFile.setLastModified(lastModified);
      gaeFile.setSegmentCount(segmentCount);
      gaeFile.setDeleted(false);
      pm.makePersistent(gaeFile);
    }
    pm.close();
    return gaeFile.getId();
  }

  public static void delete(Long fileId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEFile.class);
    query.setFilter("id == fileId");
    query.declareParameters("Long fileId");
    List<GAEFile> files = (List<GAEFile>) query.execute(fileId);
    for (int i = 0; i < files.size(); i++) {
      GAEFile file = files.get(i);
      for (int sNo = 0; sNo < file.getSegmentCount(); sNo++) {
        GAEFileContentJDO.delete(file.getId(), sNo);
      }
      pm.deletePersistent(file);
    }
    pm.close();
  }

  public static void batchDelete(String category, Long version) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEFile.class);
    query.setFilter("cat == category && ver == version");
    query.declareParameters("String category, Long version");
    List<GAEFile> files = (List<GAEFile>) query.execute(category, version);
    for (int i = 0; i < files.size(); i++) {
      GAEFile file = files.get(i);
      for (int sNo = 0; sNo < file.getSegmentCount(); sNo++) {
        GAEFileContentJDO.delete(file.getId(), sNo);
      }
      pm.deletePersistent(file);
    }
  }

}