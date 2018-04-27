package org.springframework.jdbc.datasource;

import java.util.Map;
import javax.sql.DataSource;

public interface DynamicDataSourceProvider {

 DataSource loadMasterDataSource();

  Map<String,DataSource> loadSlaveDataSource();

}
