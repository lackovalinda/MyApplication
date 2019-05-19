package com.example.draftapplication;

import android.app.Application;

public class Globals extends Application {
    private boolean isChecked;
    private boolean permissionsAccepted;

    public boolean getSettings(){
        return this.isChecked;
    }

    public void setSettings(boolean option){
        this.isChecked = option;
    }

    public boolean getPermissionsAccepted(){
        return permissionsAccepted;
    }

    public void setPermissionsAccepted(boolean permissions){
        this.permissionsAccepted = permissions;
    }
}
