package com.example.farm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class HttpConnection {

    private String link;
    private HttpURLConnection conn;

    // url을 생성 시 입력받아 connection을 생성
    public HttpConnection(String link){
        this.link = link;
        try{
            URL url = new URL(link);
            conn = (HttpURLConnection) url.openConnection();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // 헤더 설정
    public void setHeader(int time, String method, boolean output, boolean input){
        conn.setConnectTimeout(time);
        conn.setRequestProperty("Content-Type", "UTF-8");
        try{
            conn.setRequestMethod(method);
        }catch(ProtocolException e){
            e.printStackTrace();
        }
        conn.setDoInput(input);
        conn.setDoOutput(output);
    }

    // inputStream
    public String readData(){
        String result = "";
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            while((line = reader.readLine()) != null){
                result += line;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }

    // outputStream
    public boolean writeData(String data){
        try{
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(data.getBytes("UTF-8"));
            outputStream.flush();
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
