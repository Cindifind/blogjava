package com.example.demo.util;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONObject;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        String url = "http://localhost:8080/register?code=123456";
        JSONObject requestBody = new JSONObject();
        requestBody.put("userName", "user2");
        requestBody.put("password", "123456");
        requestBody.put("sex", "男");
        requestBody.put("workUnit", "springBoot");
//        requestBody.put("phone", "13800000000");
        String url1 = "http://localhost:8080/login";
        JSONObject requestBody1 = new JSONObject();
        requestBody1.put("userName", "user");
        requestBody1.put("password", "123456");
        String url2 = "http://localhost:8080/sendCode";
        HttpResponse<String> postResponse = Unirest.post(url1)
                .header("Content-Type", "application/json")
                .body(requestBody1.toString())
                .asString();
//        HttpResponse<String> getResponse = Unirest.get(url2)
//                .header("Content-Type", "application/json")
//                .asString();
//        System.out.println(getResponse.getBody());
        System.out.println(postResponse.getBody());

    }
}