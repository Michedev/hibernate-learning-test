package edu.mikedev.hibernate_learning_test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
	private SessionFactory sessionFactory;

	@Before
	public void setUp() throws Exception {
		Path testResourceDirectory = Paths.get("src","main","resources");
		File hibernateConfigFile = new File(testResourceDirectory.resolve("hibernate.cfg.xml").toAbsolutePath().toString());

		Configuration cfg = new Configuration();
		sessionFactory = cfg.configure(hibernateConfigFile).buildSessionFactory();

		session = sessionFactory.openSession();
		session.beginTransaction();
		hibernateDBUtils = new HibernateDBUtils(session);
		hibernateDBUtils.initDB();
	}

	@Test
	public void testPullTaskAndTitlesFromDB() {
		List<String> taskTitles = hibernateDBUtils.pullTaskTitles();
		List<Task> tasks = hibernateDBUtils.pullTasks();
		Assert.assertEquals(6, tasks.size());
		Assert.assertEquals(6, taskTitles.size());
	}
	
	@Test
	public void testPullNotExistentTask() {
		List<Task> resultQuery = session.createQuery("SELECT a FROM Task a where a.id = 1000", Task.class).getResultList();
		Assert.assertEquals(0, resultQuery.size());
	}


	@Test
	public void testInsertNewTaskInDB(){
		String newTaskTitle = "new generated task 123";
		String newTaskDescription = "description 1";
		Task newTask = new Task(newTaskTitle, newTaskDescription, new Date(88844328L), false);

		session.save(newTask);

		List<Task> pulledTasks = hibernateDBUtils.pullTasks();
		Assert.assertEquals(7, pulledTasks.size());

		Task newPulledTask = pulledTasks.get(6);

		Assert.assertEquals(6, newPulledTask.getId());
		Assert.assertEquals(newTaskTitle, newPulledTask.getTitle());
		Assert.assertEquals(newTaskDescription, newPulledTask.getDescription());

		Assert.assertEquals(6, hibernateDBUtils.getDBTaskTitles().size());

		commitAndReinitSession();

		List<String> dbTaskTitlesAfterReInit = hibernateDBUtils.getDBTaskTitles();

		Assert.assertEquals(7, dbTaskTitlesAfterReInit.size());

	}

	private void commitAndReinitSession() {
		session.getTransaction().commit();
		session = sessionFactory.openSession();
		session.beginTransaction();
		hibernateDBUtils.setSession(session);
	}

	@Test
	public void testRemoveTaskByID(){
		String titleTaskRemoved = "Sample task title 2";
		session.createQuery("delete from Task where id = 3").executeUpdate();

		List<String> taskTitles = hibernateDBUtils.pullTaskTitles();
		Assert.assertEquals(5, taskTitles.size());
		Assert.assertFalse(taskTitles.contains(titleTaskRemoved));

		List<String> dbTaskTitlesPreCommit = hibernateDBUtils.getDBTaskTitles();
		Assert.assertEquals(6, dbTaskTitlesPreCommit.size());
		Assert.assertTrue(dbTaskTitlesPreCommit.contains(titleTaskRemoved));

		commitAndReinitSession();

		List<String> dbTaskTitlesPostCommit = hibernateDBUtils.getDBTaskTitles();
		Assert.assertEquals(5, dbTaskTitlesPostCommit.size());
		Assert.assertFalse(dbTaskTitlesPostCommit.contains(titleTaskRemoved));
	}

	@Test
	public void testRemoveTaskThroughObject(){
		List<Task> tasks = hibernateDBUtils.pullTasks();
		Task firstTask = tasks.get(0);
		session.remove(firstTask);

		List<Task> tasksAfterRemove = hibernateDBUtils.pullTasks();
		Assert.assertEquals(6, tasks.size());
		Assert.assertEquals(5, tasksAfterRemove.size());

		Assert.assertEquals(6, hibernateDBUtils.getDBTaskTitles().size());

		commitAndReinitSession();

		Assert.assertEquals(5, hibernateDBUtils.getDBTaskTitles().size());
	}

	@Test
	public void testUpdateTask(){
		List<Task> tasks = hibernateDBUtils.pullTasks();
		Task first = tasks.get(0);

		String oldTitle = first.getTitle();
		String newTitle = "Updated title 1";
		String newDescription = "Updated description 1";

		Assert.assertNotEquals(newTitle, oldTitle);
		Assert.assertNotEquals(newDescription, first.getDescription());

		first.setTitle(newTitle);
		first.setDescription(newDescription);

		List<Task> updatedTasks = hibernateDBUtils.pullTasks();
		Task firstUpdated = updatedTasks.get(updatedTasks.size()-1);

		Assert.assertEquals(newTitle, firstUpdated.getTitle());
		Assert.assertEquals(newDescription, firstUpdated.getDescription());
		Assert.assertEquals(0, firstUpdated.getId());


		List<String> dbTaskTitles = hibernateDBUtils.getDBTaskTitles();
		Assert.assertTrue(dbTaskTitles.contains(oldTitle));
		Assert.assertFalse(dbTaskTitles.contains(newTitle));

		commitAndReinitSession();

		List<String> dbTaskTitlesAfterCommit = hibernateDBUtils.getDBTaskTitles();
		Assert.assertTrue(dbTaskTitlesAfterCommit.contains(newTitle));
		Assert.assertFalse(dbTaskTitlesAfterCommit.contains(oldTitle));
	}

	@After
	public void closeSession(){
		session.close();
	}

}
