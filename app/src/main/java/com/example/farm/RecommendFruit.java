package com.example.farm;

import java.util.ArrayList;

public class RecommendFruit {

    private String fruit_img;
    private String fruit_name;
    private String nutrition_name;

    public RecommendFruit(String fruit_img, String fruit_name, String nutrition_name){
        this.fruit_img = fruit_img;
        this.fruit_name = fruit_name;
        this.nutrition_name = nutrition_name;
    }

    public String getFruit_img() {
        return fruit_img;
    }

    public String getFruit_name() {
        return fruit_name;
    }

    public String getNutrition_name() {
        return nutrition_name;
    }
}
