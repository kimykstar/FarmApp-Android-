package com.example.farm.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.farm.Dialog.CustomDialog;
import com.example.farm.HttpConnection;
import com.example.farm.HttpUrl;
import com.example.farm.LoginActivity;
import com.example.farm.MainActivity;
import com.example.farm.R;
import com.example.farm.Session;
import com.example.farm.UpdateReview;

public class MyInfoFragment extends Fragment {

    private View view;
    private Button login_btn, guide_btn, logout_btn, category_btn, delete_btn, review_btn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.myinfo_fragment, container, false);

        login_btn = view.findViewById(R.id.login_btn);
        guide_btn = view.findViewById(R.id.guide_btn);
        category_btn = view.findViewById(R.id.category_btn);
        logout_btn = view.findViewById(R.id.logout_btn);
        delete_btn = view.findViewById(R.id.delete_user);
        review_btn = view.findViewById(R.id.review_btn);

        Session session = (Session)((MainActivity)getActivity()).getApplication();
        String sessionId = session.getSessionId();

        // 나의 게시글 보기 버튼 클릭 시
        review_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getContext(), UpdateReview.class);
                startActivity(intent);
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("로그아웃").setMessage("로그아웃 하시겠습니까?").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        session.setSessionId("default");
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        startActivity(intent);
                    }
                }).setNegativeButton("취소", null).show();

            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("회원탈퇴").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            DeleteTask task = new DeleteTask();
                            Log.i("delete Session : ", sessionId + "");
                            String result = task.execute(sessionId).get();
                            if (result.equals("true")) {
                                Toast.makeText(getContext(), "회원탈퇴 완료!", Toast.LENGTH_LONG).show();
                                session.setSessionId("default");
                                SharedPreferences preferences = getContext().getSharedPreferences(sessionId, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                startActivity(intent);
                            }else{
                                Toast.makeText(getContext(), "회원탈퇴 실패!", Toast.LENGTH_LONG).show();
                            }
                        }catch(Exception e){
                            Toast.makeText(getContext(), "서버 통신 오류", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                dialog.setMessage("Farm 회원을 탈퇴하시겠습니까?");
                dialog.setNegativeButton("취소", null).setIcon(R.drawable.logo);
                dialog.show();
            }
        });

        category_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialog dialog = new  CustomDialog(getContext(), sessionId);
                // WindowManager의 Layoutparameter변수를 생성하여 copyFrom을 통해 CustomDialog의 Window속성을 가져온다.
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                // layout Parameter의 height와 width를 설정하고 dialog의 Window를 불러와 속성을 재 설정한다.
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                Window window = dialog.getWindow();
                window.setAttributes(lp);
                dialog.show();
            }
        });

        Log.i("session : ", session.getSessionId());
        if(session.getSessionId().equals("default")){
            login_btn.setVisibility(View.VISIBLE);
            category_btn.setVisibility(View.INVISIBLE);
            logout_btn.setVisibility(View.INVISIBLE);
            delete_btn.setVisibility(View.INVISIBLE);
            review_btn.setVisibility(View.INVISIBLE);
        }else{
            login_btn.setVisibility(View.INVISIBLE);
            category_btn.setVisibility(View.VISIBLE);
            logout_btn.setVisibility(View.VISIBLE);
            delete_btn.setVisibility(View.VISIBLE);
            review_btn.setVisibility(View.VISIBLE);
        }

        return view;
    }

    public class DeleteTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            HttpUrl url = new HttpUrl();
            HttpConnection conn = new HttpConnection(url.getUrl() + "delete?id=" + strings[0]);
            conn.setHeader(1000, "GET", false,  true);
            String result = conn.readData();
            Log.i("delete Result : ", result);
            return result;
        }
    }
}
