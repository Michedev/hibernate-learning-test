package edu.mikedev.hibernate_learning_test;

import org.hibernate.Session;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

public class HibernateDBUtils {
    private final Session session;

    public HibernateDBUtils(Session session) {
        this.session = session;
    }

    public void initDB() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/root";
        Properties props = new Properties();
        props.setProperty("user", "root");
        props.setProperty("password", "root");

        Connection conn = DriverManager.getConnection(url, props);
        Statement statement = conn.createStatement();
        statement.execute("DELETE FROM tasks;");
        statement.execute("DELETE FROM users;");
        statement.execute("COPY Users FROM '/db/fake-data/sample_user.csv' DELIMITER ',' CSV HEADER;");
        statement.execute("COPY Tasks FROM '/db/fake-data/sample_task.csv' DELIMITER ',' CSV HEADER;");
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
}