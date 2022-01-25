package edu.mikedev.hibernate_learning_test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.After;
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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;


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

    private List<Task> pullTasks() {
        return session.createQuery("SELECT a FROM Task a", Task.class).getResultList();
    }


    @Test
    public void testPullUsers(){
        List<User> users = pullUsers();

        Assert.assertEquals(4, users.size());
        Assert.assertEquals("tizio", users.get(0).getUsername());
        Assert.assertEquals("pippo@pluto.com", users.get(1).getEmail());

    }

    @Test
    public void testPulledUserTasks(){
        List<User> users = pullUsers();

        User firstUser = users.get(0);
        Assert.assertEquals(2, firstUser.getTasks().size());
        List<String> firstUserTaskTitles = firstUser.getTasks().stream().map(Task::getTitle).sorted().collect(Collectors.toList());  // Doing sorting because set has arbitrary order
        List<String> firstUserTaskDescriptions = firstUser.getTasks().stream().map(Task::getDescription).sorted().collect(Collectors.toList());
        List<String> expectTaskTitles = Arrays.asList("Eat food", "Run a marathon");
        List<String> expectedTaskDescriptions = Arrays.asList("Eat food for 15 days", "Run a full marathon for 42 kilometers");
        Assert.assertArrayEquals(expectTaskTitles.toArray(), firstUserTaskTitles.toArray());
        Assert.assertArrayEquals(expectedTaskDescriptions.toArray(), firstUserTaskDescriptions.toArray());
    }

    @Test
    public void testDeleteUser(){
        List<User> users = pullUsers();

        User user = users.get(0);
        session.detach(user);
        session.delete(user);

        List<User> usersAfterDelete = pullUsers();

        Assert.assertEquals(4, users.size());
        Assert.assertEquals(3, usersAfterDelete.size());
    }

    @Test
    public void testDeleteUserCascadeTasks(){
        List<User> users = pullUsers();
        List<Task> tasks = pullTasks();


        User user = users.get(0);
        session.detach(user);
        session.delete(user);

        List<Task> tasksAfterDelete = pullTasks();
        List<String> taskTitles = tasksAfterDelete.stream().map(Task::getTitle).collect(Collectors.toList());

        Assert.assertEquals(6, tasks.size());
        Assert.assertEquals(4, tasksAfterDelete.size());
        Assert.assertFalse(taskTitles.contains("Eat food"));
        Assert.assertFalse(taskTitles.contains("Run a marathon"));
    }

    @Test
    public void testInsertionOfNewUser(){
        User newUser = new User("newuser1", "newpassword1", "newuser@pemail.com");
        newUser.setId(4); //must set the id to make it works

        session.persist(newUser);

        List<User> usersAfterInsert = pullUsers();
        Assert.assertEquals(5, usersAfterInsert.size());
        Assert.assertEquals("newuser1", usersAfterInsert.get(4).getUsername());

    }



    @After
    public void commitTransaction(){
        t.commit();
    }
}
