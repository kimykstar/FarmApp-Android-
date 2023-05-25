package com.example.farm;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

public class DialogBox {

    private AlertDialog.Builder builder;

    public DialogBox(Context context){
        builder = new AlertDialog.Builder(context);
    }

    public AlertDialog.Builder createDialog(String title, String content){
        builder.setTitle(title).setMessage(content);
        return builder;
    }
}
