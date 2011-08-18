package org.apache.gaelucene.dashboard;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletRequest;
  
public class ParamUtil {
  
      public static boolean getBoolean(ServletRequest req, String param) {
          return GetterUtil.getBoolean(req.getParameter(param));
      }
  
      public static boolean getBoolean(
          ServletRequest req, String param, boolean defaultValue) {
          return get(req, param, defaultValue);
      }
  
      public static Date getDate(
          ServletRequest req, String param, DateFormat df) {
  
          return GetterUtil.getDate(req.getParameter(param), df);
      }
  
      public static Date getDate(
          ServletRequest req, String param, DateFormat df, Date defaultValue) {
  
          return get(req, param, df, defaultValue);
      }
  
      public static double getDouble(ServletRequest req, String param) {
          return GetterUtil.getDouble(req.getParameter(param));
      }
  
      public static double getDouble(
          ServletRequest req, String param, double defaultValue) {
  
          return get(req, param, defaultValue);
      }
  
      public static float getFloat(ServletRequest req, String param) {
          return GetterUtil.getFloat(req.getParameter(param));
      }
  
      public static float getFloat(ServletRequest req, String param, float defaultValue) {
          return get(req, param, defaultValue);
      }
  
      public static int getInteger(ServletRequest req, String param) {
          return GetterUtil.getInteger(req.getParameter(param));
      }
  
      public static int getInteger(ServletRequest req, String param, int defaultValue) {
          return get(req, param, defaultValue);
      }
  
      public static List<Integer> getIntegers( ServletRequest req, String paramName ) {
          String[] intStrs = req.getParameterValues(paramName);
          if (intStrs != null && intStrs.length > 0) {
              List<Integer> ret = new ArrayList();
              for (String intStr : intStrs) {
                  Integer val = GetterUtil.getInteger(intStr);
                  if (val != null) ret.add(val);
              }
              return ret;
          }
          return new ArrayList<Integer>();
      }
      
      public static long getLong(ServletRequest req, String param) {
          return GetterUtil.getLong(req.getParameter(param));
      }
  
     public static long getLong(
         ServletRequest req, String param, long defaultValue) {
 
         return get(req, param, defaultValue);
     }
 
     public static short getShort(ServletRequest req, String param) {
         return GetterUtil.getShort(req.getParameter(param));
     }
 
     public static short getShort(
         ServletRequest req, String param, short defaultValue) {
 
         return get(req, param, defaultValue);
     }
 
     public static String getString(ServletRequest req, String param) {
         return GetterUtil.getString(req.getParameter(param));
     }
 
     public static String getString(
         ServletRequest req, String param, String defaultValue) {
 
         return get(req, param, defaultValue);
     }
 
     public static boolean get(
         ServletRequest req, String param, boolean defaultValue) {
 
         return GetterUtil.get(req.getParameter(param), defaultValue);
     }
 
     public static Date get(
         ServletRequest req, String param, DateFormat df, Date defaultValue) {
 
         return GetterUtil.get(req.getParameter(param), df, defaultValue);
     }
 
     public static double get(
         ServletRequest req, String param, double defaultValue) {
 
         return GetterUtil.get(req.getParameter(param), defaultValue);
     }
 
     public static float get(
         ServletRequest req, String param, float defaultValue) {
 
         return GetterUtil.get(req.getParameter(param), defaultValue);
     }
 
     public static int get(ServletRequest req, String param, int defaultValue) {
         return GetterUtil.get(req.getParameter(param), defaultValue);
     }
 
     public static long get(
         ServletRequest req, String param, long defaultValue) {
 
         return GetterUtil.get(req.getParameter(param), defaultValue);
     }
 
     public static short get( ServletRequest req, String param, short defaultValue) {
         return GetterUtil.get(req.getParameter(param), defaultValue);
     }
 
     public static String get( ServletRequest req, String param, String defaultValue) {
         String returnValue = GetterUtil.get(req.getParameter(param), defaultValue);
         if (returnValue != null) {
             return returnValue.trim();
         }
         return null;
     }
 
     public static void print(ServletRequest req) {
         Enumeration e = req.getParameterNames();
         while (e.hasMoreElements()) {
             String param = (String)e.nextElement(); 
             String[] values = req.getParameterValues(param);
             for (int i = 0; i < values.length; i++) {
                 System.out.println(param + "[" + i + "] = " + values[i]);
             }
         }
     }
 
}

class GetterUtil {
  
      public static final boolean DEFAULT_BOOLEAN = false;
  
      public static final double DEFAULT_DOUBLE = 0.0;
  
      public static final float DEFAULT_FLOAT = 0;
  
      public static final int DEFAULT_INTEGER = 0;
  
      public static final long DEFAULT_LONG = 0;
  
      public static final short DEFAULT_SHORT = 0;
  
      public static final String DEFAULT_STRING = "";
  
      public static String[] BOOLEANS = {"true", "t", "y", "on", "1"};
  
      public static boolean getBoolean(String value) {
          return getBoolean(value, DEFAULT_BOOLEAN);
      }
  
      public static boolean getBoolean(String value, boolean defaultValue) {
          return get(value, defaultValue);
      }
  
      public static Date getDate(String value, DateFormat df) {
          return getDate(value, df, new Date());
      }
  
      public static Date getDate(String value, DateFormat df, Date defaultValue) {
          return get(value, df, defaultValue);
      }
  
      public static double getDouble(String value) {
          return getDouble(value, DEFAULT_DOUBLE);
      }
  
      public static double getDouble(String value, double defaultValue) {
          return get(value, defaultValue);
      }
  
      public static float getFloat(String value) {
          return getFloat(value, DEFAULT_FLOAT);
      }
  
      public static float getFloat(String value, float defaultValue) {
          return get(value, defaultValue);
      }
  
      public static int getInteger(String value) {
          return getInteger(value, DEFAULT_INTEGER);
      }
  
      public static int getInteger(String value, int defaultValue) {
          return get(value, defaultValue);
      }
  
      public static long getLong(String value) {
          return getLong(value, DEFAULT_LONG);
      }
  
      public static long getLong(String value, long defaultValue) {
          return get(value, defaultValue);
      }
 
     public static short getShort(String value) {
         return getShort(value, DEFAULT_SHORT);
     }
 
     public static short getShort(String value, short defaultValue) {
         return get(value, defaultValue);
     }
 
     public static String getString(String value) {
         return getString(value, DEFAULT_STRING);
     }
 
     public static String getString(String value, String defaultValue) {
         return get(value, defaultValue);
     }
 
     public static boolean get(String value, boolean defaultValue) {
         if (value != null) {
             try {
                 value = value.trim();
 
                 if (value.equalsIgnoreCase(BOOLEANS[0]) ||
                     value.equalsIgnoreCase(BOOLEANS[1]) ||
                     value.equalsIgnoreCase(BOOLEANS[2]) ||
                     value.equalsIgnoreCase(BOOLEANS[3]) ||
                     value.equalsIgnoreCase(BOOLEANS[4])) {
 
                     return true;
                 }
                 else {
                     return false;
                 }
             }
             catch (Exception e) {
             }
         }
 
         return defaultValue;
     }
 
     public static Date get(String value, DateFormat df, Date defaultValue) {
         try {
             Date date = df.parse(value.trim());
 
             if (date != null) {
                 return date;
             }
         }
         catch (Exception e) {
         }
 
         return defaultValue;
     }
 
     public static double get(String value, double defaultValue) {
         try {
             return Double.parseDouble(_trim(value));
         }
         catch (Exception e) {
         }
 
         return defaultValue;
     }
 
     public static float get(String value, float defaultValue) {
         try {
             return Float.parseFloat(_trim(value));
         }
         catch (Exception e) {
         }
 
         return defaultValue;
     }
 
     public static int get(String value, int defaultValue) {
         try { return Integer.parseInt(_trim(value)); }
         catch (Exception e) { }
         return defaultValue;
     }
 
     public static long get(String value, long defaultValue) {
         try {
             return Long.parseLong(_trim(value));
         }
         catch (Exception e) {
         }
 
         return defaultValue;
     }
 
     public static short get(String value, short defaultValue) {
         try {
             return Short.parseShort(_trim(value));
         }
         catch (Exception e) {
         }
 
         return defaultValue;
     }
 
     public static String replace(String s, char oldSub, char newSub) {
         return replace(s, oldSub, new Character(newSub).toString());
     }
 
     public static String replace(String s, char oldSub, String newSub) {
         if ((s == null) || (newSub == null)) {
             return null;
         }
 
         char[] c = s.toCharArray();
 
         StringBuffer sb = new StringBuffer();
 
         for (int i = 0; i < c.length; i++) {
             if (c[i] == oldSub) {
                 sb.append(newSub);
             }
             else {
                 sb.append(c[i]);
             }
         }
 
         return sb.toString();
     }
 
     public static String replace(String s, String oldSub, String newSub) {
         if ((s == null) || (oldSub == null) || (newSub == null)) {
             return null;
         }
 
         int y = s.indexOf(oldSub);
 
         if (y >= 0) {
             StringBuffer sb = new StringBuffer();
 
             int length = oldSub.length();
             int x = 0;
 
             while (x <= y) {
                 sb.append(s.substring(x, y));
                 sb.append(newSub);
                 x = y + length;
                 y = s.indexOf(oldSub, x);
             }
 
             sb.append(s.substring(x));
 
             return sb.toString();
         }
         else {
             return s;
         }
     }
 
     public static String replace(String s, String[] oldSubs, String[] newSubs) {
         if ((s == null) || (oldSubs == null) || (newSubs == null)) {
             return null;
         }
 
         if (oldSubs.length != newSubs.length) {
             return s;
         }
 
         for (int i = 0; i < oldSubs.length; i++) {
             s = replace(s, oldSubs[i], newSubs[i]);
         }
 
         return s;
     }

     public static String get(String value, String defaultValue) {
         if (value != null) {
             value = value.trim();
             value = replace(value, "\r\n", "\n");
             return value;
         }
         return defaultValue;
     }
 
     private static String _trim(String value) {
         if (value != null) {
             value = value.trim();
 
             StringBuffer sb = new StringBuffer();
 
             char[] charArray = value.toCharArray();
 
             for (int i = 0; i < charArray.length; i++) {
                 if ((Character.isDigit(charArray[i])) ||
                     (charArray[i] == '-' && i == 0) ||
                     (charArray[i] == '.')) {
 
                     sb.append(charArray[i]);
                 }
             }
 
             value = sb.toString();
         }
 
         return value;
     }
 
}

