package org.springframework.jdbc.datasource;

public final class DynamicDataSourceContextHolder {

  private static final ThreadLocal<String> LOOKUP_KEY_HOLDER = new ThreadLocal<>();

  public static String getDataSourceLookupKey() {
    return LOOKUP_KEY_HOLDER.get();
  }

  public static void setDataSourceLookupKey(String dataSourceLookupKey) {
    LOOKUP_KEY_HOLDER.set(dataSourceLookupKey);
  }

  public static void clearDataSourceLookupKey() {
    LOOKUP_KEY_HOLDER.remove();
  }

}
