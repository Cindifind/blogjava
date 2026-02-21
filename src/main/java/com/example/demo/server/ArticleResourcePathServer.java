package com.example.demo.server;

import com.example.demo.mapper.ArticleResourcePathMapper;
import com.example.demo.mapper.ImageRecordsMapper;
import com.example.demo.model.ArticleResourcePath;
import com.example.demo.model.ImageRecords;
import com.example.demo.util.GetNotFoundResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ArticleResourcePathServer {
    @Autowired
    private ArticleResourcePathMapper articleResourcePathMapper;
    @Autowired
    private ImageRecordsMapper imageRecordsMapper;
    private final GetNotFoundResources getNotFoundResources = new GetNotFoundResources();

    @Autowired
    private CommentService commentService;

    public static String path = System.getProperty("user.dir");

    public void insertArticleResourcePath(ArticleResourcePath articleResourcePath, File image, File md) {
        try {
            // 创建目录（如果不存在）
            File imageDir = new File(path + "/image/");
            File mdDir = new File(path + "/md/");

            if (!imageDir.exists()) {
                boolean imageDirCreated = imageDir.mkdirs();
                if (!imageDirCreated) {
                    throw new RuntimeException("无法创建图片目录: " + imageDir.getAbsolutePath());
                }
            }
            if (!mdDir.exists()) {
                boolean mdDirCreated = mdDir.mkdirs();
                if (!mdDirCreated) {
                    throw new RuntimeException("无法创建Markdown文件目录: " + mdDir.getAbsolutePath());
                }
            }
            // 使用更可靠的文件移动方法
            long timestamp = System.currentTimeMillis();
            articleResourcePath.setTimestamp(timestamp);
            Path imagePath = Paths.get(image.getAbsolutePath());
            Path newImagePath = Paths.get(imageDir.getAbsolutePath(), image.getName());
            Files.move(imagePath, newImagePath, StandardCopyOption.REPLACE_EXISTING);
            Path mdPath = Paths.get(md.getAbsolutePath());
            Path newMdPath = Paths.get(mdDir.getAbsolutePath(), md.getName());
            Files.move(mdPath, newMdPath, StandardCopyOption.REPLACE_EXISTING);
            // 设置文章资源路径
            articleResourcePath.setImage("image/" + image.getName());
            articleResourcePath.setMd("md/" + md.getName());
            articleResourcePath.setLike(0);
            articleResourcePath.setComment(0);
            articleResourcePath.setBrowse(0);
            articleResourcePathMapper.insertArticleResourcePath(articleResourcePath);
        } catch (IOException e) {
            throw new RuntimeException("文件移动失败: " + e.getMessage(), e);
        }
    }

    public void insertMdImage(File image) {
        // 创建目录（如果不存在）
        File mdDir = new File(path + "/mdImage/");
        if (!mdDir.exists()) {
            boolean mdDirCreated = mdDir.mkdirs();
            if (!mdDirCreated) {
                throw new RuntimeException("无法创建Markdown文件目录: " + mdDir.getAbsolutePath());
            }
        }
        // 使用更可靠的文件移动方法
        long timestamp = System.currentTimeMillis();
        Path imagePath = Paths.get(image.getAbsolutePath());
        Path newImagePath = Paths.get(mdDir.getAbsolutePath(), image.getName());
        try {
            Files.move(imagePath, newImagePath, StandardCopyOption.REPLACE_EXISTING);
            ImageRecords imageRecords = new ImageRecords(timestamp, "mdImage/" + image.getName());
            imageRecordsMapper.insertImageRecords(imageRecords);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMdImage(String mdImageName) {
        // 处理带前缀的文件名，提取实际文件名
        // 定位到正确的目录
        File mdDir = new File(path + "/" + mdImageName);
        if (!mdDir.exists()) {
            return; // 目录不存在，无需删除
        }

        // 删除指定的图片文件
        if (mdDir.exists()) {
            if (!mdDir.delete()) {
                throw new RuntimeException("无法删除图片文件: " + mdDir.getAbsolutePath());
            }
            imageRecordsMapper.deleteImageRecords(mdImageName);
        }
    }


    public List<String> getMdImageNames() {
        List<String> mdImageNames = new ArrayList<>();
        List<ImageRecords> imageRecords = imageRecordsMapper.findAllImageRecords();
        for (ImageRecords imageRecord : imageRecords) {
            mdImageNames.add(imageRecord.getMdImage());
        }
        getNotFoundResources.removeMdImages(imageRecords);
        return mdImageNames;
    }

    public void deleteArticleResourcePath(long timestamp) {
        // 首先获取文章信息，以便知道要删除哪些文件
        List<ArticleResourcePath> articles = articleResourcePathMapper.findArticleByTimestamp(timestamp);
        if (!articles.isEmpty()) {
            ArticleResourcePath article = articles.get(0);

            // 删除关联的图片文件
            String imagePath = article.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(path + "/" + imagePath);
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            }

            // 删除关联的Markdown文件
            String mdPath = article.getMd();
            if (mdPath != null && !mdPath.isEmpty()) {
                File mdFile = new File(path + "/" + mdPath);
                if (mdFile.exists()) {
                    mdFile.delete();
                }
            }

            // 删除与文章相关的所有评论
            commentService.deleteCommentsByArticleId(timestamp);
        }

        // 从数据库中删除文章记录
        articleResourcePathMapper.deleteArticleResourcePath(timestamp);
    }

    //清理所有大小小于20kb以下的文件path+/image/ path+/md/ path+/mdImage/
    public void removeArticles() {
        File imageDir = new File(path + "/image/");
        File mdDir = new File(path + "/md/");
        File mdImageDir = new File(path + "/mdImage/");
        if (imageDir.exists()) {
            for (File file : Objects.requireNonNull(imageDir.listFiles())) {
                if (file.length() < 512) {
                    file.delete();
                }
            }
        }
        if (mdDir.exists()) {
            for (File file : Objects.requireNonNull(mdDir.listFiles())) {
                if (file.length() < 512) {
                    file.delete();
                }
            }
        }
        if (mdImageDir.exists()) {
            for (File file : Objects.requireNonNull(mdImageDir.listFiles())) {
                if (file.length() < 512) {
                    file.delete();
                }
            }
        }
    }

    public int getArticleCount() {
        return articleResourcePathMapper.getArticleCount();
    }

    public List<ArticleResourcePath> findArticleByPageNum(int pageNum) {
        return articleResourcePathMapper.findArticleByPageNum(pageNum);
    }
    public List<ArticleResourcePath> findAllArticleStatic() {
        return articleResourcePathMapper.findAllArticleStatic();
    }

    public File getImageFile(String imagePath) {
        return new File(path + "/" + imagePath);
    }

    public File getMdFile(String mdPath) {
        return new File(path + "/" + mdPath);
    }

    public void likeArticle(long timestamp) {
        articleResourcePathMapper.likeArticle(timestamp);
    }

    public String getImage(String image) {
        return articleResourcePathMapper.getImage(image);
    }

    public String getMd(String md) {
        return articleResourcePathMapper.getMd(md);
    }
}