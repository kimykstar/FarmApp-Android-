package com.example.farm;

import android.app.Application;

public class Session extends Application {
    private String id;

    public Session(){
        this.id = "default";
    }

    public void setSessionId(String id){
        this.id = id;
    }

    public String getSessionId(){
        return id;
    }
}
