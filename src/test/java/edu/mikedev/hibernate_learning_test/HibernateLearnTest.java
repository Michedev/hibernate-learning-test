package edu.mikedev.hibernate_learning_test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HibernateLearnTest {
	
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

	@Test
	public void testPullTaskAndTitlesFromDB() {

		List<String> taskTitles = pullTaskTitles(session);
		List<Task> tasks = pullTasks(session);
		System.out.println(taskTitles);
		Assert.assertEquals(6, tasks.size());
		Assert.assertEquals(6, taskTitles.size());
        t.commit();
	}

	private List<String> pullTaskTitles(Session session) {
		Query<String> query = session.createQuery("select title from Task");
		return query.list();
	}

	private List<Task> pullTasks(Session session) {
		return session.createQuery("SELECT a FROM Task a", Task.class).getResultList();
	}



	@Test
	public void testInsertNewTaskInDB(){
		Task newTask = new Task("new generated task 123", "description 1", new Date(88844328L), false);
		newTask.setId(7);
		session.persist(newTask);

		java.util.List<String> taskTitles = this.pullTaskTitles(session);
		System.out.println(taskTitles);
		Assert.assertEquals(7, taskTitles.size());
		Assert.assertTrue(taskTitles.contains("new generated task 123"));
	}

	@Test
	public void testRemoveTaskByID(){
		session.createQuery("delete from Task where id = 3").executeUpdate();
		t.commit();
		List<String> tasks = this.pullTaskTitles(session);
		Assert.assertEquals(5, tasks.size());
	}

	@Test
	public void testRemoveTaskThroughObject(){
		List<Task> tasks = pullTasks(session);
		Task firstTask = tasks.get(0);
		session.remove(firstTask);

		List<Task> tasksAfterRemove = pullTasks(session);
		Assert.assertEquals(6, tasks.size());
		Assert.assertEquals(5, tasksAfterRemove.size());
	}

	@Test
	public void testUpdateTask(){
		List<Task> tasks = pullTasks(session);
		Task first = tasks.get(0);

		String newTitle = "Updated title 1";
		String newDescription = "Updated description 1";

		Assert.assertNotEquals(newTitle, first.getTitle());
		Assert.assertNotEquals(newDescription, first.getDescription());

		first.setTitle(newTitle);
		first.setDescription(newDescription);
		session.evict(first);
		session.update(first);
		t.commit();

		List<Task> updatedTasks = pullTasks(session);
		Task firstUpdated = updatedTasks.get(0);

		Assert.assertEquals(newTitle, firstUpdated.getTitle());
		Assert.assertEquals(newDescription, firstUpdated.getDescription());
	}

	@After
	public void closeSession(){
		session.close();
	}

}
