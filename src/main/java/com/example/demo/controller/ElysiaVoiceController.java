package com.example.demo.controller;

import com.example.demo.server.ElysiaVoiceServer;
import org.example.text.client.Client;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ElysiaVoiceController {
    @Autowired
    private ElysiaVoiceServer elysiaVoiceServer;
    @Client(address = "/user/elysiaVoice", name = "elysiaVoice")
    @PostMapping(value = "/user/elysiaVoice", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> elysiaVoice(@RequestBody String text) {
        JSONObject jsonObject = new JSONObject(text);
        byte[] voiceData = elysiaVoiceServer.getVoice(jsonObject.getString("text"));
        if (voiceData == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"voice.mp3\"")
                .body(voiceData);
    }
}