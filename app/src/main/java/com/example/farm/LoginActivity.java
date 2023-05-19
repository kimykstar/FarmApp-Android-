package com.example.farm;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    Button loginBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginBtn = findViewById(R.id.loginBtn);
        EditText idText = findViewById(R.id.id);
        EditText pwText = findViewById(R.id.pw);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginActivity activity = new loginActivity();
                String id = idText.getText().toString();
                String pw = pwText.getText().toString();
                activity.execute(id, pw);
            }
        });
    }

    public static class loginActivity extends AsyncTask<String, Void, String> {
        String result;
        @Override
        protected String doInBackground(String... arg){
            try{
                String id = arg[0];
                String pw = arg[1];
                HttpConnection conn = new HttpConnection("http://10.0.2.2:8081/login");
                conn.setHeader(1000, "POST", true, true);
                String message = String.format("%s %s", id, pw);
                Log.i("data -->", message);
                conn.writeData(message);
                result = conn.readData();
                Log.i("message", result);
            }catch(Exception e){
                e.printStackTrace();
            }
            return result;
        }
    }
}
