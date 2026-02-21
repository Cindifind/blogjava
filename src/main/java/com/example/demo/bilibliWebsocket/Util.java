package com.example.demo.bilibliWebsocket;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Util {
    public static String getRoomAddressJson(String url, int roomId, String cookie) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .setHeader("Referer", "https://live.bilibili.com/" + roomId)
                .setHeader("User-Agent", "Mozilla/5.0")
                .setHeader("Cookie", cookie)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
