package com.ryvk.taskflow;

public class Task {
    public static final int LOW_PRIORITY = 1;
    public static final int MEDIUM_PRIORITY = 2;
    public static final int HIGH_PRIORITY = 3;
    public static final int ONGOING = 0;
    public static final int COMPLETED = 1;
    private String id;
    private String title;
    private String date;
    private String description;
    private int priority;
    private boolean completed;
    private String user_email;

    public Task(){

    }
    public Task(String title, String date, String description, int priority,String email) {
        this.title = title;
        this.date = date;
        this.description = description;
        this.priority = priority;
        this.completed = false;
        this.user_email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }
}
