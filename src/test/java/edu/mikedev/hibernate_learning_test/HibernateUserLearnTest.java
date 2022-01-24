package edu.mikedev.hibernate_learning_test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;


public class HibernateUserLearnTest {

    Path testResourceDirectory;
    File hibernateConfigFile;
    Session session;
    Transaction t;

    @Before
    public void setUp() throws Exception {
        this.testResourceDirectory = Paths.get("src","main","resources");
        this.hibernateConfigFile = new File(testResourceDirectory.resolve("hibernate.cfg.xml").toAbsolutePath().toString());
        System.out.print("Path: ");

        rebuildDB();

        Configuration cfg = new Configuration();
        SessionFactory factory = cfg.configure(this.hibernateConfigFile).buildSessionFactory();

        session = factory.openSession();
        t = session.beginTransaction();
    }

    private void rebuildDB() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/root";
        Properties props = new Properties();
        props.setProperty("user","root");
        props.setProperty("password","root");

        Connection conn = DriverManager.getConnection(url, props);
        Statement statement = conn.createStatement();
        statement.execute("DELETE FROM tasks;");
        statement.execute("DELETE FROM users;");
        statement.execute("COPY Users FROM '/db/fake-data/sample_user.csv' DELIMITER ',' CSV HEADER;");
        statement.execute("COPY Tasks FROM '/db/fake-data/sample_task.csv' DELIMITER ',' CSV HEADER;");
    }

    private List<User> pullUsers(){
        return session.createQuery("SELECT a FROM User a", User.class).getResultList();
    }


    @Test
    public void testPullUsers(){
        List<User> users = pullUsers();

        Assert.assertEquals(4, users.size());
        Assert.assertEquals("tizio", users.get(0).getUsername());
        Assert.assertEquals("pippo@pluto.com", users.get(1).getEmail());

    }
}
