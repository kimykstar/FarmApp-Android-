package com.example.farm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.internal.utils.ImageUtil;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;


import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;


public class VideoActivity extends AppCompatActivity {

    private PreviewView video;
    private ImageView capture_img;
    private Button capture_btn;
    private ProcessCameraProvider cameraProvider;
    private ImageButton back_btn;
    private Timer timer;
    private ByteBuffer inputBuffer;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private static final int REQUEST_CAMERA_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_layout);

        video = findViewById(R.id.viewFinder);
        capture_img = findViewById(R.id.capture_img);
        capture_btn = findViewById(R.id.capture_btn);
        back_btn = findViewById(R.id.back_btn);

        // Preview layout크기 조절
        int screenwidth = (int)(getResources().getDisplayMetrics().widthPixels * 0.8);
        video.getLayoutParams().width = screenwidth;
        video.getLayoutParams().height = screenwidth;

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                finish();
            }
        });
        // 카메라 권한 설정이 되지 않은 경우
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            setupCamera();
        }

        // Thread작동 전에 ByteBuffer를 초기화 해놓음 실행하면서 계속 할당하지 않도록
        // 입력 버퍼의 크기를 Direct방식으로 이용할 height : 224, width : 224, channel : 3, byte : 4
        // 만큼의 크기를 할당한다.
        inputBuffer = ByteBuffer.allocateDirect(224 * 224 * 3 * 4);
        inputBuffer.order(ByteOrder.nativeOrder());

        // 1초마다 주기적으로 Bitmap이미지를 받는다. tflite로 파일 전달해야함
        TimerTask tt = new TimerTask(){
            @Override
            public void run(){
                // UI작업은 원래 mainThread에서만 가능한데 다른 Thread에서 사용 가능하도록 하는 것
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // imageView를 Bitmap형식의 이미지로 설정
                        capture_img.setImageBitmap(video.getBitmap());
                        Bitmap image = video.getBitmap();
                        if(image != null) {
                            // TFlite객체 생성
                            TFlite lite = new TFlite(getApplicationContext());
                            // Interpreter를 통해 tflite파일 모델을 불러옴
                            Interpreter tflite = lite.getTfliteInterpreter("model_unquant.tflite");
                            inputBuffer.rewind();
                            float[][] outputs2 = new float[1][6];
                            tflite.run(inputBuffer, outputs2);
                            Log.i("AI Result1 : ", String.format("%.2f", outputs2[0][0]) + "");
                            Log.i("AI Result2 : ", String.format("%.2f", outputs2[0][1]) + "");
                            Log.i("AI Result3 : ", String.format("%.2f", outputs2[0][2]) + "");
                            Log.i("AI Result4 : ", String.format("%.2f", outputs2[0][3]) + "");
                            Log.i("AI Result5 : ", String.format("%.2f", outputs2[0][4]) + "");
                            Log.i("AI Result6 : ", String.format("%.2f", outputs2[0][5]) + "");
                        }else{
                            Log.e("Image is Null", "");
                        }
                    }
                });
            }
        };

        timer = new Timer();
        tt.run();
        timer.schedule(tt, 0, 200);

        // output데이터가 존재하면 Barchart를 그려서 신선도를 보여줌 Fragment를 띄우자

    }

    private void setupCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        video.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        video.setScaleType(PreviewView.ScaleType.FIT_CENTER);
    }

    // Preview위젯에 cameraprovider를 bind한다.
    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        // 카메라를 선택함
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(video.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);
    }

    // requestPermission메소드를 실행하여 Permission요청 시 자동으로 동작하는 함수
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 부여되었을 때
                setupCamera();
            } else {
                // 권한이 거부되었을 때
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                finish(); // 앱 종료 또는 다른 조치를 취할 수 있습니다.
            }
        }
    }

    // Bitmap사진을 TFlite파일의 입력 데이터 형식으로변환하는 함수
    public ByteBuffer bitmapToByteBuffer(Bitmap temp){
        // 매개변수로 받은 사진의 크기를 변경함 --> tflite파일에서 요구하는 사이즈로 변환한 것
        Bitmap photo = Bitmap.createScaledBitmap(temp, 224, 224, true);


        // Bitmap형식의 사진으로부터 pixel값을 int[]자료형에 저장한다.
        int[] pixels = new int[224 * 224];
        photo.getPixels(pixels, 0, 224, 0, 0, 224, 224);

        inputBuffer.rewind(); // ByteBuffer의 index를 처음으로 돌림
        int pixelIndex = 0;
        for(int row = 0; row < 224; row++){
            for(int col = 0; col < 224; col++){
                final int pixel = pixels[pixelIndex++];
                float r = ((pixel >> 16) & 0xFF) / 255.0f;
                float g = ((pixel >> 8) & 0xFF) / 255.0f;
                float b = (pixel & 0xFF) / 255.0f;

                inputBuffer.putFloat(r);
                inputBuffer.putFloat(g);
                inputBuffer.putFloat(b);
            }
        }

        return inputBuffer;
    }


}