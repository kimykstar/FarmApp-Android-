package com.example.farm.Fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.farm.Fruit;
import com.example.farm.FruitInfoActivity;
import com.example.farm.HttpConnection;
import com.example.farm.MainActivity2;
import com.example.farm.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.ExecutionException;

public class SearchFragment extends Fragment {

    private View view;

    private String TAG = "프래그먼트";
    SearchView search;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_fragment, container, false);

        search = view.findViewById(R.id.searchFruit);

        // 검색바 listener설정
        // SearchView위젯 Listener설정
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            // 검색버튼 눌렀을때 작동하는 메소드
            @Override
            public boolean onQueryTextSubmit(String query) {
                MainActivity2.SearchTask task = new MainActivity2.SearchTask();
                boolean result = false;
                try {
                    Fruit fruit = task.execute(query).get();
                    if(fruit != null) {
                        // intent로 정보 제공 layout으로 넘어감
                        Intent intent = new Intent(view.getContext().getApplicationContext(), FruitInfoActivity.class);
                        intent.putExtra("info", fruit);
                        startActivity(intent);
                    }else{
                        Toast.makeText(view.getContext().getApplicationContext(), "검색 결과가 없습니다", Toast.LENGTH_LONG).show();
                    }
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return false;
            }

            // 검색내용이 변할때 작동되는 메소드
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return view;
    }

    public static class SearchTask extends AsyncTask<String, Void, Fruit> {
        @Override
        protected Fruit doInBackground(String ... fruit) {
            HttpConnection conn = new HttpConnection("http://192.168.35.73:8081/search?fruit=" + fruit[0]);
            conn.setHeader(1000, "GET", false, true);
            // 과일 정보 받기 String형태를 object로 받기?
            String info = conn.readData();
            Gson gson = new Gson();
            Fruit f_info = gson.fromJson(info, Fruit.class);
            gson = new GsonBuilder().setPrettyPrinting().create();

            String temp = gson.toJson(f_info);
            Log.i("fruit : ", temp);
            return f_info;
        }
    }
}
