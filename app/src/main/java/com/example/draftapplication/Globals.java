package com.example.draftapplication;

import android.app.Application;

public class Globals extends Application {
    private boolean isChecked;
    private String userId;

    public boolean getSettings(){
        return this.isChecked;
    }

    public void setSettings(boolean option){
        this.isChecked = option;
    }

    public void setUserId(String userId){
        this.userId  = userId;
    }

    public String getUserId(){
        return userId;
    }
}
