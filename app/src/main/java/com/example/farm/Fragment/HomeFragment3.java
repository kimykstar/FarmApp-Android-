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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farm.HttpConnection;
import com.example.farm.PeriodFruit;
import com.example.farm.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class HomeFragment3 extends Fragment {

    private View view;

    private String TAG = "프래그먼트";
    RecyclerView fruit_list;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home_fragment, container, false);

        fruit_list = view.findViewById(R.id.fruit_list);

        RecommendTask task = new RecommendTask();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("M");
        String month = format.format(calendar.getTime());
        try {
            Log.i("success ", "true");
            ArrayList<PeriodFruit> fruits = task.execute(month).get();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Log.i("period : ", gson.toJson(fruits));

            FruitAdapter adapter = new FruitAdapter(fruits, getActivity());
            RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);

            fruit_list.setLayoutManager(manager);
            fruit_list.setAdapter(adapter);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return view;
    }

    public class FruitAdapter extends RecyclerView.Adapter<FruitAdapter.ViewHolder>{
        ArrayList<PeriodFruit> fruits;
        Context context;
        public FruitAdapter(ArrayList<PeriodFruit> fruits, Context context){
            this.fruits = fruits;
            this.context = context;
        }
        @NonNull
        @Override
        public FruitAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // inflater로 부모 Context의 컨텍스트를 받아오고 fruit_image레이아웃을 요소로 받아와 View객체로 객체화시킨다.
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fruit_image, parent, false);
            // ViewHolder로 객체를 생성한다.
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FruitAdapter.ViewHolder holder, int position) {
            holder.onBind(fruits.get(position));
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return fruits.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            ImageView fruit_image;
            TextView fruit_name;
            TextView fruit_period;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                fruit_image = (ImageView) itemView.findViewById(R.id.fruit_img);
                fruit_name = (TextView) itemView.findViewById(R.id.fruit_name);
//                fruit_period = (TextView) itemView.findViewById(R.id.period);
            }

            public void onBind(PeriodFruit fruit){
                int imageResource = getResources().getIdentifier(fruit.getFile_name().toLowerCase(), "drawable", requireContext().getPackageName());
                fruit_image.setImageResource(imageResource);
                fruit_name.setText(fruit.getFruit_name());
//                fruit_period.setText("제철시기 : " + fruit.getStart() + " ~ " + fruit.getEnd() + "월");
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
