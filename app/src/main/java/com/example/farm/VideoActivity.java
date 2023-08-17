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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;


public class VideoActivity extends AppCompatActivity {

    PreviewView video;
    private ImageView capture_img;
    private Button capture_btn;
    ProcessCameraProvider cameraProvider;
    private ImageButton back_btn;

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

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // 카메라 권한 설정이 되지 않은 경우
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            setupCamera();
        }

        // 1초마다 주기적으로 Bitmap이미지를 받는다. tflite로 파일 전달해야함
        TimerTask tt = new TimerTask(){
            @Override
            public void run(){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        capture_img.setImageBitmap(video.getBitmap());
                        // imageView를 Bitmap형식의 이미지로 설정
                        capture_img.setImageBitmap(video.getBitmap());
                        // TFlite객체 생성
                        TFlite lite = new TFlite(getApplicationContext());
                        // Interpreter를 통해 tflite파일 모델을 불러옴
                        Interpreter tflite = lite.getTfliteInterpreter("model_unquant.tflite");
                        // ByteBuffer를 2차원 형태로 생성하는데 tflite모델의 출력 수만큼 행의 갯수를 정의하고 나머지는 []
                        ByteBuffer[][] outputs = new ByteBuffer[tflite.getOutputTensorCount()][6];
                        Log.i("TensorFlow count : ", tflite.getOutputTensorCount() + "");
                        float[][] outputs2 = new float[1][6];
                    }
                });
            }
        };

        Timer timer = new Timer();
        tt.run();
        timer.schedule(tt, 0, 1000);

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


}