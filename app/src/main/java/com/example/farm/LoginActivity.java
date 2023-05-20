package com.example.farm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {
    Button loginBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginBtn = findViewById(R.id.loginBtn);
        EditText idText = findViewById(R.id.id);
        EditText pwText = findViewById(R.id.pw);
        Intent intent = new Intent(this, MainActivity.class);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginActivity activity = new loginActivity();
                String id = idText.getText().toString();
                String pw = pwText.getText().toString();
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
                    setSession(id);
                    startActivity(intent);
                }else{
                    Toast.makeText(LoginActivity.this, "아이디 혹은 비밀번호를 확인하세요", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // 로그인 후 세션 만들기 메소드
    public void setSession(String id){
        SharedPreferences preference = getApplicationContext().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preference.edit();
        // 세션이 없는 경우 세션을 만들어야 함
        edit.putString("userSession", id);
        edit.apply(); // 비동기 처리
        Log.d("Session Result : ", preference.getString("userSession", null));
    }

    public void deleteSession(String id){
        SharedPreferences preference = getApplicationContext().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preference.edit();
        // 세션이 있다면
        if(preference.getString("userSession", null) != null){
            edit.clear(); // 세션 해제
        }
    }

    public static class loginActivity extends AsyncTask<String, Void, String> {
        String result;
        @Override
        protected String doInBackground(String... arg){
            try{
                String id = arg[0];
                String pw = arg[1];
                HttpConnection conn = new HttpConnection("http://192.168.35.73:8081/login");
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
