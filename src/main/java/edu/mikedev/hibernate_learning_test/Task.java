package edu.mikedev.hibernate_learning_test;

import java.util.Date;

public class Task {
	
	private int id;
	private String title;
	private String description;
	private Date deadline;
    private boolean done;

	public Task(){

	}
       
	public Task(String title, String description, Date deadline, boolean done) {
		super();
		this.title = title;
		this.description = description;
		this.deadline = deadline;
		this.done = done;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getDeadline() {
		return deadline;
	}
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	public boolean isDone() {
		return done;
	}
	public void setDone(boolean done) {
		this.done = done;
	}
    
    
    

}
