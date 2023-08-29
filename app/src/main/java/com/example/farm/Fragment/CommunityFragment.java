package com.example.farm.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farm.Connection.HttpConnection;
import com.example.farm.HttpUrl;
import com.example.farm.LoginActivity;
import com.example.farm.R;
import com.example.farm.Review;
import com.example.farm.ReviewActivity;
import com.example.farm.ReviewInfo;
import com.example.farm.Session;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Base64;

public class CommunityFragment extends Fragment {

    private View view;
    private Button regist_review;
    private Spinner choose_box;
    private RecyclerView community;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.community_fragment, container, false);

        regist_review = view.findViewById(R.id.regist_review);
        choose_box = view.findViewById(R.id.choose_box);
        community = view.findViewById(R.id.community);

        // choose_box adapter코드 추가
        ChooseTask task = new ChooseTask();
        try{
            ArrayList<String> names = task.execute().get();
            ArrayAdapter adapter = new ArrayAdapter(getContext().getApplicationContext(), android.R.layout.simple_spinner_item, names);
            choose_box.setAdapter(adapter);
        }catch(Exception e){
            e.printStackTrace();
        }

        // 등록버튼을 누르면 게시글을 작성하는 form이 나옴
        regist_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dialog객체 생성
                Session session = (Session)getActivity().getApplication();
                if(!session.getSessionId().equals("default")) {                                // 세션이 있는 경우(로그인) Regist Form을 띄워준다.
//                    RegistDialogFragment dialog = new RegistDialogFragment(choose_box.getSelectedItem().toString());
                    RegistDialogFragment dialog = new RegistDialogFragment();
                    dialog.show(getChildFragmentManager(), null);

                }else{                      // 세션이 없는 경우(비 로그인)
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setTitle("안내").setMessage("로그인이 필요한 서비스입니다 로그인하시겠습니까?")
                            .setPositiveButton("로그인하러가기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getContext(), LoginActivity.class);
                            startActivity(intent);
                        }
                    }).setNegativeButton("취소", null)
                            .setIcon(R.drawable.logo).show();

                }
            }
        });

        choose_box.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // item이 선택되었을 때 과일의 이름을 서버로 보내서 관련 게시글만 받아온다.
                ReviewTask reviewTask = new ReviewTask();
                ArrayList<ReviewInfo> list = null;
                try {
                    list = reviewTask.execute(choose_box.getSelectedItem().toString()).get();
                    Log.i("size : ", list.size() + "");
                } catch (Exception e) {
                    Log.i("Review get Error!", "True");
                }
                if(list != null){
                    Log.i("total length : ", list.size() + "");
                    ReviewRecyclerAdapter adapter = new ReviewRecyclerAdapter(list);
                    RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

                    community.setLayoutManager(manager);
                    community.setAdapter(adapter);

                }else{
                    Toast.makeText(getContext(), "게시물 받아오기 오류", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return view;
    }

    // 서버로부터 게시글 사진 및 정보 가져오기
    private static class ReviewTask extends AsyncTask<String, Void, ArrayList<ReviewInfo>>{

        @Override
        protected ArrayList<ReviewInfo> doInBackground(String ... args) {
            String fruit_name = args[0];
            ArrayList<ReviewInfo> result = new ArrayList<>();
            HttpUrl url = new HttpUrl();
            HttpConnection conn = new HttpConnection(url.getUrl() + "reviews?fruit_name=" + fruit_name);
            conn.setHeader(1000, "GET", false, true);

            conn.connect();
            try{
                String temp = conn.readData();
                Gson gson = new Gson();
                result = gson.fromJson(temp, new TypeToken<ArrayList<ReviewInfo>>() {}.getType());
            }catch(Exception e){
                e.printStackTrace();
            }
            conn.closeAll();
            return result;
        }
    }

    public static class ReviewRecyclerAdapter extends RecyclerView.Adapter<ReviewRecyclerAdapter.ReviewViewHolder>{

        ArrayList<ReviewInfo> reviews;

        public ReviewRecyclerAdapter(ArrayList<ReviewInfo> reviews){
            this.reviews = reviews;
        }
        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reviewform, parent, false);
            return new ReviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            holder.onBind(reviews.get(position));
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

        public class ReviewViewHolder extends RecyclerView.ViewHolder{

            ImageView fruit_img;
            TextView user_id, fruit_name, flavor, content, time;
            ImageButton good, dialog;
            public ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                fruit_img = itemView.findViewById(R.id.fruit_img);
                fruit_name = itemView.findViewById(R.id.fruit_name);
                user_id = itemView.findViewById(R.id.user_id);
                flavor = itemView.findViewById(R.id.flavor);
                time = itemView.findViewById(R.id.time);
                content = itemView.findViewById(R.id.content);
                good = itemView.findViewById(R.id.good);
                dialog = itemView.findViewById(R.id.dialog);
            }

            public void onBind(ReviewInfo reviewInfo){
                Bitmap image = null;
                Review review = reviewInfo.getReview();
                user_id.append(review.getUser_id());
                fruit_name.setText(review.getFruit_name());
                flavor.append(review.getFlavor());
                content.setText(review.getContent());
                time.setText(review.getReview_time());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    fruit_img.setImageBitmap(getImageBitmap(Base64.getDecoder().decode(reviewInfo.getImage())));

            }

            private Bitmap getImageBitmap(byte[] data){
                Bitmap image = null;

                image = BitmapFactory.decodeByteArray(data, 0, data.length);

                return image;
            }
        }
    }

    // GridView에 연결하는 Adapter로 ImageView와 연결하는 Adapter
    public static class ReviewAdapter extends BaseAdapter{
        ArrayList<ReviewInfo> list;
        Context context;
        public ReviewAdapter(ArrayList<ReviewInfo> list){
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            context = parent.getContext();

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.review_image, parent, false);
            }

            ImageView image_view = convertView.findViewById(R.id.review_image);
            ReviewInfo reviewInfo = list.get(position); // Get the ReviewInfo for the current position
            byte[] image = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                image = Base64.getDecoder().decode(reviewInfo.getImage());
            }

            if (image != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                image_view.setImageBitmap(bitmap);
                View finalConvertView = convertView;
                image_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Review review = reviewInfo.getReview();
                        Intent intent = new Intent(finalConvertView.getContext().getApplicationContext(), ReviewActivity.class);
                        intent.putExtra("review", review);
                        finalConvertView.getContext().startActivity(intent);
                    }
                });
            }

            return convertView;
        }
    }

    public class ChooseTask extends AsyncTask<Void, Void, ArrayList<String>>{

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> list = new ArrayList<>();

            HttpUrl url = new HttpUrl();
            HttpConnection conn = new HttpConnection(url.getUrl() + "fruitnames");
            conn.setHeader(1000, "GET", false, true);
            String result = conn.readData();
            Gson gson = new Gson();
            list = gson.fromJson(result, new TypeToken<ArrayList<String>>() {}.getType());

            return list;
        }
    }


}
