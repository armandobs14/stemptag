package org.apache.lucene.store;

import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.apache.gaelucene.PMF;

public class GAEIndexCategoryJDO {

  public static void saveOrUpdate(String category, Long version, Long lastModified) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEIndexCategory.class);
    query.setFilter("cat == category");
    query.declareParameters("String category");
    List<GAEIndexCategory> categories = (List<GAEIndexCategory>) query.execute(category);
    if (categories.size() == 0) {
      GAEIndexCategory entity = new GAEIndexCategory();
      entity.setCat(category);
      entity.setVer(version);
      entity.setLastModified(lastModified);
      pm.makePersistent(entity);
    } else {
      GAEIndexCategory entity = categories.get(0);
      entity.setVer(version);
      entity.setLastModified(lastModified);
    }
    pm.close();
  }

  public static void delete(Long catId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEIndexCategory.class);
    query.setFilter("id == catId");
    query.declareParameters("Long catId");
    List<GAEIndexCategory> categories = (List<GAEIndexCategory>) query.execute(new Long(catId));
    for (int i = 0; i < categories.size(); i++) {
      GAEIndexCategory category = categories.get(i);
      pm.deletePersistent(category);
    }
    pm.close();
  }

}