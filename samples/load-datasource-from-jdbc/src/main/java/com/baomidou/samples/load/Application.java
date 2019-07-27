package com.baomidou.samples.load;

import com.baomidou.dynamic.datasource.provider.AbstractJdbcDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public DynamicDataSourceProvider dynamicDataSourceProvider() {
    return new AbstractJdbcDataSourceProvider("org.h2.Driver", "jdbc:h2:mem:test", "sa", "") {
      @Override
      protected Map<String, DataSourceProperty> executeStmt(Statement statement)
          throws SQLException {
        //*************** here is needn't in prod env***************
        statement.execute("CREATE TABLE IF NOT EXISTS `DB`\n"
            + "(\n"
            + "    `name`   VARCHAR(30) NULL DEFAULT NULL,\n"
            + "    `username`   VARCHAR(30) NULL DEFAULT NULL,\n"
            + "    `password`   VARCHAR(30) NULL DEFAULT NULL,\n"
            + "    `url`   VARCHAR(30) NULL DEFAULT NULL,\n"
            + "    `driver`   VARCHAR(30) NULL DEFAULT NULL\n"
            + ")");
        statement.executeUpdate(
            "insert into DB values ('master','sa','','jdbc:h2:mem:test','org.h2.Driver')");
        statement.executeUpdate(
            "insert into DB values ('slave_1','sa','','jdbc:h2:mem:test','org.h2.Driver')");
        statement.executeUpdate(
            "insert into DB values ('slave_2','sa','','jdbc:h2:mem:test','org.h2.Driver')");
        statement.executeUpdate(
            "insert into DB values ('slave_3','sa','','jdbc:h2:mem:test','org.h2.Driver')");
        Map<String, DataSourceProperty> map = new HashMap<>();
        //*************** ↑↑↑↑↑↑↑ here is needn't in prod env***************

        ResultSet rs = statement.executeQuery("select * from DB");
        while (rs.next()) {
          String name = rs.getString("name");
          String username = rs.getString("username");
          String password = rs.getString("password");
          String url = rs.getString("url");
          String driver = rs.getString("driver");
          DataSourceProperty property = new DataSourceProperty();
          property.setUsername(username);
          property.setPassword(password);
          property.setUrl(url);
          property.setDriverClassName(driver);
          map.put(name, property);
        }
        return map;
      }
    };
  }

}