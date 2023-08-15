package com.example.farm.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.example.farm.Nutrition;
import com.example.farm.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Iterator;

public class NutritionDialog extends Dialog {

    private BarChart vitamin_chart, etc_chart;
    private ImageButton close_btn;


    public NutritionDialog(@NonNull Context context, ArrayList<Nutrition> vitamin, ArrayList<Nutrition> etc) {
        super(context);
        setContentView(R.layout.innutrition_dialog);

        vitamin_chart = findViewById(R.id.vitamin_ch);
        etc_chart = findViewById(R.id.etc_ch);
        close_btn = findViewById(R.id.close_btn);

        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        BarData vitaminBarData = getBarDatas(vitamin);
        vitamin_chart.setData(vitaminBarData);
        XAxis xAxis = vitamin_chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String name = vitamin.get((int)value).getNutrition();
                StringBuilder nutrition = new StringBuilder("");
                for(int i = 0; i < name.length(); i++){
                    if(name.charAt(i) == '(')
                        break;
                    nutrition.append(name.charAt(i));
                }
                return nutrition.toString();
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        vitamin_chart.animateXY(1000, 1000);

        BarData etcBarData = getBarDatas(etc);
        etc_chart.setData(etcBarData);
        XAxis xAxisEtc = etc_chart.getXAxis();
        xAxisEtc.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String name = etc.get((int)value).getNutrition();
                StringBuilder nutrition = new StringBuilder("");
                for(int i = 0; i < name.length(); i++){
                    if(name.charAt(i) == '(')
                        break;
                    nutrition.append(name.charAt(i));
                }
                return nutrition.toString();
            }
        });
        xAxisEtc.setPosition(XAxis.XAxisPosition.BOTTOM);
        etc_chart.animateXY(1000, 1000);

    }

    private BarData getBarDatas(ArrayList<Nutrition> nutritions){

        Iterator<Nutrition> it = nutritions.iterator();
        ArrayList<BarEntry> list = new ArrayList<>();
        int cnt = 0;
        while(it.hasNext()){
            Nutrition temp = it.next();
            list.add(new BarEntry(cnt++, (float)temp.getAmount()));
        }

        BarDataSet barDataSet = new BarDataSet(list, "");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(12f);

        BarData barData = new BarData(barDataSet);

        return barData;
    }
}
