package com.example.farm.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.AbsSavedState;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.farm.DialogBox;
import com.example.farm.HttpConnection;
import com.example.farm.LoginActivity;
import com.example.farm.MainActivity;
import com.example.farm.R;
import com.example.farm.Session;

public class MyInfoFragment extends Fragment {

    private View view;
    private Button login_btn, guide_btn, logout_btn, category_btn, delete_btn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.myinfo_fragment, container, false);

        login_btn = view.findViewById(R.id.login_btn);
        guide_btn = view.findViewById(R.id.guide_btn);
        category_btn = view.findViewById(R.id.category_btn);
        logout_btn = view.findViewById(R.id.logout_btn);
        delete_btn = view.findViewById(R.id.delete_user);

        Session session = (Session)((MainActivity)getActivity()).getApplication();
        String sessionId = session.getSessionId();

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
                session.setSessionId("default");
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
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
                dialog.setNegativeButton("취소", null);
                dialog.show();
            }
        });

        Log.i("session : ", session.getSessionId());
        if(session.getSessionId().equals("default")){
            login_btn.setVisibility(View.VISIBLE);
            category_btn.setVisibility(View.INVISIBLE);
            logout_btn.setVisibility(View.INVISIBLE);
            delete_btn.setVisibility(View.INVISIBLE);
        }else{
            login_btn.setVisibility(View.INVISIBLE);
            category_btn.setVisibility(View.VISIBLE);
            logout_btn.setVisibility(View.VISIBLE);
            delete_btn.setVisibility(View.VISIBLE);
        }

        return view;
    }

    public class DeleteTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            HttpConnection conn = new HttpConnection("http://192.168.35.73:8081/delete?id=" + strings[0]);
            conn.setHeader(1000, "GET", false,  true);
            String result = conn.readData();
            Log.i("delete Result : ", result);
            return result;
        }
    }
}
