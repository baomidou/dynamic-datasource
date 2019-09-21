package com.baomidou.samples.dynamicload.controller;

import com.baomidou.dynamic.datasource.DynamicDataSourceCreator;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/datasources")
public class LoadController {

  @Autowired
  private DataSource dataSource;
  @Autowired
  private DynamicDataSourceCreator dataSourceCreator;

  @GetMapping
  public Set<String> now() {
    DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
    return ds.getCurrentDataSources().keySet();
  }

  @PostMapping
  public Set<String> add(@RequestBody DataSourceProperty dataSourceProperty) {
    DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
    DataSource dataSource = dataSourceCreator.createDataSource(dataSourceProperty);
    ds.addDataSource(dataSourceProperty.getPollName(), dataSource);
    return ds.getCurrentDataSources().keySet();
  }

  @DeleteMapping
  public void remove(String name) {
    DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
    ds.removeDataSource(name);
  }
}
