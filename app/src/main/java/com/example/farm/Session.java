package com.example.farm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class Session extends AppCompatActivity {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public Session(Context context){
        pref = context.getSharedPreferences("userSession", MODE_PRIVATE);
        editor = pref.edit();
    }


    // setting Session parameter request user id
    public void setSession(String id){
        editor.putString("user", id);
        editor.apply();
        Log.d("Session : ", "설정이 완료되었습니다.");
    }

    // Delete LoginSession
    public void deleteSession(String id){
        editor.clear();
    }

    // get Login Session
    public String getSession(){
        return pref.getString("userSession", null);
    }
}

