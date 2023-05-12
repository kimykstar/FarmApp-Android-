package com.example.farm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    ImageButton login;
    ImageButton camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = findViewById(R.id.login);
        camera = findViewById(R.id.camera);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }


    public static class MyActivity extends AsyncTask<String, Void, String>{
        String message;
        @Override
        protected String doInBackground(String... strings){
            try{
                HttpConnection conn = new HttpConnection("http://10.0.2.2:8080/hello");
                conn.setHeader(1000, "GET", true, true);
                conn.writeData("hello I'm Client");
                message = conn.readData();
                Log.i("message", message);
            }catch(Exception e){
                e.printStackTrace();
            }
            return message;
        }
    }
}