package com.example.farm.Fragment;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.farm.MainActivity;
import com.example.farm.R;
import com.example.farm.Review;
import com.example.farm.ReviewActivity;
import com.example.farm.ReviewInfo;
import com.example.farm.Session;
import com.example.farm.SingleComment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class CommunityFragment extends Fragment {

    private View view;
    private Button regist_review;
    private Spinner choose_box;
    private RecyclerView community;

    Session session;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.community_fragment, container, false);

        session = (Session)((MainActivity)getActivity()).getApplication();
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

                if(list != null) {
                    Log.i("total length : ", list.size() + "");
                    ReviewRecyclerAdapter adapter = new ReviewRecyclerAdapter(list);
                    RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

                    community.setLayoutManager(manager);
                    community.setAdapter(adapter);
                }else{ // null인 경우 보이지 않는다.
                    list = new ArrayList<>();
                    ReviewRecyclerAdapter adapter = new ReviewRecyclerAdapter(list);
                    RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

                    community.setLayoutManager(manager);
                    community.setAdapter(adapter);
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

    public class ReviewRecyclerAdapter extends RecyclerView.Adapter<ReviewRecyclerAdapter.ReviewViewHolder>{

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
            TextView user_id, fruit_name, flavor, content, time, review_id;
            ImageButton heart, dialog, check, colorheart;
            EditText new_comment;
            public ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                fruit_img = itemView.findViewById(R.id.fruit_img);
                fruit_name = itemView.findViewById(R.id.fruit_name);
                user_id = itemView.findViewById(R.id.user_id);
                flavor = itemView.findViewById(R.id.flavor);
                time = itemView.findViewById(R.id.time);
                content = itemView.findViewById(R.id.content);
                heart = itemView.findViewById(R.id.heart);
                dialog = itemView.findViewById(R.id.dialog);
                check = itemView.findViewById(R.id.check);
                new_comment = itemView.findViewById(R.id.new_comment);
                review_id = itemView.findViewById(R.id.review_id);
                colorheart = itemView.findViewById(R.id.colorheart);
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
                review_id.setText(review.getReview_id());
                // 자신이 review에 좋아요 한 것이라면
                if(review.getGood() != null) {
                    if (review.getGood().equals(session.getSessionId())) {
                        colorheart.setVisibility(VISIBLE);
                        heart.setVisibility(INVISIBLE);
                    } else {
                        colorheart.setVisibility(INVISIBLE);
                        heart.setVisibility(VISIBLE);
                    }
                }
                Log.i("review ", new Gson().toJson(review));

                heart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!session.getSessionId().equals("default")) {
                            heart.setVisibility(INVISIBLE);
                            colorheart.setVisibility(VISIBLE);
                            InsertGoodTask task = new InsertGoodTask();
                            try {
                                String result = task.execute(review_id.getText().toString(), session.getSessionId()).get();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else{
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

                colorheart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 좋아요 취소
                        heart.setVisibility(VISIBLE);
                        colorheart.setVisibility(INVISIBLE);
                        DeleteGoodTask task = new DeleteGoodTask();
                        try{
                            task.execute(review_id.getText().toString(), session.getSessionId());
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });

                dialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 댓글 누를 시 댓글들을 보여주는 DialogFragment를 띄운다.
                        CommentDialogFragment dialog = new CommentDialogFragment(review.getReview_id());
                        dialog.show(getParentFragmentManager(), null);
                    }
                });

                check.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 로그인이 된 상태
                        if(!session.getSessionId().equals("default")) {
                            // 댓글 내용을 입력한 경우
                            if (new_comment.getText().length() > 0) {
                                try {
                                    CommentDialogFragment.InsertCommentTask task = new CommentDialogFragment.InsertCommentTask();
                                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                                    String now = format.format(new Date());
                                    // 여기 DB DateTime자료형 형식이랑 맞게 DateFormatter를 이용하여서 String now에 저장
                                    // Insert결과 처리해야 함
                                    String result = task.execute(new SingleComment(session.getSessionId(), new_comment.getText().toString(), now, review_id.getText().toString())).get();
                                    new_comment.setText("");
                                } catch (ExecutionException e) {
                                    throw new RuntimeException(e);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }else{
                                Toast.makeText(getContext(), "댓글 내용을 입력해야합니다.", Toast.LENGTH_LONG);
                            }
                        }else{ // 로그인이 안된 상태
                            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext()).setTitle("안내").setIcon(R.drawable.logo)
                                    .setNegativeButton("로그인하러가기", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(getContext().getApplicationContext(), LoginActivity.class);
                                            startActivity(intent);
                                        }
                                    }).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new_comment.setText("");
                                        }
                                    }).setMessage("댓글을 이용하려면 로그인해야합니다.");
                            dialog.show();
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

    // 좋아요 누를 시 전송
    public static class InsertGoodTask extends AsyncTask<String, Void, String>{

        // review_id, user_id를 받는다.
        @Override
        protected String doInBackground(String... strings) {
            HttpUrl url = new HttpUrl();
            String review_id = strings[0];
            String user_id = strings[1];
            HttpConnection conn = new HttpConnection(url.getUrl() + "insertGood");
            conn.setHeader(1000, "POST", true, true);

            conn.writeData(review_id + " " + user_id);
            String result = conn.readData();

            return result;
        }
    }

    public static class DeleteGoodTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            HttpUrl url = new HttpUrl();
            HttpConnection conn = new HttpConnection(url.getUrl() + "deleteGood?review_id=" + strings[0] + "&user_id=" + strings[1]);
            conn.setHeader(1000, "DELETE", false, true);
            String result = conn.readData();

            return result;
        }
    }


}
