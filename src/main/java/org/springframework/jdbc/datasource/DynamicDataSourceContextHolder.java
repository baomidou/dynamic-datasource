package org.springframework.jdbc.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DynamicDataSourceContextHolder {

  private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

  private static final List<String> DATA_SOURCE_IDS = new ArrayList<>();

  public static void addDataSourceId(String... ids) {
    DATA_SOURCE_IDS.addAll(Arrays.asList(ids));
  }

  public static String getDataSource() {
    return CONTEXT_HOLDER.get();
  }

  public static void setDataSource(String dataSourceType) {
    CONTEXT_HOLDER.set(dataSourceType);
  }

  public static void clearDataSource() {
    CONTEXT_HOLDER.remove();
  }

  public static boolean containDataSource(String key) {
    return DATA_SOURCE_IDS.contains(key);
  }

}