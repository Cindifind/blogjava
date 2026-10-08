package com.example.demo.server;

import com.example.demo.mapper.MCServeMapper;
import com.example.demo.model.MCServe;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MCServeServe {
    private final MCServeMapper mcServeMapper;
    private static final Logger logger = LoggerFactory.getLogger(MCServeServe.class);

    public MCServeServe(MCServeMapper mcServeMapper) {
        this.mcServeMapper = mcServeMapper;
    }

    public Map<String, Object> insertMCServe(String serverId, String email, String serverAddress) {
        Map<String, Object> result = new HashMap<>();
        List<MCServe> mcServe = mcServeMapper.selectMCServeByEmail(email);
        if (mcServe.size() > 5) {
            result.put("message", "Exceeded maximum number of servers");
            return result;
        }
        if (mcServeMapper.insertMCServe(serverId, email, serverAddress) == 1) {
            result.put("message", "Insertion successful");
        } else {
            result.put("message", "Insertion failed");
        }
        return result;
    }

    public Map<String, Object> deleteMCServe(String serverId, String email) {
        Map<String, Object> result = new HashMap<>();
        if (mcServeMapper.deleteMCServe(serverId, email) == 1) {
            result.put("message", "Deletion successful");
        } else {
            result.put("message", "Deletion failed");
        }
        return result;
    }

    public Map<String, Object> updateMCServe(String serverId, String email, String serverAddress) {
        Map<String, Object> result = new HashMap<>();
        if (mcServeMapper.updateMCServe(serverId, email, serverAddress) == 1) {
            result.put("message", "Update successful");
        } else {
            result.put("message", "Update failed");
        }
        return result;
    }

    public Map<String, Object> selectMCServe(String serverId) {
        Map<String, Object> result = new HashMap<>();
        MCServe mcServe = mcServeMapper.selectMCServe(serverId);
        if (mcServe != null) {
            result.put("message", "Selection successful");
            result.put("data", mcServe);
        } else {
            result.put("message", "Selection failed");
        }
        return result;
    }

    public Map<String, Object> selectAllMCServe() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Selection successful");
        result.put("data", mcServeMapper.selectAllMCServe());
        return result;
    }

    public List<Map<String, Object>> selectMCServeByEmail(String email) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<MCServe> mcServes = mcServeMapper.selectMCServeByEmail(email);
        Map<String, List<String>> query = new HashMap<>();
        List<String> addressQuery = new ArrayList<>();
        mcServes.forEach(mcServe -> {
            String serverId = mcServe.getServerId();
            if (query.containsKey(serverId)) {
                query.get(serverId).add(mcServe.getServerAddress());
            } else {
                List<String> addresses = new ArrayList<>();
                addresses.add(mcServe.getServerAddress());
                query.put(serverId, addresses);
            }
            JSONArray addressList = new JSONArray(mcServe.getServerAddress());
            for (int i = 0; i < addressList.length(); i++) {
                addressQuery.add(addressList.getString(i));
            }
        });
        JSONArray queryResult = getQueryResult(String.join(",", addressQuery));
        Map<String, JSONObject> queryResultMap = new HashMap<>();
        if (queryResult != null) {
            for (int i = 0; i < queryResult.length(); i++) {
                JSONObject o = queryResult.getJSONObject(i);
                queryResultMap.put(o.getString("address"), o);
            }
        }
        Set<String> serverTcpIds = getServerTcpIds();
        query.forEach((serverId, addresses) -> {
            Map<String, Object> serverPlayerInfo = new HashMap<>();
            if (serverTcpIds.contains(serverId)) {
                serverPlayerInfo.put("serverPlayerInfo", Objects.requireNonNull(getPlayerInfoByServerId(serverId)).toMap());
            } else {
                serverPlayerInfo.put("serverPlayerInfo", "游戏服务器为注册到管理中心");
            }
            List<Map<String, Object>> serverInfo = new ArrayList<>();
            addresses.forEach(address -> {
                        JSONArray addressList = new JSONArray(address);
                        for (int i = 0; i < addressList.length(); i++) {
                            Map<String, Object> tempMap = new HashMap<>();
                            tempMap.put("address", addressList.getString(i));
                            tempMap.put("playerInfo", queryResultMap.get(addressList.getString(i)).toMap());
                            serverInfo.add(tempMap);
                        }
                    }
            );
            Map<String, Object> temp = new HashMap<>();
            temp.put("serverPlayerInfo", serverPlayerInfo);
            temp.put("serverInfo", serverInfo);
            temp.put("serverId", serverId);
            result.add(temp);
        });
        return result;
    }

    private JSONArray getQueryResult(String addresses) {
        HttpResponse<String> response = Unirest.get("https://muqingxi.com:2345/proxy/servers?addresses=" + addresses)
                .asString();
        try {
            return new JSONArray(response.getBody());
        } catch (Exception e) {
            logger.error("Error getQueryResult while parsing JSON response: {}", response.getBody());
            return null;
        }
    }

    private JSONObject getPlayerInfoByServerId(String serverId) {
        HttpResponse<String> response = Unirest.get("https://muqingxi.com:2345/proxy/playerstats?serverId=" + serverId)
                .asString();
        try {
            return new JSONObject(response.getBody());
        } catch (Exception e) {
            logger.error("Error getPlayerInfoByServerId while parsing JSON response: {}", response.getBody());
            return null;
        }
    }

    private Set<String> getServerTcpIds() {
        HttpResponse<String> response = Unirest.get("https://muqingxi.com:2345/proxy/serverlist")
                .asString();
        try {
            JSONObject re = new JSONObject(response.getBody());
            JSONArray list = re.getJSONArray("servers");
            Set<String> serverTcpIds = new HashSet<>();
            for (int i = 0; i < list.length(); i++) {
                serverTcpIds.add(list.getString(i));
            }
            return serverTcpIds;
        } catch (Exception e) {
            logger.error("Error getServerTcpIds while parsing JSON response: {}", response.getBody());
            return new HashSet<>();
        }
    }
}
