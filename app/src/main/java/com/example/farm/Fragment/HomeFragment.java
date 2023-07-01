package com.example.farm.Fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.farm.HttpConnection;
import com.example.farm.PeriodFruit;
import com.example.farm.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class HomeFragment extends Fragment {

    private View view;
    private ViewPager2 viewPager;
    private TextView hashTag;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_content, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        hashTag = view.findViewById(R.id.tv_tag1);

        RecommendTask task = new RecommendTask();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("M");
        String month = format.format(calendar.getTime());
        try {
            ArrayList<PeriodFruit> fruits = task.execute(month).get();
            ViewPagerAdapter adapter = new ViewPagerAdapter(fruits);

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

        return view;
    }

    public class ViewPagerAdapter extends RecyclerView.Adapter<FruitViewHolder>{
        ArrayList<PeriodFruit> fruits;

        public ViewPagerAdapter(ArrayList<PeriodFruit> fruits){
            this.fruits = fruits;
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            Log.i("period : ", gson.toJson(fruits));
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
            return fruits.size();

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

    public static class RecommendTask extends AsyncTask<String, Void, ArrayList<PeriodFruit>> {

        @Override
        protected ArrayList<PeriodFruit> doInBackground(String... strings) {
            HttpConnection conn = new HttpConnection("http://192.168.35.73:8081/period?month=" + strings[0]);
            conn.setHeader(1000, "GET", false, true);
            String fruit_list = conn.readData();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            Log.i("fruit_list(background) : ", gson.toJson(fruit_list));

            ArrayList<PeriodFruit> result = gson.fromJson(fruit_list, new TypeToken<ArrayList<PeriodFruit>>() {}.getType());

            return result;
        }
    }

}
