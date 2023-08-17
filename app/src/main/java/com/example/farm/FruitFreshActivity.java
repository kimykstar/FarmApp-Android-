package com.example.farm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class FruitFreshActivity extends AppCompatActivity {
    private ImageView fruit_image;
    private TextView fruit_name, fruit_fresh;
    private BarChart fresh_graph;
    private ImageButton back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fruitfresh_layout);

        Intent intent = getIntent();
//        FruitFresh info = (FruitFresh) intent.getSerializableExtra("freshInfo");
        Bitmap photo = null;

        // Intent로부터 Image의 URI를 받아 Bitmap으로 변환한다.
        Uri imageURI = intent.getParcelableExtra("imageURI");
        Log.i("Image URI : ", imageURI.toString());
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageURI);
            photo = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        String info = intent.getStringExtra("freshInfo");;

        String[] temp = info.split(" ");
        String f_name = temp[1];
        String fresh_grade = temp[2];
        String fresh_num = temp[3];

        fruit_image = findViewById(R.id.fruit_img);
        fruit_name = findViewById(R.id.fruit_name);
        fruit_fresh = findViewById(R.id.fruit_fresh);
        fresh_graph = findViewById(R.id.fresh_graph);
        back_btn = findViewById(R.id.back_btn);

        if(photo != null)
            fruit_image.setImageBitmap(photo);
        else
            Log.i("Image is NULL", "");
        fruit_name.setText(f_name);
        fruit_fresh.setText(fresh_grade);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
