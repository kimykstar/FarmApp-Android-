package com.example.farm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

// Server와 HTTP통신을 위한 클래스
// 이거는 AsyncTask상속받아서 doInBackground메소드 오버라이드 해서 HTTP통신 해야 함
// 무조건 백그라운드로 실행해야 오류 안남
// 이 클래스 사용법
// 1. 생성자로 Server url설정
// 2. setHeader를 통해 Header설정
// 3. readData or writeData메소드를 통해 서버와 데이터 주고받음
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

    // HTTP 헤더 설정
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

    // Header를 설정한 Connection을 통해 Server로부터 데이터를 읽어온다.
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

    // 서버로부터 받은 String형태를 Fruit객체의 필드에 맞게 변환하여 Fruit자료형으로 반환해주는 메소드
    public Fruit parseStringToFruit(String st){
        Fruit fruit = new Fruit();
        String[] elements = st.split(",");

        fruit.setFruit_name(elements[0].split(":")[1]);
        fruit.setCalories(elements[1].split(":")[1]);
        fruit.setCarbohydrate(elements[2].split(":")[1]);
        fruit.setProtein(elements[3].split(":")[1]);
        fruit.setFat(elements[4].split(":")[1]);
        fruit.setSugar(elements[5].split(":")[1]);

        return fruit;
    }

    // Header를 설정한 Connection을 통해 Server로 데이터를 보낸다.
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
