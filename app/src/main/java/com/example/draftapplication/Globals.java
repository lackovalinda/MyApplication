package com.example.draftapplication;

import android.app.Application;

public class Globals extends Application {
    private boolean isChecked = true;
    private boolean permissionsAccepted = false;

    public boolean getData(){
        return this.isChecked;
    }

    public void setData(boolean option){
        this.isChecked = option;
    }

    public boolean getPermissionsAccepted(){
        return permissionsAccepted;
    }

    public void setPermissionsAccepted(boolean permissions){
        this.permissionsAccepted = permissions;
    }
}
