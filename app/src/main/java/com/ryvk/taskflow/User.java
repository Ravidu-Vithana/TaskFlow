package com.ryvk.taskflow;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class User {
    private String id;
    private String name;
    private String mobile;
    private String email;
    private String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public static User getSPUser(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Utils.PREFS_NAME, Context.MODE_PRIVATE);
        String userJSON = sharedPreferences.getString("user",null);
        Gson gson = new Gson();
        return gson.fromJson(userJSON, User.class);
    }

    public void updateSPUser (Context context,User user){
        Gson gson = new Gson();
        String userJSON = gson.toJson(user);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Utils.PREFS_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user", userJSON);
        editor.apply();
        editor.commit();
    }

    public void removeSPUser(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Utils.PREFS_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("user");
        editor.apply();
    }
}
