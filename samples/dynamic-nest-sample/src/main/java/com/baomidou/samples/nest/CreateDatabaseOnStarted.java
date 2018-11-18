package com.baomidou.samples.nest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class CreateDatabaseOnStarted implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private DataSource dataSource;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        try {
            Connection connection = dataSource.getConnection();
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS  teacher (\n" +
                    "  id BIGINT(20) NOT NULL AUTO_INCREMENT,\n" +
                    "  name VARCHAR(30) NULL DEFAULT NULL ,\n" +
                    "  age INT(11) NULL DEFAULT NULL ,\n" +
                    "  PRIMARY KEY (id)\n" +
                    ");");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS  student (\n" +
                    "  id BIGINT(20) NOT NULL AUTO_INCREMENT,\n" +
                    "  name VARCHAR(30) NULL DEFAULT NULL ,\n" +
                    "  age INT(11) NULL DEFAULT NULL ,\n" +
                    "  PRIMARY KEY (id)\n" +
                    ");");
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
