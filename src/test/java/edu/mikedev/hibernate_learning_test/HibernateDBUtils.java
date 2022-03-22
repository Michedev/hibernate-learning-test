package edu.mikedev.hibernate_learning_test;

import org.hibernate.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HibernateDBUtils {
    private Session session;

    public HibernateDBUtils(Session session) {
        this.session = session;
    }

    public void initDB() throws SQLException {
        Connection conn = initDBConnection();
        Statement statement = conn.createStatement();
        statement.execute("DELETE FROM tasks;");
        statement.execute("DELETE FROM users;");
        statement.execute("COPY Users FROM '/db/fake-data/sample_user.csv' DELIMITER ',' CSV HEADER;");
        statement.execute("COPY Tasks FROM '/db/fake-data/sample_task.csv' DELIMITER ',' CSV HEADER;");
        conn.close();
    }

    public Connection initDBConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/root";
        Properties props = new Properties();
        props.setProperty("user", "root");
        props.setProperty("password", "root");

        return DriverManager.getConnection(url, props);
    }

    public List<String> getDBTaskTitles(){
        List<String> taskTitles = new ArrayList<>();
        Connection connection = null;
        try {
            connection = initDBConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ResultSet titlesDBIterator = null;
        try {
            titlesDBIterator = connection.createStatement().executeQuery("Select * from Tasks");
            while(titlesDBIterator.next()){
                String title = titlesDBIterator.getString("title");
                taskTitles.add(title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return taskTitles;
    }

    public List<User> pullUsers() {
        return session.createQuery("SELECT a FROM User a", User.class).getResultList();
    }

    public List<Task> pullTasks() {
        return session.createQuery("SELECT a FROM Task a", Task.class).getResultList();
    }

    public List<String> pullTaskTitles(){
        return session.createQuery("SELECT title from Task", String.class).getResultList();
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}