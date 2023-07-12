package com.example.farm.Fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.farm.HttpConnection;
import com.example.farm.HttpUrl;
import com.example.farm.R;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.community_fragment, container, false);

        regist_review = view.findViewById(R.id.regist_review);
        setimage = view.findViewById(R.id.setimg);
        regist_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                // 갤러리에서 사진 여러개를 선택하기 위한 코드
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                try{
                    InputStream in = getContext().getContentResolver().openInputStream(data.getData());

                    Bitmap img = BitmapFactory.decodeStream(in);
                    in.close();

//                    UploadTask task = new UploadTask();
//                    task.execute(img);

                    setimage.setImageBitmap(img);
                }catch(Exception e){
                    Log.e("갤러리 불러오기 오류", "");
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
            connection.setHeader(1000, "POST",true, true);
            connection.setProperty("Connection", "Keep-Alive");
            connection.setProperty("Content-Type", "multipart/form-data;boundary=*****");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] bitmapData = byteArrayOutputStream.toByteArray();

            OutputStream outputStream = connection.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            try {
                dataOutputStream.writeBytes("--*****\r\n");
                dataOutputStream.write(bitmapData);
                dataOutputStream.flush();
                dataOutputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            connection.close_All();

            return null;
        }
    }


}
