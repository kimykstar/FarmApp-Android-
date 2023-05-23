package com.example.farm;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {
    Button loginBtn;
    Session session;
    Button join;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginBtn = findViewById(R.id.loginBtn);
        EditText idText = findViewById(R.id.id);
        EditText pwText = findViewById(R.id.pw);
        Intent intent = new Intent(this, MainActivity.class);
        join = findViewById(R.id.join);
        session = (Session)getApplication();
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginTask activity = new LoginTask();
                String id = idText.getText().toString();
                if(id.length() == 0) {
                    Toast.makeText(getApplicationContext(), "아이디를 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                String pw = pwText.getText().toString();
                if(pw.length() == 0){
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                String result = null;
                try {
                    result = activity.execute(id, pw).get();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Log.i("main : ", result);
                if(result.equals("true")){
                    session.setSessionId(id);
                    startActivity(intent);
                    Log.i("LoginSession2", session.getSessionId());
                }else{
                    Toast.makeText(LoginActivity.this, "아이디 혹은 비밀번호를 확인하세요", Toast.LENGTH_LONG).show();
                }
            }
        });
        join.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent joinIntent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivity(joinIntent);
            }
        });
    }
    public static class LoginTask extends AsyncTask<String, Void, String> {
        String result;
        @Override
        protected String doInBackground(String... arg){
            try{
                String id = arg[0];
                String pw = arg[1];
                HttpConnection conn = new HttpConnection("http://192.168.55.89:8081/login");
                conn.setHeader(1000, "POST", true, true);
                String message = String.format("%s %s", id, pw);
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
