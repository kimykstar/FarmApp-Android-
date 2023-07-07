package com.example.farm.Fragment;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.farm.HttpConnection;
import com.example.farm.HttpUrl;
import com.example.farm.MainActivity;
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
    private TextView hashTag, recommend_tv;
    private RecyclerView recommend;
    private LinearLayout recommend_ll;
    private LinearLayout view_ll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_content, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        hashTag = view.findViewById(R.id.tv_tag1);
        recommend_tv = view.findViewById(R.id.recommend_tv);
        recommend = view.findViewById(R.id.fruit_list);
        recommend_ll = view.findViewById(R.id.recommend_fl);
        view_ll = view.findViewById(R.id.view_ll);

        // ViewPager 화면 크기에 맞게 크기 조절
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager)getActivity().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view_ll.getLayoutParams();
        params.width = (int) (metrics.widthPixels * 0.8);
        params.height = (int)(metrics.widthPixels * 0.8);
        view_ll.setLayoutParams(params);


        Session session = (Session)((MainActivity)getActivity()).getApplication();
        if(session.getSessionId().equals("default"))
            recommend_ll.setVisibility(View.INVISIBLE);
        recommend_tv.setText(session.getSessionId() + "님을 위한 과일");

        RecommendTask task = new RecommendTask();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("M");

        String month = format.format(calendar.getTime());
        try {
            // 제철과일 정보 받아오기
            ArrayList<PeriodFruit> fruits = task.execute(month).get();
            // 제철과일 정보를 띄우기 위한 Adapter생성 및 설정
            ViewPagerAdapter adapter = new ViewPagerAdapter(fruits, R.layout.fruit_img);
            viewPager.setAdapter(adapter);
            Iterator<PeriodFruit> it = fruits.iterator();
            while(it.hasNext()){
                 String text = "#" + it.next().getFruit_name() + "  ";
                 hashTag.append(text);
            }

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
        private TextView tv_tag;

        public FruitViewHolder(@NonNull View itemView) {
            super(itemView);
            fruit_img = itemView.findViewById(R.id.fruit_img);
        }

        public void onBind(PeriodFruit fruit){
            if(fruit != null){
                int imageResource = getResources().getIdentifier(fruit.getFile_name().toLowerCase(), "drawable", requireContext().getPackageName());
                fruit_img.setImageResource(imageResource);
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
            nutrition_name = itemView.findViewById(R.id.nutritiion_tv);
        }

        public void onBind(RecommendFruit fruit){
            int imageResource = getResources().getIdentifier(fruit.getFruit_img().toLowerCase(), "drawable", requireContext().getPackageName());
            fruit_img.setImageResource(imageResource);
            fruit_name.setText(fruit.getFruit_name());
            nutrition_name.setText(fruit.getNutrition_name());
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
//            nutritions = url.getEncodeingURLParam(nutritions);
            HttpConnection conn = new HttpConnection(url.getUrl() + "recommend?nutrition=" + nutritions[0] + "&nutrition=" + nutritions[1] + "&nutrition=" + nutritions[2]);
            Log.i("Recommend URL : ", url.getUrl() + "recommend?nutrition=" + nutritions[0] + "&nutirition=" + nutritions[1] + "&nutirition=" + nutritions[2]);
            conn.setHeader(1000, "GET", false, true);
            String result = conn.readData();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Log.i("recommend Fruits : ", gson.toJson(result));
            ArrayList<RecommendFruit> fruits = gson.fromJson(result, new TypeToken<ArrayList<RecommendFruit>>() {}.getType());
            return fruits;
        }

    }

}
