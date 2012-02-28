package org.apache.gaelucene.dashboard;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GAELuceneAdminServlet extends HttpServlet {

  public static final long serialVersionUID = 1l;

  private static Logger log = Logger.getLogger(GAELuceneAdminServlet.class.getName());

  private static final long START_TIME = System.currentTimeMillis();

  private AdminModuleProcessor adminModuleProcessor = null;

  private int processCount = 0;

  public static final long getStartTime() {
    return START_TIME;
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    adminModuleProcessor = new AdminModuleProcessor(this);
    log.info("<---- " + GAELuceneAdminServlet.class.getName() + " has been inited. ---->");
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    process(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    process(request, response);
  }

  public void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    processCount++;
    long timeStart = 0;
    if (log.isLoggable(Level.INFO)) {
      timeStart = System.currentTimeMillis();
    }
    try {
      String responseURI = adminModuleProcessor.process(request, response);
      if (responseURI != null && !response.isCommitted()) {
        if (responseURI.endsWith(".jsp")) {
          request.getRequestDispatcher(responseURI).forward(request, response);
        } else {
          response.sendRedirect(responseURI);
        }
      }
    } catch (Exception e) {
      log.warning("Unhandled exception:" + e);
    }
    if (log.isLoggable(Level.INFO)) {
      long timeUsed = System.currentTimeMillis() - timeStart;
      log.info("GAELuceneAdminServlet - no." + processCount + ", " + timeUsed + " ms used.");
    }
  }

  @Override
  public void destroy() {
    super.destroy();
    log.info("<---- " + GAELuceneAdminServlet.class.getName() + " has been destroied. ---->");
  }

}