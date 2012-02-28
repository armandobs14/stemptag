package org.apache.lucene.store;

import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.apache.gaelucene.PMF;
import com.google.appengine.api.datastore.Blob;

public class GAEFileContentJDO {

  public static GAEFileContent get(Long fileId, Integer segmentNo) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEFileContent.class);
    query.setFilter("fileId == fId && segmentNo == sNo");
    query.declareParameters("Long fId, Integer sNo");
    List<GAEFileContent> gaeContents = (List<GAEFileContent>) query.execute(fileId, segmentNo);
    GAEFileContent gaeContent = null;
    if (gaeContents.size() > 0) {
      gaeContent = gaeContents.get(0);
      pm.retrieve(gaeContent);
    }
    pm.close();
    return gaeContent;
  }

  public static void saveOrUpdate(Long fileId, Integer segmentNo, Long segmentLength, byte[] content) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEFileContent.class);
    query.setFilter("fileId == fId && segmentNo == sNo");
    query.declareParameters("Long fId, Integer sNo");
    List<GAEFileContent> segments = (List<GAEFileContent>) query.execute(fileId, segmentNo);
    if (segments.size() == 0) {
      GAEFileContent gaeContent = new GAEFileContent();
      gaeContent.setFileId(fileId);
      gaeContent.setSegmentNo(segmentNo);
      gaeContent.setSegmentLength(segmentLength);
      gaeContent.setContent(new Blob(content));
      pm.makePersistent(gaeContent);
    } else {
      GAEFileContent gaeContent = segments.get(0);
      gaeContent.setSegmentLength(segmentLength);
      gaeContent.setContent(new Blob(content));
    }
    pm.close();
  }

  public static void delete(Long fileId, Integer segmentNo) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEFileContent.class);
    query.setFilter("fileId == fId && segmentNo == sNo");
    query.declareParameters("Long fId, Integer sNo");
    List<GAEFileContent> gaeContents = (List<GAEFileContent>) query.execute(fileId, segmentNo);
    GAEFileContent gaeContent = null;
    if (gaeContents.size() > 0) {
      gaeContent = gaeContents.get(0);
      pm.deletePersistent(gaeContent);
    }
    pm.close();
  }

}