package com.example.farm.Fragment;

import static android.app.Activity.RESULT_OK;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.farm.Dialog.RegistDialog;
import com.example.farm.HttpConnection;
import com.example.farm.HttpUrl;
import com.example.farm.R;
import com.example.farm.Review;
import com.example.farm.Session;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;

public class RegistDialogFragment extends DialogFragment {

    View view;
    ImageButton close_btn, image_button;
    ImageView image;
    Button regist_btn;
    EditText flavor, content;
    Bitmap set_image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.regist_review, container);
        close_btn = view.findViewById(R.id.close_btn);
        regist_btn = view.findViewById(R.id.regist_btn);
        flavor = view.findViewById(R.id.flavor);
        content = view.findViewById(R.id.content);
        image_button = view.findViewById(R.id.image_button);
        image = view.findViewById(R.id.image_view);

        String session_id = ((Session)getActivity().getApplication()).getSessionId();

        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // 갤러리에서 사진 여러개를 선택하기 위한 코드
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        regist_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String flavor_content = flavor.getText().toString();
                String body_content = content.getText().toString();
                SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
                Review review = new Review("참외", format.toString(), session_id, body_content, flavor_content);

                ImageTask imageTask = new ImageTask();
                try {
                    Log.i("Task Result : ", imageTask.execute(set_image).get());
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // 게시글 정보 텍스트 입력 데이터 서버로 전송
                try {
                    if(review != null){
                        CommunityTask task = new CommunityTask();
                        String result = task.execute(review).get(); // review객체를 서버에 전달 및 결과 반환
                        Log.i("Result Request : ", result);
                        if(result != null) { // 서버로부터 응답값을 받은 경우
                            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                            if (result.equals("true")) { // 게시 성공
                                dialog.setTitle("리뷰 작성").setMessage("리뷰 작성에 성공하였습니다.").setIcon(R.drawable.logo)
                                        .setPositiveButton("확인", null).show();
                                dismiss();
                            } else { // 게시 요청했는데 서버에서 값을 못받아서 false를 반환한 경우
                                dialog.setTitle("리뷰 작성").setMessage("리뷰 작성에 실패하였습니다.(네트워크 요청 오류)").setIcon(R.drawable.logo)
                                        .setPositiveButton("확인", null).show();
                            }
                        }else{ // 서버로부터 응답값을 받지 못한 경우
                            Toast.makeText(getContext(), "리뷰 작성에 실패하였습니다.(네트워크 응답 오류)", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Log.e("register Error ", "review Object is NULL");
                    }
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        Window window = getDialog().getWindow();
        if(window == null) return;
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){ // 갤러리로부터 사진을 선택하면 실행되는 메소드
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                try{
                    Uri imageUri = data.getData(); // 선택된 사진으로부터 uri를 반환받는다.
                    Log.i("imageURI : ", imageUri.toString());
                    if(imageUri != null){ // 받은 URI가 NULL이 아니라면
                        // image URI에 대한 InputStream을 연다
                        InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
                        // InputStream으로 받은 이미지를 Bitmap으로 변환한다.
                        Bitmap img = BitmapFactory.decodeStream(inputStream);

                        inputStream.close();
                        image.setImageBitmap(img);
                        image_button.setVisibility(View.INVISIBLE);
                        image.setVisibility(View.VISIBLE);
                        set_image = img;
                    }else{
                        Log.e("Image URI Null", "need uri");
                    }

                }catch(Exception e){
                    Log.e("갤러리 불러오기 오류", "");
                    e.printStackTrace();
                }
            }
        }
    }

    public class ImageTask extends AsyncTask<Bitmap, Void, String>{

        @Override
        protected String doInBackground(Bitmap...args){
            String result = "false";
            HttpUrl url = new HttpUrl();
            HttpConnection conn = new HttpConnection(url.getUrl() + "image");
            conn.setHeader(1000, "POST", true, true);
            // boundary는 파일의 시작과 끝을 의미
            conn.setProperty("Content-Type", "image/jpeg");

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            args[0].compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageData = stream.toByteArray();

            OutputStream outputStream = conn.getDataOutputStream();
            try {
                outputStream.write(imageData);
                outputStream.flush();
                outputStream.close();
                result = conn.readData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return result;
        }
    }

    public class CommunityTask extends AsyncTask<Review, Void, String> {

        @Override
        protected String doInBackground(Review... args) { // HttpUrlConnection에서 객체 전송이 안됨..인자로 초깃값 받아야 함
            HttpUrl url = new HttpUrl();
            HttpConnection conn = new HttpConnection(url.getUrl() + "regist");
            conn.setHeader(1000, "POST", true, true);
            Gson gson = new Gson();
            String temp = gson.toJson(args[0]);
            conn.writeData(temp); // json형태로 값을 전달
            String result = conn.readData();

            return result;
        }
    }

}
