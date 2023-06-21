package com.example.farm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class FruitInfo implements Serializable {

    private ArrayList<Nutrition> allInfos  = new ArrayList<Nutrition>();
    private String fruitName;

    public FruitInfo(ArrayList<Nutrition> infoList, String f_name){
        this.allInfos = infoList;
        this.fruitName = f_name;
    }

    public FruitInfo(String f_name){this.fruitName = f_name;}

    // 메소드
    public void setAllInfos(ArrayList<Nutrition> allInfos) {
        this.allInfos = allInfos;
    }

    public ArrayList<Nutrition> getAllInfos() {
        return allInfos;
    }

    public void add(Nutrition nut){
        allInfos.add(nut);}

    public String getFruitName() {
        return fruitName;
    }

    public Iterator<Nutrition> iterator(){
        return allInfos.iterator();
    }
}