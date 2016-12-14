package net.snet.crm.service.utils;

public class Utils {

  public static String replaceChars(String str, String searchChars, String replaceChars) {
    int replaceCharsLength = replaceChars.length();
    int strLength = str.length();
    StringBuilder buf = new StringBuilder(strLength);
    for (int i = 0; i < strLength; i++) {
      char ch = str.charAt(i);
      int index = searchChars.indexOf(ch);
      if (index >= 0) {
        if (index < replaceCharsLength) {
          buf.append(replaceChars.charAt(index));
        }
      } else {
        buf.append(ch);
      }
    }
    return buf.toString();
  }
}
