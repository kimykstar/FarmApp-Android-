package com.example.farm;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farm.Connection.HttpConnection;
import com.example.farm.Fragment.UpdateDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

public class UpdateReview extends AppCompatActivity{

    ImageButton close_btn;
    RecyclerView post_list;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_form);

        close_btn = findViewById(R.id.close_btn);
        post_list = findViewById(R.id.post_list);

        Session user_id = (Session)this.getApplication();

        try{
            ReviewTask task = new ReviewTask();
            ArrayList<ReviewInfo> reviews = task.execute(user_id.getSessionId()).get();
            Gson gson = new Gson();
            Log.i("result : ", gson.toJson(reviews));
            PostListRecyclerAdapter adapter = new PostListRecyclerAdapter(reviews);
            RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            post_list.setLayoutManager(manager);
            post_list.setAdapter(adapter);
        }catch(Exception e){
            e.printStackTrace();
        }

        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    public class ReviewTask extends AsyncTask<String, Void, ArrayList<ReviewInfo>>{

        @Override
        protected ArrayList<ReviewInfo> doInBackground(String... args) {
            ArrayList<ReviewInfo> reviews = new ArrayList<>();

            String user_id = args[0];

            HttpUrl url = new HttpUrl();
            HttpConnection conn = new HttpConnection(url.getUrl() + "userreviews?user_id=" + user_id);
            conn.setHeader(1000, "GET", false, true);

            try{
                String result = conn.readData();
                Gson gson = new Gson();
                reviews = gson.fromJson(result,  new TypeToken<ArrayList<ReviewInfo>>() {}.getType());
            }catch(Exception e){
                e.printStackTrace();
            }

            return reviews;
        }
    }

    private class PostListRecyclerAdapter extends RecyclerView.Adapter<PostListRecyclerAdapter.ReviewHolder>{

        ArrayList<ReviewInfo> reviews;

        public PostListRecyclerAdapter(ArrayList<ReviewInfo> reviews){
            this.reviews = reviews;
        }

        @NonNull
        @Override
        public ReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reviewform, parent, false);
            return new ReviewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewHolder holder, int position) {
            holder.onBind(reviews.get(position));
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

        private class ReviewHolder extends RecyclerView.ViewHolder{
            ImageView fruit_img;
            TextView user_id, fruit_name, flavor, content, time, review_id;
            ImageButton heart, dialog;
            Button update_btn, delete_btn;
            public ReviewHolder(@NonNull View itemView) {
                super(itemView);
                fruit_img = itemView.findViewById(R.id.fruit_img);
                fruit_name = itemView.findViewById(R.id.fruit_name);
                user_id = itemView.findViewById(R.id.user_id);
                flavor = itemView.findViewById(R.id.flavor);
                time = itemView.findViewById(R.id.time);
                content = itemView.findViewById(R.id.content);
                update_btn = itemView.findViewById(R.id.update_btn);
                delete_btn = itemView.findViewById(R.id.delete_btn);
                review_id = itemView.findViewById(R.id.review_id);
                update_btn.setVisibility(View.VISIBLE);
                delete_btn.setVisibility(View.VISIBLE);

                heart = itemView.findViewById(R.id.heart);
                dialog = itemView.findViewById(R.id.dialog);
                heart.setVisibility(View.INVISIBLE);
                dialog.setVisibility(View.INVISIBLE);
            }


            // update btn과 delete btn 클릭 이벤트 넣기
            public void onBind(ReviewInfo reviewInfo){
                Review review = reviewInfo.getReview();
                user_id.append(review.getUser_id());
                fruit_name.setText(review.getFruit_name());
                flavor.append(review.getFlavor());
                content.setText(review.getContent());
                time.setText(review.getReview_time());
                review_id.setText(review.getReview_id());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    fruit_img.setImageBitmap(getImageBitmap(Base64.getDecoder().decode(reviewInfo.getImage())));

                update_btn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        UpdateDialogFragment dialog = null;
                        // 서버연동

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            dialog = new UpdateDialogFragment(getImageBitmap(Base64.getDecoder().decode(reviewInfo.getImage())), review);
                        }
                        dialog.show(getSupportFragmentManager(), null);
                    }
                });


                delete_btn.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SuspiciousIndentation")
                    @Override
                    public void onClick(View v) {
                        DeleteTask task = new DeleteTask();
                        String f_name = fruit_name.getText().toString();
                        String r_time = time.getText().toString();
                        String u_id = user_id.getText().toString();
                        String cont = content.getText().toString();
                        String fla = flavor.getText().toString();
                        String review_t = review_id.getText().toString();
                        try {
                            String result = task.execute(new Review(f_name, r_time, u_id, cont, fla, review_t, "")).get();

                            // DB작업 성공 시 안내 메시지와 게시글 삭제


                            if(result.equals("true"))
                                ((ViewGroup)delete_btn.getParent()).removeAllViews();
                                Toast.makeText(getApplicationContext(), "삭제 완료!", Toast.LENGTH_LONG).show();
                            if(result.equals("false")){
                                Toast.makeText(getApplicationContext(), "삭제 오류", Toast.LENGTH_LONG).show();
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }

            private Bitmap getImageBitmap(byte[] data){
                Bitmap image = null;

                image = BitmapFactory.decodeByteArray(data, 0, data.length);

                return image;
            }
        }
    }

    private class DeleteTask extends AsyncTask<Review, Void, String>{

        @Override
        protected String doInBackground(Review... reviews) {
            Review review = reviews[0];
            String result = "false";

            HttpUrl url = new HttpUrl();
            HttpConnection conn = new HttpConnection(url.getUrl() + "deletereview?fruit_name=" + review.getFruit_name() + "&user_id=" + review.getUser_id() + "&reviewtime=" + review.getReview_time());
            conn.setHeader(1000, "DELETE", true, true);

            conn.setProperty("Content-Type", "application/json; charset=" + StandardCharsets.UTF_8);


            Gson gson = new Gson();
            conn.writeData(gson.toJson(reviews[0]));
            result = conn.readData();

            return result;
        }
    }
}
