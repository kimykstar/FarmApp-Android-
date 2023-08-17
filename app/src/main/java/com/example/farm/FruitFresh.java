package com.example.farm;

import android.graphics.Bitmap;

import java.io.Serializable;

public class FruitFresh implements Serializable {

    private String info;
    private Bitmap photo;

    public FruitFresh(String info, Bitmap photo){
        this.info = info;
        this.photo = photo;
    }

    public String getInfo() {
        return info;
    }

    public Bitmap getPhoto() {
        return photo;
    }
}
