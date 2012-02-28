package org.apache.gaelucene.dashboard;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.gaelucene.auth.GAELuceneAuthException;
import org.apache.gaelucene.auth.GAELuceneOnlineUser;
import org.apache.gaelucene.auth.GAELuceneOnlineUserManager;

public class AdminModuleProcessor {

  private static Logger log = Logger.getLogger(AdminModuleProcessor.class.getName());

  private HttpServlet adminServlet = null;
  
  private ServletContext servletContext = null;
  
  private GAELuceneOnlineUserManager userManager = GAELuceneOnlineUserManager.getInstance();

  GAELuceneIndexWebHandler indexWebHandler = new GAELuceneIndexWebHandler();
  
  IndexReaderPoolWebHandler poolWebHandler = new IndexReaderPoolWebHandler();
  
  GAELuceneCacheWebHandler cacheWebHandler = new GAELuceneCacheWebHandler();
  
  GAELuceneUserWebHandler userWebHandler = new GAELuceneUserWebHandler();

  public AdminModuleProcessor(HttpServlet servlet) {
    adminServlet = servlet;
    servletContext = adminServlet.getServletContext();
    servletContext.getContextPath();
  }

  public String process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String userEmail = null;
    long timeStart = 0;
    if (log.isLoggable(Level.INFO)) {
      timeStart = System.currentTimeMillis();
    }
    String pathInfo = request.getPathInfo();
    if (pathInfo == null) {
      pathInfo = "";
    }
    GAELuceneOnlineUser onlineUser = null;
    try {
      onlineUser = userManager.getOnlineUser(request);
    } catch (GAELuceneAuthException ae) {
      response.sendRedirect(userManager.createLoginURL(request));
      return null;
    }
    if (log.isLoggable(Level.INFO)) {
      userEmail = onlineUser.getEmail();
      log.info("AdminModuleProcessor - user(" + userEmail + ") - pathInfo=" + pathInfo);
    }
    String responseURI = null;
    try {
      if ("/list".equals(pathInfo)) {
        indexWebHandler.processListGAEFiles(request);
        responseURI = "/dashboard/gaelucene/listgaefile.jsp";
      }
      else if ("/registernewindexfile".equals(pathInfo)) {
        indexWebHandler.processRegisterNewFile(request, response);
      }
      else if ("/commitnewindexfile".equals(pathInfo)) {
        indexWebHandler.processCommitNewFile(request, response);
      }
      else if ("/activatenewindex".equals(pathInfo)) {
        indexWebHandler.processActivateNewIndex(request, response);
      }
      else if ("/importpackagedindex".equals(pathInfo)) {
        indexWebHandler.processImportPrepackagedIndex(request);
        responseURI = getUrlPattern() + "/list";
      }
      else if ("/deletefileprocess".equals(pathInfo)) {
        indexWebHandler.deleteGAEFile(request);
        responseURI = getUrlPattern() + "/list";
      }
      else if ("/deletecategoryprocess".equals(pathInfo)) {
        indexWebHandler.deleteGAECategory(request);
        responseURI = getUrlPattern() + "/list";
      }
      else if ("/batchdeletefileprocess".equals(pathInfo)) {
        indexWebHandler.batchDeleteGAEFiles(request);
        responseURI = getUrlPattern() + "/list";
      }
      else if ("/showpoolstat".equals(pathInfo)) {
        poolWebHandler.processShowPoolStats(request);
        responseURI = "/dashboard/gaelucene/showpoolstats.jsp";
      }
      else if ("/showcachestat".equals(pathInfo)) {
        cacheWebHandler.processShowCacheStats(request);
        responseURI = "/dashboard/gaelucene/showcachestats.jsp";
      }
      // clear cached objects
      else if ("/clearcache".equals(pathInfo)) {
        cacheWebHandler.processShowCacheStats(request);
        responseURI = getUrlPattern() + "/showcachestat";
      }
      else if ("/listusers".equals(pathInfo)) {
        userWebHandler.processListUser(request);
        responseURI = "/dashboard/gaelucene/listgaeuser.jsp";
      }
      else if ("/adduserprocess".equals(pathInfo)) {
        userWebHandler.processAddUser(request);
        responseURI = getUrlPattern() + "/listusers";
      }
      else if ("/edituserpermission".equals(pathInfo)) {
        userWebHandler.prepareEditUserPermission(request);
        responseURI = "/dashboard/gaelucene/edituserpermission.jsp";
      }
      else if ("/updateuserpermissionprocess".equals(pathInfo)) {
        userWebHandler.processUpdateUserPermission(request);
        responseURI = getUrlPattern() + "/listusers";
      }
      else if ("/deleteruserprocess".equals(pathInfo)) {
        userWebHandler.processDeleteUser(request);
        responseURI = getUrlPattern() + "/listusers";
      }
      else {
        responseURI = "/dashboard/gaelucene/index.jsp";
      }
    } catch (GAELuceneAuthException ae) {
      response.sendRedirect(userManager.createLoginURL(request));
      return null;
    } catch (Exception e) {
      request.setAttribute("requestURI", getUrlPattern() + pathInfo);
      request.setAttribute("exception", e);
      responseURI = "/dashboard/gaelucene/error.jsp";
    }
    if (log.isLoggable(Level.INFO)) {
      long timeUsed = System.currentTimeMillis() - timeStart;
      log.info("AdminModuleProcessor - user(" + userEmail + ") - responseURI=" + responseURI + ", " + timeUsed + " ms used.");
    }
    return responseURI;
  }
  
  public static String getUrlPattern() { return "/gaelucenedashboard"; }

}