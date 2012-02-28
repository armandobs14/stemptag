package org.apache.lucene.store;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.apache.gaelucene.PMF;

public class GAEDirectory extends Directory {

  private static Logger log = Logger.getLogger(GAEDirectory.class.getName());

  private String category;

  private Long version;

  private HashMap<String, GAEFile> fileMap = new HashMap<String, GAEFile>();

  public GAEDirectory(String category, Long version) {
    super();
    this.category = category;
    this.version = version;
  }

  @Override
  public void close() throws IOException { }

  @Override
  public void deleteFile(final String fileName) throws IOException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEFile.class);
    query.setFilter("cat == category && ver == version && name == fileName");
    query.declareParameters("String category, Long version, String fileName");
    List<GAEFile> files = (List<GAEFile>) query.execute(this.category, this.version, fileName);
    for (int i = 0; i < files.size(); i++) {
      GAEFile file = files.get(i);
      pm.deletePersistent(file);
    }
  }

  @Override
  public boolean fileExists(final String fileName) throws IOException {
    log.info("detect if file '" + fileName + "' exist");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEFile.class);
    query.setFilter("cat == category && ver == version && name == fileName");
    query.declareParameters("String category, Long version, String fileName");
    List<GAEFile> files = (List<GAEFile>) query.execute(this.category, this.version, fileName);
    return files.size() > 0;
  }

  @Override
  public long fileLength(final String fileName) throws IOException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEFile.class);
    query.setFilter("cat == category && ver == version && name == fileName");
    query.declareParameters("String category, Long version, String fileName");

    List<GAEFile> files = (List<GAEFile>) query.execute(this.category, this.version, fileName);
    GAEFile file = files.get(0);
    return file.getLength();
  }

  @Override
  public long fileModified(final String fileName) throws IOException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEFile.class);
    query.setFilter("cat == category && ver == version && name == fileName");
    query.declareParameters("String category, Long version, String fileName");
    List<GAEFile> files = (List<GAEFile>) query.execute(this.category, this.version, fileName);
    GAEFile file = files.get(0);
    return file.getLastModified();
  }

  @Override
  public String[] list() throws IOException {
    log.info("trying to fetch index files");
    if (fileMap.size() == 0) {
      PersistenceManager pm = PMF.get().getPersistenceManager();
      Query query = pm.newQuery(GAEFile.class);
      query.setFilter("cat == category && ver == version");
      query.declareParameters("String category, Long version");
      List<GAEFile> files = (List<GAEFile>) query.execute(this.category, this.version);
      String[] fileNames = new String[files.size()];
      for (int i = 0; i < files.size(); i++) {
        GAEFile file = files.get(i);
        fileNames[i] = file.getName();
        fileMap.put(file.getName(), file);
      }
      return fileNames;
    } else {
      Set<String> fileNames = fileMap.keySet();
      String[] result = new String[fileNames.size()];
      int i = 0;
      Iterator<String> it = fileNames.iterator();
      while (it.hasNext()) {
        result[i++] = (String) it.next();
      }
      return result;
    }
  }

  @Override
  public void renameFile(final String from, final String to) throws IOException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEFile.class);
    query.setFilter("cat == category && ver == version && name == fileName");
    query.declareParameters("String category, Long version, String fileName");
    List<GAEFile> files = (List<GAEFile>) query.execute(this.category, this.version, from);
    GAEFile file = files.get(0);
    file.setName(to);
  }

  @Override
  public void touchFile(final String fileName) throws IOException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GAEFile.class);
    query.setFilter("cat == category && ver == version && name == fileName");
    query.declareParameters("String category, Long version, String fileName");
    List<GAEFile> files = (List<GAEFile>) query.execute(this.category, this.version, fileName);
    GAEFile file = files.get(0);
    file.setLastModified(System.currentTimeMillis());
  }

  @Override
  public IndexInput openInput(String fileName) throws IOException {
    //log.info("trying to open input for: " + fileName);
    GAEFile file = fileMap.get(fileName);
    if (file == null) {
      PersistenceManager pm = PMF.get().getPersistenceManager();
      Query query = pm.newQuery(GAEFile.class);
      query.setFilter("cat == category && ver == version && name == fileName");
      query.declareParameters("String category, Long version, String fileName");
      List<GAEFile> files = (List<GAEFile>) query.execute(this.category, this.version, fileName);
      if (files.size() == 0) {
        log.warning("failed to fetch '" + this.category + "-" + this.version + "-" + fileName + "', not exist!");
      }
      file = files.get(0);
      fileMap.put(fileName, file);
    }

    return new GAEIndexInput(file);
  }

  @Override
  public IndexOutput createOutput(String fileName) throws IOException {
    log.warning("GAEDirectory.createOutput - creating output for: " + fileName);
    throw new IOException("method should not be invoked!");
  }

}