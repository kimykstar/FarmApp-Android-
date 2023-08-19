package com.example.farm;

import static com.example.farm.Fragment.CameraFragment.rotateImage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farm.Connection.SearchTask;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class FruitFreshActivity extends AppCompatActivity {
    private ImageView fruit_image;
    private TextView fruit_name, fruit_fresh;
    private HorizontalBarChart fresh_graph;
    private ImageButton back_btn;
    private Button info_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fruitfresh_layout);

        Intent intent = getIntent();
        String info = intent.getStringExtra("freshInfo");;
        String[] temp = info.split(" ");
        String f_name = temp[1];
        String fresh_grade = temp[2];
        float fresh_num = Math.round((Float.parseFloat(temp[3]) * 100));

        fruit_image = findViewById(R.id.fruit_img);
        fruit_name = findViewById(R.id.fruit_name);
        fruit_fresh = findViewById(R.id.fruit_fresh);
        fresh_graph = findViewById(R.id.fresh_graph);
        back_btn = findViewById(R.id.back_btn);
        info_btn = findViewById(R.id.info_btn);

        Bitmap photo = null;

        // Intent로부터 Image의 URI를 받아 Bitmap으로 변환한다.
        Uri imageURI = Uri.parse(intent.getStringExtra("imageURI"));
        try {
            photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(imageURI.toString())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("Image URI : ", imageURI.toString());
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(imageURI);
//            photo = BitmapFactory.decodeStream(inputStream);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }

        if(photo != null) {
            ExifInterface ei = null;
            try {
                ei = new ExifInterface(imageURI.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap rotatedBitmap = null;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(photo, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(photo, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(photo, 270);
                    break;
            }
            fruit_image.setImageBitmap(rotatedBitmap);
        }

        fruit_name.setText(f_name);
        fruit_fresh.setText("상태 : " + fresh_grade);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fresh_graph.setDrawBarShadow(true);

        ArrayList<Float> li = new ArrayList<>();
        li.add(fresh_num);
        BarDataSet dataSet = getBarDataSet(li);
        BarData data = new BarData(dataSet);
        fresh_graph.setData(data);
        fresh_graph.setDrawValueAboveBar(false);

        XAxis xAxis = fresh_graph.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setEnabled(false);

        YAxis yLeft = fresh_graph.getAxisLeft();
        yLeft.setAxisMinimum(0f);
        yLeft.setAxisMaximum(100f);
        yLeft.setEnabled(false);

        YAxis yRight = fresh_graph.getAxisRight();
        yRight.setDrawGridLines(false);
        yRight.setDrawAxisLine(true);
        yRight.setEnabled(false);

        fresh_graph.animateY(1000);

        info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FruitInformationActivity.class);
                SearchTask task = new SearchTask();
                Fruit fruit;
                try {
                    fruit = task.execute(f_name).get();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                intent.putExtra("info", fruit);
                startActivity(intent);
            }
        });
    }

    private BarDataSet getBarDataSet(ArrayList<Float> data){
        ArrayList<BarEntry> list = new ArrayList<>();

        list.add(new BarEntry(0f, data.get(0)));
        BarDataSet dataSet = new BarDataSet(list, "Grade");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(10f);
        return dataSet;
    }

}
