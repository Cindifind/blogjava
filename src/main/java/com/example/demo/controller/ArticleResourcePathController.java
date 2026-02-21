package com.example.demo.controller;

import com.example.demo.auth.mapper.UserInfoMapper;
import com.example.demo.mapper.ImageRecordsMapper;
import com.example.demo.model.ArticleResourcePath;
import com.example.demo.model.ImageRecords;
import com.example.demo.server.ArticleResourcePathServer;
import com.example.demo.util.GetNotFoundResources;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.example.text.client.Client;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class ArticleResourcePathController {

    @Autowired
    private ArticleResourcePathServer articleResourcePathServer;
    @Autowired
    private ImageRecordsMapper imageRecordsMapper;
    private GetNotFoundResources getNotFoundResources = new GetNotFoundResources();
    private static final Logger log = LoggerFactory.getLogger(ArticleResourcePathController.class);
    /**
     * 上传文章资源
     *
     * @param image       图片文件
     * @param md          Markdown文件
     * @param title       标题
     * @param label       标签
     * @param description 描述
     * @return 上传结果
     */
    @Client(address = "/admin/upload", name = "upload")
    @PostMapping("/admin/upload")
    public ResponseEntity<Map<String, Object>> uploadArticle(
            @RequestParam("image") MultipartFile image,
            @RequestParam("md") MultipartFile md,
            @RequestParam("email") String email,
            @RequestParam("title") String title,
            @RequestParam("label") String label,
            @RequestParam("description") String description) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 创建临时文件
            File tempImageFile = convertToFile(image);
            File tempMdFile = convertToFile(md);

            // 创建文章资源对象
            ArticleResourcePath article = new ArticleResourcePath();
            article.setEmail(email);
            article.setTitle(title);
            article.setLabel(label);
            article.setDescription(description);

            // 保存文章资源
            articleResourcePathServer.insertArticleResourcePath(article, tempImageFile, tempMdFile);

            result.put("code", 200);
            result.put("message", "文章上传成功");
            result.put("data", article);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "文章上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @Client(address = "/admin/mdImage", name = "mdImage")
    @PostMapping("/admin/mdImage")
    public ResponseEntity<Map<String, Object>> uploadMdImage(@RequestParam("image") MultipartFile image) {
        Map<String, Object> result = new HashMap<>();
        try {
            File tempImageFile = convertToFile(image);
            articleResourcePathServer.insertMdImage(tempImageFile);
            result.put("code", 200);
            result.put("message", "图片上传成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "图片上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Client(address = "/admin/getMdImageList", name = "getMdImageList")
    @GetMapping("/admin/getMdImageList")
    public ResponseEntity<Map<String, Object>> getImageList() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取图片列表成功");
        result.put("data", articleResourcePathServer.getMdImageNames());
        return ResponseEntity.ok(result);
    }

    @Client(address = "/admin/deleteMdImage", name = "deleteMdImage")
    @PostMapping("/admin/deleteMdImage")
    public ResponseEntity<Map<String, Object>> deleteMdImage(@RequestParam String mdImageName) {
        Map<String, Object> result = new HashMap<>();
        try {

            articleResourcePathServer.deleteMdImage(mdImageName);
            result.put("code", 200);
            result.put("message", "图片删除成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "图片删除失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
        return ResponseEntity.ok(result);
    }
    /**
     * 分页获取文章列表
     *
     * @param pageNum 页码（从0开始）
     * @return 文章列表
     */
    @Client(address = "/page", name = "page")
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getArticlesByPage(@RequestParam int pageNum) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ArticleResourcePath> articles = articleResourcePathServer.findArticleByPageNum(pageNum);
            // 使用线程池并行处理QQ昵称获取
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (ArticleResourcePath article : articles) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        HttpResponse<String> name = Unirest.get("https://users.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins=" + article.getEmail().replaceAll("@qq.com", ""))
                                .header("Accept", "application/vnd.github.v3+json")
                                .asString();
                        JSONArray QQname = new JSONArray(new JSONObject(name.getBody().replaceAll(".*(\\{.*})\\).*", "$1")).getJSONArray(article.getEmail().replaceAll("@qq.com", "")));
                        article.setAuthor(QQname.getString(6));
                    } catch (Exception e) {

                        log.info("非qq邮箱登录{}", article.getEmail());
                        article.setAuthor("未知");

                    }
                }, executor);
                futures.add(future);
            }
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            // 关闭线程池
            executor.shutdown();
            result.put("code", 200);
            result.put("message", "获取文章列表成功");
            result.put("data", articles);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取文章列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 获取文章总数
     *
     * @return 文章总数
     */
    @Client(address = "/count", name = "count")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getArticleCount() {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = articleResourcePathServer.getArticleCount();
            result.put("code", 200);
            result.put("message", "获取文章总数成功");
            result.put("data", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取文章总数失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 根据时间戳删除文章
     *
     * @param timestamp 文章时间戳
     * @return 删除结果
     */
    @Client(address = "/admin/delete", name = "delete")
    @GetMapping("/admin/delete")
    public ResponseEntity<Map<String, Object>> deleteArticle(@RequestParam long timestamp) {
        Map<String, Object> result = new HashMap<>();
        try {
            articleResourcePathServer.deleteArticleResourcePath(timestamp);
            result.put("code", 200);
            result.put("message", "文章删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "文章删除失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @Client(address = "/image", name = "image")
    @GetMapping("/image")
    public ResponseEntity<Resource> getImageFile(@RequestParam String filename) {
        try {
            String image;
            if (!filename.contains("md")) {
                image = articleResourcePathServer.getImage(filename);
            } else {
                image = imageRecordsMapper.getImage(filename);
            }
            if (image == null) {
                return ResponseEntity.status(410).build();
            }
            File file = articleResourcePathServer.getImageFile(filename);
            Resource resource = new FileSystemResource(file);
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // 根据实际图片类型设置
                        .body(resource);
            } else {
                getNotFoundResources.getNotFoundImages(filename);
                return ResponseEntity.status(506).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Client(address = "/md", name = "md")
    @GetMapping("/md")
    public ResponseEntity<Resource> getMdFile(@RequestParam String filename) {
        try {
            String md = articleResourcePathServer.getMd(filename);
            if (md == null) {
                return ResponseEntity.status(410).build();
            }
            File file = articleResourcePathServer.getMdFile(filename);
            Resource resource = new FileSystemResource(file);
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_MARKDOWN) // Markdown类型
                        .body(resource);
            } else {
                getNotFoundResources.getNotFoundMd(filename);
                return ResponseEntity.status(506).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @Client(address = "/admin/clean", name = "clean")
    @GetMapping("/admin/clean")
    public ResponseEntity<Map<String, Object>> clean() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ArticleResourcePath> articles = articleResourcePathServer.findAllArticleStatic();
            List<ImageRecords> images = imageRecordsMapper.findAllImageRecords();
            articleResourcePathServer.removeArticles();
            result.put("code", 200);
            result.put("message", "文章默认文件成功");
            getNotFoundResources.removeArticles(articles, images);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", "文章默认文件失败: " + e.getMessage());
            return ResponseEntity.status(400).body(result);
        }
    }

    @GetMapping("/user/likeArticle")
    public ResponseEntity<Map<String, Object>> likeArticle(@RequestParam long timestamp) {
        Map<String, Object> result = new HashMap<>();
        try {
            articleResourcePathServer.likeArticle(timestamp);
            result.put("code", 200);
            result.put("message", "文章点赞成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "文章点赞失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 将MultipartFile转换为File
     *
     * @param multipartFile MultipartFile对象
     * @return File对象
     * @throws IOException IO异常
     */
    private File convertToFile(MultipartFile multipartFile) throws IOException {
        // 创建临时文件
        String originalFilename = multipartFile.getOriginalFilename();
        String tempDir = System.getProperty("java.io.tmpdir");
        Path tempPath = Paths.get(tempDir, originalFilename);
        // 直接将上传的文件内容传输到临时文件
        multipartFile.transferTo(tempPath);
        return tempPath.toFile();
    }

}
