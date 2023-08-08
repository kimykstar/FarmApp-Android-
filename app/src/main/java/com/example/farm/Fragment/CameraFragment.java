package com.example.farm.Fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.farm.FruitInformationActivity;
import com.example.farm.MainActivity;
import com.example.farm.R;
import com.example.farm.TFlite;
import com.example.farm.VideoActivity;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CameraFragment extends Fragment {

    private View view;
    private ImageView image;
    private Button camera_btn, video_btn;
    String mCurrentPhotoPath = null;
    static final int REQUEST_TAKE_PHOTO = 1;

//    Map<Integer, Object> outputs = new HashMap<>();

    public CameraFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.start_camera_layout, container, false);
        image = view.findViewById(R.id.image);
        camera_btn = view.findViewById(R.id.camera_open);
        video_btn = view.findViewById(R.id.video_open);

        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        video_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext().getApplicationContext(), VideoActivity.class);
                startActivity(intent);
            }
        });


        return view;
    }

    // 카메라로 촬영한 이미지를 파일로 저장
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // 카메라 인텐트를 실행하는 함수
    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch(IOException e){
                Log.i("Camera Error", null);
            }

            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(getContext(), "com.example.farm.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // 카메라로부터 찍은 사진을 imageView에 설정
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        double result = 0;
        try{
            switch(requestCode){ // 카메라로 촬영한 사진을 모델링파일에 전달하여 결과값을 받는다.
                case REQUEST_TAKE_PHOTO:{
                    if(resultCode == RESULT_OK){
                        File file = new File(mCurrentPhotoPath);
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.fromFile(file));
                        if(bitmap != null){
                            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                            Bitmap rotatedBitmap = null;
                            switch(orientation){
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotatedBitmap = rotateImage(bitmap, 90);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotatedBitmap = rotateImage(bitmap, 180);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotatedBitmap = rotateImage(bitmap, 270);
                                    break;
                            }
                            // imageView를 Bitmap형식의 이미지로 설정
                            image.setImageBitmap(rotatedBitmap);
                            // TFlite객체 생성
                            TFlite lite = new TFlite(getContext());
                            // Interpreter를 통해 tflite파일 모델을 불러옴
                            Interpreter tflite = lite.getTfliteInterpreter("model_unquant.tflite");
                            // ByteBuffer를 2차원 형태로 생성하는데 tflite모델의 출력 수만큼 행의 갯수를 정의하고 나머지는 []
                            ByteBuffer[][] outputs = new ByteBuffer[tflite.getOutputTensorCount()][6];
                            Log.i("TensorFlow count : ", tflite.getOutputTensorCount() + "");
                            float[][] outputs2 = new float[1][6];


                            // tflite를 실행 인자(인자1 : 전달할 데이터, 인자2 : 출력된 데이터를 받을 데이터)
                            tflite.run(convertColorBitmapToFloatArray(rotatedBitmap), outputs2);
                            Log.i("AI Result1 : ", outputs2[0][0] + "");
                            Log.i("AI Result2 : ", outputs2[0][1] + "");
                            Log.i("AI Result3 : ", outputs2[0][2] + "");
                            Log.i("AI Result4 : ", outputs2[0][3] + "");
                            Log.i("AI Result5 : ", outputs2[0][4] + "");
                            Log.i("AI Result6 : ", outputs2[0][5] + "");
                        }
                    }
                    break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // Bitmap이미지를 받아 컬러형태의 int배열로 변환하는 함수
    // 4차원 배열로 batch_size, height, width, channels형태의 입력을 받아야 함
    private float[][][][] convertColorBitmapToFloatArray(Bitmap imageBitmap) {
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        float[][][][] inputArray = new float[1][height][width][3]; // 1 batch, 3 channels (RGB)

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = imageBitmap.getPixel(x, y);
                float red = ((pixel >> 16) & 0xFF) / 224.0f;
                float green = ((pixel >> 8) & 0xFF) / 224.0f;
                float blue = (pixel & 0xFF) / 255.0f;

                inputArray[0][y][x][0] = red;
                inputArray[0][y][x][1] = green;
                inputArray[0][y][x][2] = blue;
            }
        }

        return inputArray;
    }

    // 이미지 회전
    public static Bitmap rotateImage(Bitmap source, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
