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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class HibernateUserLearnTest {

    private HibernateDBUtils hibernateDBUtils;
    private Session session;
    private SessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception {
        Path testResourceDirectory = Paths.get("src", "main", "resources");
        File hibernateConfigFile = new File(testResourceDirectory.resolve("hibernate.cfg.xml").toAbsolutePath().toString());


        Configuration cfg = new Configuration();
        sessionFactory = cfg.configure(hibernateConfigFile).buildSessionFactory();

        session = sessionFactory.openSession();
        session.beginTransaction();
        this.hibernateDBUtils = new HibernateDBUtils(session);
        hibernateDBUtils.initDB();
    }

    @Test
    public void testPullUsers(){
        List<User> users = hibernateDBUtils.pullUsers();

        Assert.assertEquals(4, users.size());
        Assert.assertEquals("tizio", users.get(0).getUsername());
        Assert.assertEquals("pippo@pluto.com", users.get(1).getEmail());
    }

    @Test
    public void testPulledUserTasks(){
        List<User> users = hibernateDBUtils.pullUsers();

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
        List<User> users = hibernateDBUtils.pullUsers();

        User user = users.get(0);
        int deletedUserId = user.getId();
        session.delete(user);

        List<User> usersAfterDelete = hibernateDBUtils.pullUsers();

        Assert.assertEquals(4, users.size());
        Assert.assertEquals(3, usersAfterDelete.size());
        Assert.assertFalse(usersAfterDelete.stream().map(User::getId).anyMatch(x -> x == deletedUserId));
    }

    @Test
    public void testDeleteUserCascadeTasks(){
        List<User> users = hibernateDBUtils.pullUsers();
        List<Task> tasks = hibernateDBUtils.pullTasks();


        User user = users.get(0);
        session.delete(user);

        List<Task> tasksAfterDelete = hibernateDBUtils.pullTasks();
        List<String> taskTitles = tasksAfterDelete.stream().map(Task::getTitle).collect(Collectors.toList());

        Assert.assertEquals(6, tasks.size());
        Assert.assertEquals(4, tasksAfterDelete.size());
        Assert.assertFalse(taskTitles.contains("Eat food"));
        Assert.assertFalse(taskTitles.contains("Run a marathon"));
    }

    @Test
    public void testInsertionOfNewUser(){
        User newUser = new User("newuser1", "newpassword1", "newuser@pemail.com");

        session.save(newUser);

        List<User> usersAfterInsert = hibernateDBUtils.pullUsers();
        Assert.assertEquals(5, usersAfterInsert.size());
        Assert.assertEquals("newuser1", usersAfterInsert.get(4).getUsername());
    }

    @Test
    public void testUpdateUser(){
        String newUsername = "UsernameUpdated1";

        List<User> users = hibernateDBUtils.pullUsers();
        User firstUser = users.get(0);

        Assert.assertNotEquals(newUsername, firstUser.getUsername());

        firstUser.setUsername(newUsername);

        List<User> usersAfterUpdate = hibernateDBUtils.pullUsers();

        User firstUserUpdated = usersAfterUpdate.get(usersAfterUpdate.size()-1);

        Assert.assertEquals(firstUser.getId(), firstUserUpdated.getId());
        Assert.assertEquals(newUsername, firstUserUpdated.getUsername());
    }



    @After
    public void commitTransaction(){
        session.getTransaction().commit();
    }

}
