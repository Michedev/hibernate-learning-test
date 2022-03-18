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
import java.util.Date;
import java.util.List;

public class HibernateTaskLearnTest {
	
	HibernateDBUtils hibernateDBUtils;
	Session session;
	Transaction t;

	@Before
	public void setUp() throws Exception {
		Path testResourceDirectory = Paths.get("src","main","resources");
		File hibernateConfigFile = new File(testResourceDirectory.resolve("hibernate.cfg.xml").toAbsolutePath().toString());

		Configuration cfg = new Configuration();
		SessionFactory factory = cfg.configure(hibernateConfigFile).buildSessionFactory();

		session = factory.openSession();
		t = session.beginTransaction();
		hibernateDBUtils = new HibernateDBUtils(session);
		hibernateDBUtils.initDB();
	}

	@Test
	public void testPullTaskAndTitlesFromDB() {
		List<String> taskTitles = hibernateDBUtils.pullTaskTitles();
		List<Task> tasks = hibernateDBUtils.pullTasks();
		Assert.assertEquals(6, tasks.size());
		Assert.assertEquals(6, taskTitles.size());
        t.commit();
	}
	
	@Test
	public void testPullNotExistentTask() {
		List<Task> resultQuery = session.createQuery("SELECT a FROM Task a where a.id = 1000", Task.class).getResultList();
		Assert.assertEquals(0, resultQuery.size());
	}


	@Test
	public void testInsertNewTaskInDB(){
		Task newTask = new Task("new generated task 123", "description 1", new Date(88844328L), false);
		newTask.setId(7);
		session.persist(newTask);

		java.util.List<String> taskTitles = hibernateDBUtils.pullTaskTitles();
		Assert.assertEquals(7, taskTitles.size());
		Assert.assertTrue(taskTitles.contains("new generated task 123"));
	}

	@Test
	public void testRemoveTaskByID(){
		session.createQuery("delete from Task where id = 3").executeUpdate();
		t.commit();
		List<String> tasks = hibernateDBUtils.pullTaskTitles();
		Assert.assertEquals(5, tasks.size());
	}

	@Test
	public void testRemoveTaskThroughObject(){
		List<Task> tasks = hibernateDBUtils.pullTasks();
		Task firstTask = tasks.get(0);
		session.remove(firstTask);

		List<Task> tasksAfterRemove = hibernateDBUtils.pullTasks();
		Assert.assertEquals(6, tasks.size());
		Assert.assertEquals(5, tasksAfterRemove.size());
	}

	@Test
	public void testUpdateTask(){
		List<Task> tasks = hibernateDBUtils.pullTasks();
		Task first = tasks.get(0);

		String newTitle = "Updated title 1";
		String newDescription = "Updated description 1";

		Assert.assertNotEquals(newTitle, first.getTitle());
		Assert.assertNotEquals(newDescription, first.getDescription());

		session.evict(first);

		first.setTitle(newTitle);
		first.setDescription(newDescription);

		session.update(first);
		t.commit();

		List<Task> updatedTasks = hibernateDBUtils.pullTasks();
		Task firstUpdated = updatedTasks.get(updatedTasks.size()-1);

		Assert.assertEquals(newTitle, firstUpdated.getTitle());
		Assert.assertEquals(newDescription, firstUpdated.getDescription());
		Assert.assertEquals(0, firstUpdated.getId());
	}

	@After
	public void closeSession(){
		session.close();
	}

}
