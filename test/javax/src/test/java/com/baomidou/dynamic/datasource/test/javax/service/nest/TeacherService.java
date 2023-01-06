package com.baomidou.dynamic.datasource.test.javax.service.nest;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
@Service
@DS("teacher")
public class TeacherService {
    private final DataSource dataSource;

    public TeacherService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional
    public int addTeacherWithTx(String name, Integer age) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("insert into teacher (name,age) values (?,?)")) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, age);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public int addTeacherNoTx(String name, Integer age) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("insert into teacher (name,age) values (?,?)")) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, age);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Teacher> selectTeachers() {
        List<Teacher> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM student");
            while (resultSet.next()) {
                result.add(new Teacher(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3)));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}