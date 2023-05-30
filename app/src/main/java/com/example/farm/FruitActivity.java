package com.example.farm;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FruitActivity extends AppCompatActivity {
    TextView infoText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit);

        infoText = findViewById(R.id.fruitInfo);

        Intent intent = getIntent();
        Fruit info = (Fruit)intent.getSerializableExtra("info");

        infoText.setText("Fruit Name : " + info.getFruit_name() + ", Fruit Calories : " + info.getCalories()
         + ", Fruit Carbohydrate : " + info.getCarbohydrate() + ", Fruit Protein : " + info.getProtein()
         + ", Fruit Fat : " + info.getFat() + ", Fruit Sugar : " + info.getSugar());
    }

}
