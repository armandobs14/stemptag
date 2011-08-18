package org.apache.gaelucene.dashboard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.gaelucene.PMF;
import org.apache.gaelucene.auth.GAELuceneAuthException;
import org.apache.gaelucene.auth.GAELuceneOnlineUser;
import org.apache.gaelucene.auth.GAELuceneOnlineUserManager;
import org.apache.gaelucene.auth.GAELucenePermission;
import org.apache.gaelucene.tools.LuceneIndexImportHandler;
import org.apache.lucene.store.GAEFile;
import org.apache.lucene.store.GAEFileContentJDO;
import org.apache.lucene.store.GAEFileJDO;
import org.apache.lucene.store.GAEIndexCategory;
import org.apache.lucene.store.GAEIndexCategoryJDO;

public class GAELuceneIndexWebHandler {

  private static Logger log = Logger.getLogger(GAELuceneIndexWebHandler.class.getName());

  private GAELuceneOnlineUserManager userManager = GAELuceneOnlineUserManager.getInstance();

  public void processImportPrepackagedIndex(HttpServletRequest request) throws GAELuceneAuthException, IOException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.canAdminSystem()) throw new GAELuceneAuthException(0);
    String dirName = ParamUtil.getString(request, "dir", "").trim();
    String category = ParamUtil.getString(request, "cat", "").trim();
    log.info("trying to import prepackaged index under '" + dirName + "' to db.");
    URL resource = this.getClass().getClassLoader().getResource(dirName);
    if (resource == null) {
      URL indicesRoot = this.getClass().getClassLoader().getResource("./");
      throw new IOException("FAILED TO LOCATE '" + dirName + "' UNDER '" + indicesRoot + "'!");
    }
    File indxDir = new File(resource.getFile());
    LuceneIndexImportHandler.importToGAEDataStore(indxDir, category);
  }

  public void processListGAEFiles(HttpServletRequest request) throws GAELuceneAuthException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query1 = pm.newQuery(GAEIndexCategory.class);
    query1.setOrdering("cat asc");
    List<GAEIndexCategory> categories = (List<GAEIndexCategory>) query1.execute();
    pm.detachCopyAll(categories);
    Query query2 = pm.newQuery(GAEFile.class);
    query2.setOrdering("name asc");
    List<GAEFile> files = (List<GAEFile>) query2.execute();
    pm.detachCopyAll(files);
    request.setAttribute("categories", categories);
    request.setAttribute("files", files);
    pm.close();
  }

  public void deleteGAEFile(HttpServletRequest request) throws GAELuceneAuthException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.canAdminSystem()) throw new GAELuceneAuthException(0);
    long id = ParamUtil.getLong(request, "id", 0);
    if (id > 0) {
      log.info("trying to delete GAEFile entity '" + id + "'");
      GAEFileJDO.delete(new Long(id));
    }
  }

  public void batchDeleteGAEFiles(HttpServletRequest request) throws GAELuceneAuthException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.canAdminSystem()) throw new GAELuceneAuthException(0);
    String category = ParamUtil.getString(request, "cat", "").trim();
    long version = ParamUtil.getLong(request, "ver", 0);
    log.info("trying to delete GAEFile entities of '" + category + "' with version='" + version + "'");
    GAEFileJDO.batchDelete(category, new Long(version));
  }

  public void deleteGAECategory(HttpServletRequest request) throws GAELuceneAuthException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.canAdminSystem()) throw new GAELuceneAuthException(0);
    long id = ParamUtil.getLong(request, "id", 0);
    if (id > 0) {
      log.info("trying to delete GAECategory entity '" + id + "'");
      GAEIndexCategoryJDO.delete(new Long(id));
    }
  }

  public void processRegisterNewFile(HttpServletRequest request, HttpServletResponse response) throws GAELuceneAuthException, IOException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.canAdminSystem()) throw new GAELuceneAuthException(0);
    String category = ParamUtil.getString(request, "cat", "").trim();
    long version = ParamUtil.getLong(request, "ver", 0);
    String fileName = ParamUtil.getString(request, "name", "").trim();
    long fileLength = ParamUtil.getLong(request, "length", -1);
    long lastModified = ParamUtil.getLong(request, "lastModified", -1);
    int segmentCount = ParamUtil.getInteger(request, "segmentCount", 0);
    log.info("receive a new file {cat:" + category + "; name:" + fileName + "; length:" + fileLength + "; lastModified:" + lastModified + "; segmentCount:" + segmentCount + "}.");
    Long fileId = GAEFileJDO.saveOrUpdate(category, new Long(version), fileName, new Long(fileLength), new Long(lastModified), new Integer(segmentCount));
    log.info("registered fileId:" + fileId);
    PrintWriter out = response.getWriter();
    out.print(fileId);
    out.flush();
  }

  private Properties getRequestParameters(String queryString, String encoding) {
    Properties parameters = new Properties();
    if (queryString == null || queryString.trim().length() <= 0) return parameters;
    int i = 0;
    char c = queryString.charAt(i);
    while (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
      c = queryString.charAt(++i);
    }
    if (i > 0) queryString = queryString.substring(i);
    queryString = queryString.replaceAll("&amp;", "&");
    String[] tokens = queryString.split("[&]");
    for (int j = 0; j < tokens.length; j++) {
      int p = tokens[j].indexOf('=');
      if (p < 0) continue;
      String pname = tokens[j].substring(0, p);
      String pvalue = tokens[j].substring(p + 1);
      try {
        pvalue = URLDecoder.decode(pvalue, encoding);
      } catch (Exception e) {
        pvalue = URLDecoder.decode(pvalue);
      }
      parameters.setProperty(pname, pvalue);
      log.fine("parameter[" + pname + "]={" + pvalue + "}");
    }
    return parameters;
  }

  public void processCommitNewFile(HttpServletRequest request, HttpServletResponse response) throws GAELuceneAuthException, IOException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.canAdminSystem()) throw new GAELuceneAuthException(0);
    String queryString = request.getQueryString();
    Properties parameters = getRequestParameters(queryString, "UTF-8");
    long fileId = Long.parseLong(parameters.getProperty("fileId", "0").trim());
    int segmentNo = Integer.parseInt(parameters.getProperty("segmentNo", "0").trim());
    long segmentLength = Long.parseLong(parameters.getProperty("segmentLength", "0").trim());
    log.info("receive a new segment {fileId:" + fileId + "; segmentNo:" + segmentNo + "; segmentLength:" + segmentLength + "}.");
    ServletInputStream is = request.getInputStream();
    ByteArrayOutputStream dos = new ByteArrayOutputStream();
    byte[] buffer = new byte[10240];
    for (int bytes = is.read(buffer, 0, 10240); bytes >= 0;) {
      dos.write(buffer, 0, bytes);
      bytes = is.read(buffer, 0, 10240);
    }
    log.info("receive " + dos.size() + " bytes from remote host");
    byte[] data = dos.toByteArray();
    GAEFileContentJDO.saveOrUpdate(new Long(fileId), new Integer(segmentNo), new Long(segmentLength), data);
    PrintWriter out = response.getWriter();
    out.print("OK");
    out.flush();
  }

  public void processActivateNewIndex(HttpServletRequest request, HttpServletResponse response) throws GAELuceneAuthException, IOException {
    GAELuceneOnlineUser onlineUser = userManager.getOnlineUser(request);
    GAELucenePermission permission = onlineUser.getPermission();
    if (!permission.canAdminSystem()) throw new GAELuceneAuthException(0);
    String category = ParamUtil.getString(request, "cat", "").trim();
    long version = ParamUtil.getLong(request, "ver", 0);
    log.info("trying to activate index of '" + category + "', version=" + version);
    GAEIndexCategoryJDO.saveOrUpdate(category, new Long(version), new Long(System.currentTimeMillis()));
    PrintWriter out = response.getWriter();
    out.print("OK");
    out.flush();
  }

}