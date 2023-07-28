package com.example.farm.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.farm.Fruit;
import com.example.farm.FruitInfoActivity;
import com.example.farm.HttpConnection;
import com.example.farm.HttpUrl;
import com.example.farm.MainActivity;
import com.example.farm.MainActivity2;
import com.example.farm.PeriodFruit;
import com.example.farm.R;
import com.example.farm.RecommendFruit;
import com.example.farm.Session;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class HomeFragment extends Fragment {

    private View view;
    private ViewPager2 viewPager;
    private TextView recommend_tv;
    private RecyclerView recommend;
    private LinearLayout recommend_ll, view_ll;
    private SearchView search;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_content, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        recommend_tv = view.findViewById(R.id.recommend_tv);
        recommend = view.findViewById(R.id.fruit_list);
        recommend_ll = view.findViewById(R.id.recommend_fl);
        view_ll = view.findViewById(R.id.view_ll);
        search = view.findViewById(R.id.searchFruit);

        Session session = (Session)((MainActivity)getActivity()).getApplication();
        if(session.getSessionId().equals("default")) {
            recommend_ll.setVisibility(View.INVISIBLE);
            recommend_tv.setVisibility(View.INVISIBLE);
        }else{
            SharedPreferences preferences = getActivity().getSharedPreferences(session.getSessionId(), Context.MODE_PRIVATE);
            recommend_tv.setText(session.getSessionId() + "님을 위한 추천 과일");
            recommend_ll.setVisibility(View.VISIBLE);
            recommend_tv.setVisibility(View.VISIBLE);
        }

        RecommendTask task = new RecommendTask();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("M");

        // 검색바 이벤트 추가
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            // 검색버튼 눌렀을때 작동하는 메소드
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchTask task = new SearchTask();
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

        String month = format.format(calendar.getTime());
        try {
            // 제철과일 정보 받아오기
            ArrayList<PeriodFruit> fruits = task.execute(month).get();
            // 제철과일 정보를 띄우기 위한 Adapter생성 및 설정
            ViewPagerAdapter adapter = new ViewPagerAdapter(fruits, R.layout.fruit_img);
            viewPager.setAdapter(adapter);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SharedPreferences preferences = getContext().getSharedPreferences(session.getSessionId(), Context.MODE_PRIVATE);
        Map<String, String> nu = (Map<String, String>) preferences.getAll();
        ArrayList<String> temp = new ArrayList<>();
        Iterator<String> iterator = nu.keySet().iterator();
        while(iterator.hasNext()){
            String nutrition = iterator.next();
            if(preferences.getString(nutrition, "").equals("true")) // SharedPreferences에 저장된 영양소 정보가 true인 경우
                temp.add(nutrition);

        }
        String[] nutritions = temp.toArray(new String[0]);
        ArrayList<RecommendFruit> rFruits = new ArrayList<>();
        if(temp.size() > 0){
            RecommendUserTask recommendTask = new RecommendUserTask();
            try {
                rFruits = recommendTask.execute(nutritions).get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

        RecommendRecyclerView adapter = new RecommendRecyclerView(rFruits);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        recommend.setLayoutManager(manager);
        recommend.setAdapter(adapter);

        return view;
    }

    // 제철과일 추천을 위한 ViewPager Adapter클래스 선언
    // RecyclerView.Adapter를 사용
    public class ViewPagerAdapter extends RecyclerView.Adapter<FruitViewHolder>{
        ArrayList<PeriodFruit> fruits;

        int id;

        public ViewPagerAdapter(ArrayList<PeriodFruit> fruits, int id){
            this.fruits = fruits;
            this.id = id;
        }

        @NonNull
        @Override
        public FruitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // ViewHolder를 생성한다.
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.fruit_img, parent, false);
            return new FruitViewHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull FruitViewHolder holder, int position) { // ViewHolder와 Bind한다.
            holder.onBind(fruits.get(position));
        }

        @Override
        public int getItemCount() { // 총 Item갯수를 리턴
            if(fruits != null)
                return fruits.size();
            return -1;
        }
    }

    public class FruitViewHolder extends RecyclerView.ViewHolder{

        private ImageView fruit_img;
        private TextView fruit_name, fruit_period;

        public FruitViewHolder(@NonNull View itemView) {
            super(itemView);
            fruit_img = itemView.findViewById(R.id.fruit_img);
            fruit_name = itemView.findViewById(R.id.fruit_name);
            fruit_period = itemView.findViewById(R.id.fruit_period);

            int screenWidth = (int)(getResources().getDisplayMetrics().widthPixels * 0.8);
            fruit_img.getLayoutParams().width = screenWidth;
            fruit_img.getLayoutParams().height = screenWidth;
        }

        public void onBind(PeriodFruit fruit){
            if(fruit != null){
                int imageResource = getResources().getIdentifier(fruit.getFile_name().toLowerCase(), "drawable", requireContext().getPackageName());
                fruit_img.setImageResource(imageResource);
                fruit_name.setText(fruit.getFruit_name());
                fruit_period.append(fruit.getStart() + "월 ~ " + fruit.getEnd() + "월");
            }

        }
    }


    // 사용자 맞춤 과일을 추천하기 위한 RecyclerView의 Adapter선언
    public class RecommendRecyclerView extends RecyclerView.Adapter<RecommendViewHolder>{
        ArrayList<RecommendFruit> fruits;

        public RecommendRecyclerView(ArrayList<RecommendFruit>fruits){
            this.fruits = fruits;
        }
        @NonNull
        @Override
        public RecommendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // ViewHolder를 생성하여 반환하는 메소드
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fruit_image, parent, false);
            return new RecommendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecommendViewHolder holder, int position) { // fruits(과일 목록)을 참조하여 요소 하나당 ViewHolder메소드를 통하여 레이아웃 생성
            holder.onBind(fruits.get(position));
        }

        @Override
        public int getItemCount() {
            return fruits.size();
        }
    }

    public class RecommendViewHolder extends RecyclerView.ViewHolder{
        ImageView fruit_img;
        TextView fruit_name;
        TextView nutrition_name;
        public RecommendViewHolder(@NonNull View itemView) {
            super(itemView);

            fruit_img = itemView.findViewById(R.id.fruit_img);
            fruit_name = itemView.findViewById(R.id.fruit_name);
            nutrition_name = itemView.findViewById(R.id.nutrition_tv);
        }

        public void onBind(RecommendFruit fruit){
            int imageResource = getResources().getIdentifier(fruit.getFruit_img().toLowerCase(), "drawable", requireContext().getPackageName());
            fruit_img.setImageResource(imageResource);
            fruit_name.setText(fruit.getFruit_name());
            nutrition_name.setText(fruit.getNutrition_name() + ": " + fruit.getNutrition_amount() + fruit.getUnit());
        }
    }

    public static class RecommendTask extends AsyncTask<String, Void, ArrayList<PeriodFruit>> {

        @Override
        protected ArrayList<PeriodFruit> doInBackground(String... strings) {
            HttpUrl url = new HttpUrl();
            HttpConnection conn = new HttpConnection(url.getUrl() + "period?month=" + strings[0]);
            conn.setHeader(1000, "GET", false, true);
            String fruit_list = conn.readData();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            Log.i("fruit_list(background) : ", gson.toJson(fruit_list));
            ArrayList<PeriodFruit> result = gson.fromJson(fruit_list, new TypeToken<ArrayList<PeriodFruit>>() {}.getType());

            return result;
        }
    }

    public static class RecommendUserTask extends AsyncTask<String, Void, ArrayList<RecommendFruit>>{

        @Override
        protected ArrayList<RecommendFruit> doInBackground(String... nutritions) {
            HttpUrl url = new HttpUrl();
            HttpConnection conn;
            // nutritions갯수에 따라 제어
            if(nutritions.length == 1)
                conn = new HttpConnection(url.getUrl() + "recommend?nutrition=" + nutritions[0]);
            else if(nutritions.length == 2)
                conn = new HttpConnection(url.getUrl() + "recommend?nutrition=" + nutritions[0] + "&nutrition=" + nutritions[1]);
            else
                conn = new HttpConnection(url.getUrl() + "recommend?nutrition=" + nutritions[0] + "&nutrition=" + nutritions[1] + "&nutrition=" + nutritions[2]);
            conn.setHeader(1000, "GET", false, true);
            String result = conn.readData();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Log.i("recommend Fruits : ", gson.toJson(result));
            ArrayList<RecommendFruit> fruits = gson.fromJson(result, new TypeToken<ArrayList<RecommendFruit>>() {}.getType());
            return fruits;
        }
    }

    public static class SearchTask extends AsyncTask<String, Void, Fruit> {
        @Override
        protected Fruit doInBackground(String ... fruit) {
            HttpUrl url = new HttpUrl();
            HttpConnection conn = new HttpConnection(url.getUrl() + "search?fruit=" + fruit[0]);
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
