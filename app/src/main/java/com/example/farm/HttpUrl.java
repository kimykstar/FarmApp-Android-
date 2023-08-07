package com.example.farm;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HttpUrl {

    private String url;

    public HttpUrl(){
        this.url = "http://43.201.66.244:8081/";
    }

    public String getUrl(){
        return url;
    }

    public String[] getEncodeingURLParam(String[] params){

        for(int i = 0; i < params.length; i++){
            try{
                params[i] = URLEncoder.encode(params[i], StandardCharsets.UTF_8.toString());
            }catch(Exception e){
                e.printStackTrace();
            }

        }

        return params;
    }

    public String getEncodeURLParam(String param){
        try{
            param = URLEncoder.encode(param, StandardCharsets.UTF_8.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
        return param;
    }
}
