package com.example.farm.Fragment;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.farm.Dialog.RegistDialog;
import com.example.farm.DialogBox;
import com.example.farm.HttpConnection;
import com.example.farm.HttpUrl;
import com.example.farm.LoginActivity;
import com.example.farm.R;
import com.example.farm.Session;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CommunityFragment extends Fragment {

    private View view;
    private Button regist_review;
    private ImageView setimage;
    private Spinner choose_box;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.community_fragment, container, false);

        regist_review = view.findViewById(R.id.regist_review);
//        setimage = view.findViewById(R.id.setimg);
        choose_box = view.findViewById(R.id.choose_box);
//        regist_review.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                // 갤러리에서 사진 여러개를 선택하기 위한 코드
////                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                intent.setType("image/*");
//                startActivityForResult(intent, 1);
//            }
//        });

        // 등록버튼을 누르면 게시글을 작성하는 form이 나옴
        regist_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dialog객체 생성
                Session session = (Session)getActivity().getApplication();
                if(session != null) {                                // 세션이 있는 경우(로그인) Regist Form을 띄워준다.
                    RegistDialogFragment dialog = new RegistDialogFragment();
                    dialog.show(getChildFragmentManager(), null);
//                    RegistDialog dialog = new RegistDialog(getContext(), session.getSessionId());
//                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams(); // Layout의 매개변수를 관리할 수 있는 WindowManager생성
//                    lp.copyFrom(dialog.getWindow().getAttributes()); // dialog의 Window의 속성을 lp객체에 복사
//                    lp.width = WindowManager.LayoutParams.MATCH_PARENT; // lp의 너비속성을 Match_parent로 설정
//                    lp.height = WindowManager.LayoutParams.MATCH_PARENT; // lp의 높이 속성을 Match_parent로 설정
//                    Window window = dialog.getWindow(); // dialog의 Window에 대한 객체 생성
//                    window.setAttributes(lp); // dialog의 Window속성을 설정한다.
//                    dialog.show();
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

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                try{
                    // Intent로부터 URI를 받는다.
                    // 인코딩 : 파일에 저장된 정보의 형태나 형식을 데이터 표준화, 보안,
                    // 처리속도 향상, 저장공간 절약 등을 위해서 다른 형태로 변환하는 처리 혹은
                    // 방식으로 주로 이메일 등의 전송이나 동영상, 이미지에서 많이 사용된다.
                    // Base64 인코딩 : 이진데이터를 아스키 문자로만 이루어진 텍스트로 변환시키는 인코딩
                    Uri imageUri = data.getData();
                    Log.i("imageURI : ", imageUri.toString());
                    if(imageUri != null){ // 받은 URI가 NULL이 아니라면
                        // image URI에 대한 InputStream을 연다
                        InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
                        // InputStream으로 받은 이미지를 Bitmap으로 변환한다.
                        Bitmap img = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        setimage.setImageBitmap(img);
                        UploadTask task = new UploadTask();
                        task.execute(img);
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

    public class UploadTask extends AsyncTask<Bitmap, Void, Void> {
        @Override
        protected Void doInBackground(Bitmap... params){
            Bitmap selectedImage = params[0];
            HttpUrl url = new HttpUrl();
            HttpConnection connection = new HttpConnection(url.getUrl() + "load");
//            connection.setProperty("Connection", "Keep-Alive");
            connection.setProperty("Content-Type", "multipart/form-data;boundary=*****");
            connection.setHeader(1000, "POST",true, true);


            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] bitmapData = byteArrayOutputStream.toByteArray();

            DataOutputStream dataOutputStream = connection.getDataOutputStream();
            try {
                connection.connect_method();
                // 이미지 데이터 전송
//                dataOutputStream.writeBytes("--*****\r\n");
//                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n");
//                dataOutputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n");
//                dataOutputStream.write(bitmapData);
//                dataOutputStream.writeBytes("\r\n");
//                dataOutputStream.writeBytes("--*****--\r\n");
//                dataOutputStream.flush();
                connection.writeData("--*****\r\n Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n");
//                connection.writeData("Content-Type: image/jpeg\r\n\r\n");
//                connection.writeData(bitmapData);
//                connection.writeData()
                Log.i("image Load : ", "--------------------");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            return null;
        }
    }


}
