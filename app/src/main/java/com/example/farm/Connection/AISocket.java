package com.example.farm.Connection;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.farm.HttpUrl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class AISocket {
    private static Socket socket;
    private static DataOutputStream outputStream;
    private static DataInputStream inputStream;

    public AISocket(){
        try {
            // Socket및 input, outputstream 생성
//            socket = new Socket("43.200.3.29", 5800);
            socket = new Socket("192.168.35.73", 8081);

            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
            if(inputStream != null)
                Log.i("input생성 완료", "");
            Log.i("Socket 생성 ", "완료");
        }catch(Exception e){
            if(socket != null)
                disconnect();
            e.printStackTrace();
        }
    }


    // 서버 소켓으로부터 출력데이터를 받아온다.
    public byte communication(Bitmap image){
        byte result = 1;
        ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream();

        try {
            Log.i("이미지 압축 및 저장", "시작");
            image.compress(Bitmap.CompressFormat.PNG, 100, imageOutputStream);
            byte[] bytes = imageOutputStream.toByteArray(); // 바이트 배열로 변환
            int buffsize = bytes.length;
            Log.i("image Length : ", buffsize + "");
            BufferedOutputStream bos = new BufferedOutputStream(outputStream, buffsize);
            Log.i("이미지 압축 및 저장", "끝 파일 길이 전달");

            Log.i("소켓 이미지 전송 시작 : ", "start");
            bos.write(bytes);

            bos.flush();
            Log.i("소켓 이미지 전송 완료 : ", "complete");

            result = (byte)inputStream.read();
            Log.i("소켓 통신 결과 데이터 : ", result + "");
            bos.close();
            disconnect();
            Log.i("Input Data : ", result + " ");
        }catch(Exception e){
            disconnect();
            e.printStackTrace();
        }

        return result;
    }

    public String readString(DataInputStream dis) throws IOException{
        int length = dis.readInt();
        byte[] data = new byte[length];
        dis.readFully(data, 0, length);
        String text = new String(data, StandardCharsets.UTF_8);
        return text;
    }

    // socket통신 해제
    public void disconnect(){
        try {
            socket.close();
            outputStream.close();
            inputStream.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
