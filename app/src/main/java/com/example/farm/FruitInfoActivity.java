package com.example.farm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FruitInfoActivity extends AppCompatActivity {
    TextView fruit_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fruit_information);

        Intent intent = getIntent();
        Fruit info = (Fruit)intent.getSerializableExtra("info");

        fruit_name = findViewById(R.id.fruit_name);
        fruit_name.setText(info.getFruit_name());
    }

}
