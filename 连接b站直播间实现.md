# B站直播间弹幕监听系统：从零实现弹幕实时捕获

你是否曾经想过如何实时获取B站直播间的弹幕？本文将详细介绍如何从零开始构建一个完整的B站直播间弹幕监听系统。该系统可以连接到指定的B站直播间，实时接收弹幕消息并通过WebSocket广播给用户客户端。我们将深入探讨与B站弹幕服务器的连接、身份认证、心跳维持以及弹幕消息的解析和转发等核心技术。

## 项目亮点

- 实现了完整的B站弹幕协议解析
- 支持多房间同时连接和监听
- 采用WebSocket双向通信技术
- 实现了高效的会话管理和连接生命周期控制
- 具备自动重连和错误处理机制

## 系统整体架构

本系统采用分层架构设计，主要包含以下几个核心组件：

1. **用户接入层** - UserWebSocketHandler：处理用户WebSocket连接请求
2. **服务管理层** - BilibiliWebSocketService：核心服务类，管理房间连接和用户会话
3. **客户端层** - BilibiliClient：负责与B站服务器建立连接
4. **协议处理层** - BilibiliRoomWebSocketHandler：处理B站弹幕协议
5. **工具层** - 各类工具类：提供协议签名、数据解析等功能

## 详细连接流程与实现步骤

### 1. 用户连接流程详解
用户连接是我们系统的入口点，整个过程如下：

1. **建立WebSocket连接**：用户首先通过WebSocket连接到我们的系统，这由UserWebSocketHandler处理
2. **发送连接指令**：用户发送格式为`connect:房间号:cookie`的文本消息
3. **参数解析**：系统解析房间号和cookie参数，如果cookie为空则使用默认管理员cookie
4. **触发B站连接**：调用BilibiliWebSocketService的connectToRoom方法开始连接B站
5. **建立连接与认证**：系统获取弹幕服务器地址，建立WebSocket连接并发送认证包
6. **反馈结果**：连接成功后向用户发送确认消息，并开始转发弹幕

### 2. B站连接与认证详细步骤
B站连接和认证是系统的核心环节，具体步骤如下：

1. **获取服务器地址**：通过BiliBiliTool.buildDanmuInfoUrl(roomId)构建带WBI签名的请求URL
2. **发送HTTP请求**：使用Util.getRoomAddressJson获取弹幕服务器地址信息
3. **解析响应数据**：将响应JSON转换为Address对象，提取host、port和token信息
4. **构建认证包**：创建包含用户ID、房间ID、buvid等信息的Package对象
5. **序列化认证数据**：使用Package.createDataPackage方法创建认证数据包
6. **建立WebSocket连接**：使用Spring WebSocket客户端连接到B站服务器
7. **发送认证包**：连接建立后立即发送认证数据包完成身份验证

### 3. 心跳维持机制详解
为了保持与B站服务器的长连接，系统实现了心跳机制：

1. **定时任务**：BilibiliWebSocketService初始化时创建30秒间隔的定时任务
2. **创建心跳包**：使用Package.createDataPackage(2, new byte[0])创建心跳数据包
3. **发送心跳**：遍历所有已连接的客户端，向每个打开的连接发送心跳包
4. **处理回应**：在BilibiliRoomWebSocketHandler中处理服务器的心跳回应

### 4. 弹幕消息处理流程
当B站服务器发送弹幕消息时，系统按以下步骤处理：

1. **接收二进制消息**：BilibiliRoomWebSocketHandler.handleMessage处理BinaryMessage
2. **解包数据**：调用unpack方法解析二进制数据包
3. **处理压缩包**：如果数据包是zlib压缩的，则先解压缩
4. **解析JSON**：将解包后的数据解析为JSON对象
5. **提取弹幕**：从JSON中提取用户名和弹幕内容
6. **广播消息**：将格式化后的弹幕消息广播给所有订阅该房间的用户

## 核心模块实现细节

### BilibiliWebSocketService - 核心服务类
这是系统的中枢组件，负责管理所有房间连接和用户会话：
- 使用ConcurrentHashMap维护房间ID到客户端的映射关系
- 管理用户会话和房间的关联
- 定时发送心跳包保持连接
- 自动处理连接的建立和断开
- 实现了连接的生命周期管理，当房间没有用户监听时自动断开连接

**连接管理方法**：
```java
public void connectToRoom(int roomId, String cookie, WebSocketSession userSession) throws Exception {
    // 获取或创建该房间的客户端
    BilibiliClient client = roomClients.computeIfAbsent(roomId, k -> {
        // 创建客户端逻辑
    });
    
    // 将用户会话添加到该房间的监听者列表
    CopyOnWriteArraySet<WebSocketSession> sessions = roomUserSessions.computeIfAbsent(roomId, k -> {
        return new CopyOnWriteArraySet<>();
    });
    
    // 建立反向映射并连接
    sessionToRoomMap.put(userSession, roomId);
    if (!client.isOpen()) {
        client.connect(); // 建立B站连接
    }
}
```

### BilibiliClient - B站客户端
负责与B站弹幕服务器建立和维护连接：
- 通过BiliBiliTool获取带签名的弹幕服务器地址
- 构建认证数据包完成身份验证
- 实现了心跳包发送机制
- 使用Spring WebSocket客户端建立连接

**核心连接方法**：
```java
public void connect() throws Exception {
    // 1. 获取服务器地址
    String url = BiliBiliTool.buildDanmuInfoUrl(roomId);
    Address address = JSON.readValue(Util.getRoomAddressJson(url, roomId, cookie), Address.class);
    
    // 2. 提取连接参数
    int port = address.getData().getHostList().get(0).getWssPort();
    String host = address.getData().getHostList().get(0).getHost();
    String token = address.getData().getToken();
    
    // 3. 构建认证包
    URI uri = new URI("wss://" + host + ":" + port + "/sub");
    Package pkg = new Package(/* 参数 */);
    byte[] dataPackage = Package.createDataPackage(7, JSON.writeValueAsString(pkg).getBytes(StandardCharsets.UTF_8));
    
    // 4. 建立连接并发送认证包
    WebSocketClient client = new StandardWebSocketClient();
    CompletableFuture<WebSocketSession> future = client.execute(handler, new WebSocketHttpHeaders(), uri);
    this.session = future.get();
    
    if (session.isOpen()) {
        session.sendMessage(new org.springframework.web.socket.BinaryMessage(dataPackage));
    }
}
```

### BilibiliRoomWebSocketHandler - 协议处理器
处理来自B站服务器的消息：
- 解析二进制弹幕数据包
- 解压缩zlib压缩的数据包
- 提取弹幕内容并广播给订阅用户
- 实现了完整的B站弹弹幕协议解析

**消息处理方法**：
```java
@Override
public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
    if (message instanceof BinaryMessage) {
        ByteBuffer bytes = ((BinaryMessage) message).getPayload();
        String content = unpack(bytes);
        if (content != null && content.charAt(0) == '{') {
            JSONObject jsonObject = new JSONObject(content);
            if (jsonObject.has("info")) {
                JSONArray array = jsonObject.getJSONArray("info");
                String username = array.getJSONArray(2).get(1).toString();
                String danmu = array.get(1).toString();
                // 广播弹幕给所有监听此房间的用户
                broadcastToUsers("[弹幕]" + username + "说: " + danmu);
            }
        }
    }
}
```

### UserWebSocketHandler - 用户接入点
处理用户连接请求：
- 接收用户连接请求
- 解析连接指令(`connect:房间号:cookie`)
- 管理用户会话生命周期
- 使用默认Cookie保证基本连接能力

**连接处理方法**：
```java
@Override
protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();
    if (payload.startsWith("connect:")) {
        // 解析格式: connect:房间号:cookie
        String[] parts = payload.split(":", 3);
        if (parts.length == 3) {
            try {
                int roomId = Integer.parseInt(parts[1]);
                String cookie = parts[2];
                if (cookie.isEmpty()) {
                    cookie = ADMIN_COOKIE;
                }
                // 连接到指定的B站直播间
                bilibiliService.connectToRoom(roomId, cookie, session);
            } catch (Exception e) {
                session.sendMessage(new TextMessage("连接失败: " + e.getMessage()));
            }
        }
    }
}
```

## 核心技术原理解析

### 1. B站弹幕协议详解
B站弹幕协议使用二进制数据包格式，每个数据包都有固定的结构：

```
Offset(Bytes)  Length(Bytes)  Field        Note
0              4              Packet Length  数据包长度
4              2              Header Length  数据包头部长度 固定为16
6              2              Protocol Version  版本号 0普通包 1心跳包 2zlib压缩包
8              4              Operation   操作类型 2心跳 3心跳回应 5普通消息 7认证包 8认证回应
12             4              Sequence    可丢弃 递增的序列号
16             -              Body        数据体
```

**协议关键点**：
- Packet Length：整个数据包的长度，包括头部和数据体
- Header Length：固定为16字节的头部长度
- Protocol Version：协议版本，0表示普通包，1表示心跳包，2表示zlib压缩包
- Operation：操作类型，不同值代表不同含义
- 数据体：根据Operation类型包含不同的内容

### 2. WBI签名机制解析
为了访问B站的弹幕服务器信息接口，需要实现WBI签名机制，这是连接过程中的关键步骤：

1. **获取密钥**：从B站接口获取img_key和sub_key
2. **生成mixin_key**：根据固定的索引数组生成mixin_key
3. **添加时间戳**：添加wts时间戳参数
4. **参数排序**：对所有参数按键名进行排序
5. **拼接参数**：将参数拼接成key=value格式并用&连接
6. **计算签名**：对拼接后的字符串和mixin_key组合进行MD5计算得到w_rid

**核心代码**：
```java
public static Map<String, String> wbiSign(Map<String, String> params, BiliBiliSecretKey secret) {
    // 1. 生成 mixin_key
    String key = secret.getImgKey() + secret.getSubKey();
    String mixinKey = Arrays.stream(ENCRYPT_INDEX)
            .mapToObj(i -> String.valueOf(key.charAt(i)))
            .collect(Collectors.joining())
            .substring(0, 32);

    // 2. 添加 wts 时间戳
    params.put("wts", String.valueOf(System.currentTimeMillis() / 1000));

    // 3. 排序并拼接参数
    String query = params.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> encodeParam(entry.getKey()) + "=" + encodeParam(entry.getValue()))
            .collect(Collectors.joining("&"));

    // 4. 计算 w_rid (MD5)
    String w_rid = DigestUtils.md5Hex(query + mixinKey);

    // 5. 返回签名后的参数
    Map<String, String> signedParams = new HashMap<>(params);
    signedParams.put("w_rid", w_rid);
    return signedParams;
}
```

### 3. 多房间多用户管理机制
系统支持同时连接多个房间，每个房间可以有多个用户监听：
- 使用ConcurrentHashMap存储房间连接信息
- 使用CopyOnWriteArraySet管理用户会话，保证线程安全
- 实现会话与房间的双向映射，便于快速查找和清理
- 当房间无用户监听时自动断开连接，节约资源

**连接管理核心代码**：
```java
// 存储房间ID到B站客户端的映射
private final ConcurrentHashMap<Integer, BilibiliClient> roomClients = new ConcurrentHashMap<>();

// 存储房间ID到用户WebSocket会话的映射
private final ConcurrentHashMap<Integer, CopyOnWriteArraySet<WebSocketSession>> roomUserSessions = new ConcurrentHashMap<>();

// 存储用户WebSocket会话到其所属房间的反向映射
private final ConcurrentHashMap<WebSocketSession, Integer> sessionToRoomMap = new ConcurrentHashMap<>();
```

## 核心代码示例

### 1. Bilibili 客户端连接代码

以下展示了 BilibiliClient 中建立连接的核心代码：

```java
public void connect() throws Exception {
    String url = BiliBiliTool.buildDanmuInfoUrl(roomId);
    //使用Util.getRoomAddressJson()方法获取地址，并使用readValue方法将JSON转成Address对象
    Address address = JSON.readValue(Util.getRoomAddressJson(url, roomId, cookie), Address.class);
    //获取地址主要信息
    int port = address.getData().getHostList().get(0).getWssPort();
    String host = address.getData().getHostList().get(0).getHost();
    String token = address.getData().getToken();
    Map<String, String> map = Datacenter.getCookieMap(cookie);
    //连接并发送心跳包
    URI uri = new URI("wss://" + host + ":" + port + "/sub");
    Package pkg = new Package(
            Long.parseLong(map.get("DedeUserID")),
            roomId,
            2,
            "buvid3="+map.get("buvid3"),
            "web",
            2,
            token);
    //创建鉴权包
    byte[] dataPackage = Package.createDataPackage(7, JSON.writeValueAsString(pkg).getBytes(StandardCharsets.UTF_8));
    //创建客户端
    WebSocketClient client = new StandardWebSocketClient();

    // 使用execute方法连接
    CompletableFuture<WebSocketSession> future = client.execute(handler, new WebSocketHttpHeaders(), uri);
    this.session = future.get();

    // 发送认证包
    if (session.isOpen()) {
        session.sendMessage(new org.springframework.web.socket.BinaryMessage(dataPackage));
        log.info("房间 {} 鉴权成功", roomId);
    }
}
```

### 2. 弹幕协议解析代码

BilibiliRoomWebSocketHandler 中解析弹幕消息的核心代码：

```java
public static String unpack(ByteBuffer byteBuffer) {
    try {
        if (byteBuffer.remaining() < 16) {
            log.warn("数据包不完整，需要等待更多数据");
            return null;
        }
        int packageLen = byteBuffer.getInt();
        short headLength = byteBuffer.getShort();
        short packageVer = byteBuffer.getShort();
        int optCode = byteBuffer.getInt();
        int sequence = byteBuffer.getInt();

        if (2 == optCode) {
            log.info("这是服务器心跳回复");
        }

        byte[] contentBytes = new byte[packageLen - headLength];
        byteBuffer.get(contentBytes);

        // 如果是zip包就进行解包
        if (2 == packageVer) {
            return unpack(ByteBuffer.wrap(ZipUtil.unZlib(contentBytes)));
        }

        String content = new String(contentBytes, StandardCharsets.UTF_8);

        if (5 == optCode) {
            return content;
        }

        if (byteBuffer.position() < byteBuffer.limit()) {
            return unpack(byteBuffer);
        }

        return content;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}
```

### 3. WBI 签名算法(w_rid部分)

BiliBiliTool 中实现的 WBI 签名算法：

```java
public static Map<String, String> wbiSign(Map<String, String> params, BiliBiliSecretKey secret) {
    // 1. 生成 mixin_key
    String key = secret.getImgKey() + secret.getSubKey();
    String mixinKey = Arrays.stream(ENCRYPT_INDEX)
            .mapToObj(i -> String.valueOf(key.charAt(i)))
            .collect(Collectors.joining())
            .substring(0, 32);

    // 2.html. 添加 wts 时间戳
    params.put("wts", String.valueOf(System.currentTimeMillis() / 1000));

    // 3. 排序并拼接参数
    String query = params.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> encodeParam(entry.getKey()) + "=" + encodeParam(entry.getValue()))
            .collect(Collectors.joining("&"));

    // 4. 计算 w_rid (MD5)
    String w_rid = DigestUtils.md5Hex(query + mixinKey);

    // 5. 返回签名后的参数
    Map<String, String> signedParams = new HashMap<>(params);
    signedParams.put("w_rid", w_rid);
    return signedParams;
}
```

### 4. 数据包构造

Package 类中创建数据包的方法：

```java
public static byte[] createDataPackage(int operation, byte[] data) {
    int length = data.length + 16;
    ByteBuffer buffer = ByteBuffer.allocate(length);
    buffer.putInt(length);
    buffer.putShort((short) 16);
    buffer.putShort((short) 1);
    buffer.putInt(operation);
    buffer.putInt(1);
    buffer.put(data);
    return buffer.array();
}
```

## 部署与使用

### 环境要求
- Java 8 或更高版本
- Maven 项目构建工具
- Spring Boot 框架
- 支持 WebSocket 的 Web 服务器

### 使用方法
1. 用户通过 WebSocket 连接到服务
2. 发送连接指令: `connect:房间号:cookie`
3. 如果 cookie 为空，将使用默认的管理员 cookie
4. 系统将自动连接到指定的 Bilibili 直播间
5. 弹幕消息将以 `[弹幕]用户名说: 内容` 的格式广播给所有订阅用户

### 注意事项

1. 需要有效的 Bilibili Cookie 才能连接到某些直播间
2. 默认使用管理员 Cookie，但建议为每个连接提供特定的 Cookie
3. 系统会自动管理连接的生命周期，当没有用户监听某个房间时会自动断开连接
4. 心跳包每30秒自动发送一次以维持连接

## 总结

本项目完整实现了 Bilibili 弹幕监听系统的全部功能，包括协议解析、连接管理、消息转发等核心功能。通过分层架构设计，系统具备良好的可扩展性和维护性，可以作为学习 WebSocket 通信、协议解析和实时消息处理的优秀案例。