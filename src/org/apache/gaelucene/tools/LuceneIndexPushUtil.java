package org.apache.gaelucene.tools;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.GAEFile;

public class LuceneIndexPushUtil {

  private static final String GAE_APP_URL = "http://localhost:8080/gaelucenedashboard";

  private static boolean commitFile(File file, String category, long version, int fi) throws IOException {
    String fileName = file.getName();
    long fileLength = file.length();
    long lastModified = file.lastModified();
    System.out.println("file[" + fi + "].fileName:" + fileName);
    System.out.println("file[" + fi + "].length:" + fileLength);
    System.out.println("file[" + fi + "].lastModified:" + (dateFormat.format(new Date(lastModified))));
    int segmentCount = (int) (fileLength + 1) / GAEFile.SEGMENT_LENGTH + 1;
    long fileId = -1;
    {
      StringBuffer requestUrl = new StringBuffer(128);
      requestUrl.append(gaeAppURL);
      requestUrl.append("/registernewindexfile?");
      requestUrl.append("&cat=").append(category);
      requestUrl.append("&ver=").append(version);
      requestUrl.append("&name=").append(fileName);
      requestUrl.append("&length=").append(fileLength);
      requestUrl.append("&lastModified=").append(lastModified);
      requestUrl.append("&segmentCount=").append(segmentCount);
      URL url = new URL(requestUrl.toString());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("Cookie", authCookie);
      conn.setDoOutput(true);
      conn.connect();
      OutputStream os = conn.getOutputStream();
      os.write(0);
      os.flush();
      os.close();
      System.out.println("registering '" + fileName + "' ....");
      BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] bytes = new byte[1024];
      int length = 0;
      for (int i = in.read(bytes); i != -1; i = in.read(bytes)) {
        out.write(bytes, 0, i);
        length += i;
      }
      in.close();
      in = null;
      conn.disconnect();
      conn = null;
      byte[] ostream = out.toByteArray();
      String content = new String(ostream, "UTF-8");
      System.out.println("response:" + content);
      fileId = Long.parseLong(content);
      System.out.println("'" + fileName + "' registered, fileId:" + fileId + "|");
    }
    System.out.print("uploading '" + fileName + "' ....");
    FileInputStream fis = new FileInputStream(file);
    long bytesUnread = fileLength;
    for (int i = 0; i < segmentCount; i++) {
      System.out.print("\nuploading '" + fileName + "[" + i + "]' ....");
      StringBuffer requestUrl = new StringBuffer(128);
      requestUrl.append(gaeAppURL);
      requestUrl.append("/commitnewindexfile?");
      requestUrl.append("&fileId=").append(fileId);
      requestUrl.append("&segmentNo=").append(i);
      requestUrl.append("&segmentLength=").append(Math.min(bytesUnread, GAEFile.SEGMENT_LENGTH));
      URL url = new URL(requestUrl.toString());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("Cookie", authCookie);
      conn.setDoOutput(true);
      conn.connect();
      OutputStream os = conn.getOutputStream();
      byte[] buffer = new byte[GAEFile.SEGMENT_LENGTH];
      int bytesNeedRead = GAEFile.SEGMENT_LENGTH;
      while (bytesNeedRead > 0) {
        int bytes = fis.read(buffer, 0, bytesNeedRead);
        if (bytes < 0) {
          break;
        }
        System.out.print(".");
        os.write(buffer, 0, bytes);
        bytesNeedRead -= bytes;
      }
      os.flush();
      os.close();
      BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] bytes = new byte[1024];
      int length = 0;
      for (int c = in.read(bytes); c != -1; c = in.read(bytes)) {
        out.write(bytes, 0, c);
        length += c;
      }
      in.close();
      in = null;
      conn.disconnect();
      conn = null;
      byte[] ostream = out.toByteArray();
      String content = new String(ostream, "UTF-8");
      System.out.println("\nresponse:" + content);
    }
    fis.close();
    System.out.println("fine! over!\n\n");
    return true;
  }

  private static boolean activateIndex(String category, long version) throws IOException {
    StringBuffer requestUrl = new StringBuffer(128);
    requestUrl.append(gaeAppURL);
    requestUrl.append("/activatenewindex?");
    requestUrl.append("&cat=").append(category);
    requestUrl.append("&ver=").append(version);
    URL url = new URL(requestUrl.toString());
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestProperty("Cookie", authCookie);
    conn.setDoOutput(true);
    conn.connect();
    System.out.print("activating....");
    OutputStream os = conn.getOutputStream();
    byte[] buffer = new byte[] { 0 };
    os.write(buffer, 0, buffer.length);
    os.flush();
    os.close();
    BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] bytes = new byte[1024];
    int length = 0;
    for (int i = in.read(bytes); i != -1; i = in.read(bytes)) {
      out.write(bytes, 0, i);
      length += i;
    }
    in.close();
    in = null;
    byte[] ostream = out.toByteArray();
    String content = new String(ostream, "UTF-8");
    System.out.println("fine! over!");
    System.out.println("response:" + content);
    return true;
  }

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private static final String USAGE = "java "
      + LuceneIndexPushUtil.class.getName()
      + " -app-url ${gaeAppURL} -auth-cookie ${authCookie} -cat ${index-category} -src ${path-to-index-folder} -rec-file ${path-to-rec-file}";

  static String gaeAppURL = GAE_APP_URL;

  static String authCookie = null;

  static String sourceDirName = null;

  static String category = null;

  static String jobRecFileName = null;

  public static void main(String[] args) throws IOException {
    for (int i = 0; i < args.length; i++) {
      if ("-app-url".equals(args[i])) {
        gaeAppURL = args[++i];
      } else if ("-auth-cookie".equals(args[i])) {
        authCookie = args[++i];
      } else if ("-src".equals(args[i])) {
        sourceDirName = args[++i];
      } else if ("-cat".equals(args[i])) {
        category = args[++i];
      } else if ("-rec-file".equals(args[i])) {
        jobRecFileName = args[++i];
      }
    }
    if (gaeAppURL == null || authCookie == null || sourceDirName == null || category == null || jobRecFileName == null) {
      System.err.println(USAGE);
      System.exit(-1);
    }
    File sourceDir = new File(sourceDirName);
    if (!sourceDir.exists()) {
      System.err.println("'" + sourceDir.getAbsolutePath() + "' DOES NOT EXIST!");
      System.exit(-1);
    }
    sourceDirName = sourceDir.getAbsolutePath();
    HashSet<String> uploadedRec = new HashSet<String>();
    File jobRecFile = new File(jobRecFileName);
    if (jobRecFile.exists()) {
      LineNumberReader reader = new LineNumberReader(new FileReader(jobRecFile));
      for (String line = reader.readLine(); line != null;) {
        if (line.indexOf(" OK") > -1) {
          line = line.substring(0, line.indexOf(" ")).trim();
        }
        uploadedRec.add(line);
        line = reader.readLine();
      }
      reader.close();
    }
    System.out.println("[INFO ] - trying to open index under " + sourceDirName);
    IndexReader indexReader = IndexReader.open(sourceDir);
    int maxDoc = indexReader.maxDoc();
    int numDocs = indexReader.numDocs();
    long version = indexReader.getVersion();
    boolean hasDeletions = indexReader.hasDeletions();
    boolean isOptimized = indexReader.isOptimized();
    System.out.println("maxDoc:" + maxDoc);
    System.out.println("numDocs:" + numDocs);
    System.out.println("version:" + version);
    System.out.println("hasDeletions:" + hasDeletions);
    System.out.println("isOptimized:" + isOptimized);
    BufferedWriter dataWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jobRecFile, true)));
    System.out.println("[INFO ] - trying to synchronize the index files onto gae...");
    File[] files = sourceDir.listFiles();
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      if (uploadedRec.contains(file.getName())) {
        System.out.println("[INFO ] - skip file '" + file.getName() + "'");
        continue;
      }
      try {
        commitFile(file, category, version, i);
        dataWriter.write(file.getName() + " OK\n");
      } catch (IOException ioe) {
        System.out.println("[WARN ] - failed to upload '" + file.getName() + "', because:" + ioe);
      }
    }
    dataWriter.flush();
    dataWriter.close();
    System.out.println("[INFO ] - trying to activate the index...");
    try {
      activateIndex(category, version);
    } catch (IOException ioe) {
      System.out.println("[WARN ] - failed to activate the index, because:" + ioe);
    }
  }

}