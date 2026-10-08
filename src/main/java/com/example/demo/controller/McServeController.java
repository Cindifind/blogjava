package com.example.demo.controller;

import com.example.demo.server.MCServeServe;
import com.example.demo.server.UserInfoServer;
import jakarta.servlet.http.HttpServletRequest;
import org.example.text.client.Client;
import org.json.JSONArray;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class McServeController {
    private final MCServeServe mcServeServe;
    private final UserInfoServer userInfoServer;

    public McServeController(MCServeServe mcServeServe, UserInfoServer userInfoServer) {
        this.mcServeServe = mcServeServe;
        this.userInfoServer = userInfoServer;
    }

    @GetMapping("/api/selectMCServe")
    @Client(name = "selectMCServe", address = "/api/selectMCServe")
    public ResponseEntity<Map<String, Object>> selectMCServe(String serverId) {
        return ResponseEntity.ok(mcServeServe.selectMCServe(serverId));
    }
    @PostMapping("/user/insertMCServe")
    @Client(name = "insertMCServe", address = "/user/insertMCServe")
    public ResponseEntity<Map<String, Object>> insertMCServe(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        Map<String, Object> check = checkParam(requestBody);
        if (check != null) return ResponseEntity.ok(check);
        String serverId = requestBody.get("serverId").toString();
        String serverAddress = requestBody.get("serverAddress").toString();
        return ResponseEntity.ok(mcServeServe.insertMCServe(serverId, getEmailByToken(request), serverAddress));
    }
    @PostMapping("/user/deleteMCServe")
    @Client(name = "deleteMCServe", address = "/user/deleteMCServe")
    public ResponseEntity<Map<String, Object>> deleteMCServe(@RequestParam String serverId, HttpServletRequest request) {
        String email = getEmailByToken(request);
        return ResponseEntity.ok(mcServeServe.deleteMCServe(serverId, email));
    }

    @PostMapping("/user/updateMCServe")
    @Client(name = "updateMCServe", address = "/user/updateMCServe")
    public ResponseEntity<Map<String, Object>> updateMCServe(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        Map<String, Object> check = checkParam(requestBody);
        if (check != null) return ResponseEntity.ok(check);
        String serverId = requestBody.get("serverId").toString();
        String serverAddress = requestBody.get("serverAddress").toString();
        return ResponseEntity.ok(mcServeServe.updateMCServe(serverId, getEmailByToken(request), serverAddress));
    }
    @GetMapping("/api/selectAllMCServe")
    @Client(name = "selectAllMCServe", address = "/api/selectAllMCServe")
    public ResponseEntity<Map<String, Object>> selectAllMCServe() {
        return ResponseEntity.ok(mcServeServe.selectAllMCServe());
    }
    @GetMapping("/api/selectMCServeByEmail")
    @Client(name = "selectMCServeByEmail", address = "/api/selectMCServeByEmail")
    public ResponseEntity<Map<String, Object>> selectMCServeByEmail(@RequestParam String email) {
        List<Map<String, Object>> maps = mcServeServe.selectMCServeByEmail(email);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Selection successful");
        result.put("data", maps);
        return ResponseEntity.ok(result);
    }

    private String getEmailByToken(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        token = token == null ? request.getHeader("token") : token;
        token = token.replace("Bearer ", "");
        return userInfoServer.getEmailByAccessToken(token);
    }
    private Map<String, Object> checkParam(Map<String, Object> requestBody) {
        Map<String, Object> fail = new HashMap<>();
        if (requestBody.get("serverId")==null) {
            fail.put("code", 401);
            fail.put("message", "serverId is null");
            return fail;
        }
        Object serverAddress = requestBody.get("serverAddress");
        JSONArray addressArray;
        if (serverAddress == null) {
            addressArray = new JSONArray();
        } else if (serverAddress instanceof List<?> list) {
            addressArray = new JSONArray(list);
        } else {
            try {
                addressArray = new JSONArray(serverAddress.toString());
            } catch (Exception e) {
                fail.put("code", 400);
                fail.put("message", "serverAddress is not a JSONArray");
                return fail;
            }
        }
        if (addressArray.length() > 5) {
            fail.put("code", 400);
            fail.put("message", "serverAddress length > 5");
            return fail;
        }
        // 回写为规范 JSON 字符串，避免 List.toString() 产生的无引号文本存入数据库后无法解析
        requestBody.put("serverAddress", addressArray.toString());
        return null;
    }
}
