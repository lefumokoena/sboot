package model;

import java.util.Date;

public class Task {

	private Integer id;
	private String title;
	private Date due_date;
	private Double estimated_hours;
	private Integer project;
	private Project project_data;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getDue_date() {
		return due_date;
	}
	public void setDue_date(Date due_date) {
		this.due_date = due_date;
	}
	public Double getEstimated_hours() {
		return estimated_hours;
	}
	public void setEstimated_hours(Double estimated_hours) {
		this.estimated_hours = estimated_hours;
	}
	public Integer getProject() {
		return project;
	}
	public void setProject(Integer project) {
		this.project = project;
	}
	public Project getProject_data() {
		return project_data;
	}
	public void setProject_data(Project project_data) {
		this.project_data = project_data;
	}
	
	
	
	
}
