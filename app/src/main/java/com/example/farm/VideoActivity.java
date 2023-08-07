package com.example.farm;

<<<<<<< HEAD
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
=======
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
>>>>>>> 93ceec6520bd00a5ecd3c67775aa2a6810ea6879
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
<<<<<<< HEAD
import androidx.core.content.ContextCompat;
=======
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
>>>>>>> 93ceec6520bd00a5ecd3c67775aa2a6810ea6879


import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;


public class VideoActivity extends AppCompatActivity {

    PreviewView video;
    ImageButton back_btn;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

<<<<<<< HEAD
=======
    private static final int REQUEST_CAMERA_PERMISSION = 200;

>>>>>>> 93ceec6520bd00a5ecd3c67775aa2a6810ea6879
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_layout);

<<<<<<< HEAD

        video = findViewById(R.id.viewFinder);

        // ProcessCameraProvider는 카메라 제공자
=======
        video = findViewById(R.id.viewFinder);

        // 카메라 권한 설정이 되지 않은 경우
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            setupCamera();
        }
    }

    private void setupCamera() {
>>>>>>> 93ceec6520bd00a5ecd3c67775aa2a6810ea6879
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
<<<<<<< HEAD
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, getExecutor());

    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {

        cameraProvider.unbindAll();
=======
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
>>>>>>> 93ceec6520bd00a5ecd3c67775aa2a6810ea6879

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

<<<<<<< HEAD
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(video.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture);
    }
}
=======
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
>>>>>>> 93ceec6520bd00a5ecd3c67775aa2a6810ea6879
