package com.example.farm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Iterator;

public class FruitInfoActivity extends AppCompatActivity {
    TextView fruit_name;
    GridView nutritions;
    Button detail;
    TextView calories, carbohydrate, protein, fat, sugar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fruit_information);

        Intent intent = getIntent();
        Fruit info = (Fruit)intent.getSerializableExtra("info");

        detail = findViewById(R.id.detail);
        fruit_name = findViewById(R.id.fruit_name);
        calories = findViewById(R.id.calories);
        carbohydrate = findViewById(R.id.carbohydrate);
        protein = findViewById(R.id.protein);
        fat = findViewById(R.id.fat);
        sugar = findViewById(R.id.sugar);

//        nutritions = findViewById(R.id.nutritions);

        fruit_name.setText(info.getFruit_name());
        calories.setText("칼로리 : " + info.getCalories() + " Kcal");
        carbohydrate.setText("탄수화물 : " + info.getCarbohydrate() + " g");
        protein.setText("단백질 : " + info.getProtein() + " g");
        fat.setText("지방 : " + info.getFat() + " g");
        sugar.setText("당 : " + info.getSugar() + " g");
        detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailIntent = new Intent(getApplicationContext(), FruitDetailInfoActivity.class);
                detailIntent.putExtra("Fruit", info);
                startActivity(detailIntent);
            }
        });

//        NutritionAdapter adapter = new NutritionAdapter();
//        ArrayList<Nutrition> infos = info.getFruitInfo().getInfoList(); // FruitInfo클래스의 영양소 배열을 반환
//        adapter = new NutritionAdapter(this, infos);
//
//        nutritions.setAdapter(adapter);

    }

    public class NutritionAdapter extends BaseAdapter {
        ArrayList<Nutrition> infos = new ArrayList<Nutrition>();
        Context context;

        public NutritionAdapter(){}
        public NutritionAdapter(Context context, ArrayList<Nutrition> infos) {
            this.context = context;
            this.infos = infos;
        }
        @Override
        public int getCount() {
            return infos.size();
        }

        @Override
        public Object getItem(int position) {
            return infos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView text = new TextView(context);

            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            text.setLayoutParams(lp);

            text.setText(infos.get(position).getNutrition());
            text.setTextColor(Color.BLACK);
            text.setTextSize(12f);

            return text;
        }
    }

}
