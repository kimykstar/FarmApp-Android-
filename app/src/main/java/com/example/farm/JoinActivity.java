package com.example.farm;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutionException;

public class JoinActivity extends AppCompatActivity {
    EditText idEdit, pwEdit, pwCheckEdit, name, phone1, phone2, phone3, age;
    Button joinBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        idEdit = findViewById(R.id.idEdit);
        pwEdit = findViewById(R.id.pwEdit);
        pwCheckEdit = findViewById(R.id.pwCheckEdit);
        name = findViewById(R.id.name);
        phone1 = findViewById(R.id.phone1);
        phone2 = findViewById(R.id.phone2);
        phone3 = findViewById(R.id.phone3);
        joinBtn = findViewById(R.id.joinBtn);
        age = findViewById(R.id.age);
        JoinTask task = new JoinTask();

        // Join Button Click listener
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idEdit.getText().toString();
                String pw = pwCheckEdit.getText().toString();
                String name_text = name.getText().toString();
                String phone = phone1.getText().toString() + "-" + phone2.getText().toString() + "-" + phone3.getText().toString();
                String age_text = age.getText().toString();

                try {
                    String result = task.execute(id, pw, name_text, phone, age_text).get();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static class JoinTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... arg){
            String id = arg[0];
            String pw = arg[1];
            String name = arg[2];
            String phone = arg[3];
            String age = arg[4];

            HttpConnection conn = new HttpConnection("http://192.168.35.73:8081/join");
            conn.setHeader(1000, "POST", true, true);
            String message = String.format("%s %s %s %s %s", id, pw, name, phone, age);
            conn.writeData(message);
            String result = conn.readData();
            return result;
        }
    }
}
