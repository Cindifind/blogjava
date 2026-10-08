package com.example.demo.util;

import com.example.demo.model.ArticleResourcePath;
import com.example.demo.model.ImageRecords;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class GetNotFoundResources {
    private static final Logger log = LoggerFactory.getLogger(GetNotFoundResources.class);
    public static String path = System.getProperty("user.dir");

    public void getNotFoundImages(String path) {
        File targetFile = new File(GetNotFoundResources.path, path);
        log.info("开始获取缺失的图片文件, 文件名: {}, 目标保存路径: {}", path, targetFile.getAbsolutePath());
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            log.warn("创建目录失败: {}", parentDir.getAbsolutePath());
        }
        //先下载到临时文件, 成功后再移动到目标路径, 避免目标目录不存在或目标文件已存在导致写入失败
        File tempFile = new File(parentDir, targetFile.getName() + ".tmp");
        long startTime = System.currentTimeMillis();
        try {
//        HttpResponse<File> response = Unirest.get("http://luren.online:2345/prosy/image?filename="+path)
            //使用这个获取的文件存入路径this.path+path
            HttpResponse<File> response = Unirest.get("https://muqingxi.com:2345/proxy/image?filename=" + path)
                    .asFile(tempFile.getAbsolutePath());
            log.info("图片请求完成, 文件名: {}, HTTP状态码: {}, 耗时: {} ms", path, response.getStatus(), System.currentTimeMillis() - startTime);
            //将获取的文件保存到本地
            File file = response.getBody();
            if (file != null && file.exists() && file.length() > 0) {
                log.info("图片下载成功, 文件名: {}, 临时文件大小: {} 字节", path, file.length());
                try {
                    Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    log.info("图片已保存到本地: {}", targetFile.getAbsolutePath());
                } catch (IOException e) {
                    log.error("图片移动失败, 临时文件: {}, 目标路径: {}", tempFile.getAbsolutePath(), targetFile.getAbsolutePath(), e);
                    throw new RuntimeException("图片移动失败: " + targetFile.getAbsolutePath(), e);
                }
            } else {
                log.warn("图片下载失败, 响应内容为空, 文件名: {}, HTTP状态码: {}, Content-Length: {}, Content-Type: {}",
                        path, response.getStatus(),
                        response.getHeaders().getFirst("Content-Length"),
                        response.getHeaders().getFirst("Content-Type"));
                cleanupTempFile(tempFile);
            }
        } catch (Exception e) {
            cleanupTempFile(tempFile);
            throw e;
        }
    }

    public void getNotFoundMd(String path) {
        File targetFile = new File(GetNotFoundResources.path, path);
        log.info("开始获取缺失的md文件, 文件名: {}, 目标保存路径: {}", path, targetFile.getAbsolutePath());
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            log.warn("创建目录失败: {}", parentDir.getAbsolutePath());
        }
        //先下载到临时文件, 成功后再移动到目标路径, 避免目标目录不存在或目标文件已存在导致写入失败
        File tempFile = new File(parentDir, targetFile.getName() + ".tmp");
        long startTime = System.currentTimeMillis();
        try {
//        HttpResponse<File> response = Unirest.get("http://luren.online:2345/prosy/md?filename="+path)
            //使用这个获取的文件存入路径this.path+path
            HttpResponse<File> response = Unirest.get("https://muqingxi.com:2345/proxy/md?filename=" + path)
                    .asFile(tempFile.getAbsolutePath());
            log.info("md请求完成, 文件名: {}, HTTP状态码: {}, 耗时: {} ms", path, response.getStatus(), System.currentTimeMillis() - startTime);
            //将获取的文件保存到本地
            File file = response.getBody();
            if (file != null && file.exists() && file.length() > 0) {
                log.info("md下载成功, 文件名: {}, 临时文件大小: {} 字节", path, file.length());
                try {
                    Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    log.info("md已保存到本地: {}", targetFile.getAbsolutePath());
                } catch (IOException e) {
                    log.error("md移动失败, 临时文件: {}, 目标路径: {}", tempFile.getAbsolutePath(), targetFile.getAbsolutePath(), e);
                    throw new RuntimeException("md移动失败: " + targetFile.getAbsolutePath(), e);
                }
            } else {
                log.warn("md下载失败, 响应内容为空, 文件名: {}, HTTP状态码: {}, Content-Length: {}, Content-Type: {}",
                        path, response.getStatus(),
                        response.getHeaders().getFirst("Content-Length"),
                        response.getHeaders().getFirst("Content-Type"));
                cleanupTempFile(tempFile);
            }
        } catch (Exception e) {
            cleanupTempFile(tempFile);
            throw e;
        }
    }

    public void removeArticles(List<ArticleResourcePath> articles,List<ImageRecords> images) {
        File mdDir = new File(path + "/md/");
        File imageDir = new File(path + "/image/");
        File mdImageDir = new File(path + "/mdImage/");
        File[] imageFiles = imageDir.listFiles();
        File[] mdFiles = mdDir.listFiles();
        File[] mdImageFiles = mdImageDir.listFiles();

        // 获取本地文件列表
        List<String> localMdList = new ArrayList<>();
        if (mdFiles != null) {
            for (File mdFile : mdFiles) {
                localMdList.add(mdFile.getName());
            }
        } else {
            log.warn("md目录不存在或无法读取: {}", mdDir.getAbsolutePath());
        }

        List<String> localImageList = new ArrayList<>();
        if (imageFiles != null) {
            for (File imageFile : imageFiles) {
                localImageList.add(imageFile.getName());
            }
        } else {
            log.warn("image目录不存在或无法读取: {}", imageDir.getAbsolutePath());
        }
        List<String> localMdImageList = new ArrayList<>();
        if (mdImageFiles != null) {
            for (File mdImageFile : mdImageFiles) {
                localMdImageList.add(mdImageFile.getName());
            }
        } else {
            log.warn("mdImage目录不存在或无法读取: {}", mdImageDir.getAbsolutePath());
        }

        // 创建数据库中文件名的集合
        List<String> dbMdList = new ArrayList<>();
        List<String> dbImageList = new ArrayList<>();
        List<String> dbMdImageList = new ArrayList<>();

        for (ArticleResourcePath article : articles) {
            if (article.getMd() != null && !article.getMd().isEmpty()) {
                String mdName = article.getMd().substring(article.getMd().lastIndexOf("/") + 1);
                dbMdList.add(mdName);
            }
            if (article.getImage() != null && !article.getImage().isEmpty()) {
                String imageName = article.getImage().substring(article.getImage().lastIndexOf("/") + 1);
                dbImageList.add(imageName);
            }
        }
        for (ImageRecords image : images) {
            if (image.getMdImage() != null && !image.getMdImage().isEmpty()) {
                String mdImageName = image.getMdImage().substring(image.getMdImage().lastIndexOf("/") + 1);
                dbMdImageList.add(mdImageName);
            }
        }

        log.info("开始清理本地冗余文件: 本地md={}个/数据库md={}个, 本地image={}个/数据库image={}个, 本地mdImage={}个/数据库mdImage={}个",
                localMdList.size(), dbMdList.size(), localImageList.size(), dbImageList.size(),
                localMdImageList.size(), dbMdImageList.size());

        // 删除本地存在但数据库中不存在的md文件
        int deletedMdCount = 0;
        if (mdFiles != null) {
            for (File mdFile : mdFiles) {
                if (!dbMdList.contains(mdFile.getName())) {
                    boolean deleted = mdFile.delete();
                    if (deleted) {
                        deletedMdCount++;
                        log.info("已删除冗余md文件: {}", mdFile.getName());
                    } else {
                        log.warn("删除冗余md文件失败: {}", mdFile.getName());
                    }
                }
            }
        }

        // 删除本地存在但数据库中不存在的图片文件
        int deletedImageCount = 0;
        if (imageFiles != null) {
            for (File imageFile : imageFiles) {
                if (!dbImageList.contains(imageFile.getName())) {
                    boolean deleted = imageFile.delete();
                    if (deleted) {
                        deletedImageCount++;
                        log.info("已删除冗余image文件: {}", imageFile.getName());
                    } else {
                        log.warn("删除冗余image文件失败: {}", imageFile.getName());
                    }
                }
            }
        }
        // 删除本地存在但数据库中不存在的mdImage文件
        int deletedMdImageCount = 0;
        if (mdImageFiles != null) {
            for (File mdImageFile : mdImageFiles) {
                if (!dbMdImageList.contains(mdImageFile.getName())) {
                    boolean deleted = mdImageFile.delete();
                    if (deleted) {
                        deletedMdImageCount++;
                        log.info("已删除冗余mdImage文件: {}", mdImageFile.getName());
                    } else {
                        log.warn("删除冗余mdImage文件失败: {}", mdImageFile.getName());
                    }
                }
            }
        }
        log.info("冗余文件清理完成: 删除md={}个, image={}个, mdImage={}个", deletedMdCount, deletedImageCount, deletedMdImageCount);
    }

    public void removeMdImages(List<ImageRecords> articles) {
        File mdDir = new File(path + "/mdImage/");
        File[] mdFiles = mdDir.listFiles();

        if (mdFiles == null || mdFiles.length == 0) {
            log.info("mdImage目录为空或不存在, 无需清理: {}", mdDir.getAbsolutePath());
            return;
        }

        // 获取本地文件名列表
        List<String> localMdList = new ArrayList<>();
        for (File mdFile : mdFiles) {
            localMdList.add(mdFile.getName());
        }

        // 获取数据库中文件名列表
        List<String> dbMdList = new ArrayList<>();
        for (ImageRecords article : articles) {
            String mdImage = article.getMdImage();
            if (mdImage != null && !mdImage.isEmpty()) {
                String mdImageName = mdImage.substring(mdImage.lastIndexOf("/") + 1);
                dbMdList.add(mdImageName);
            }
        }

        log.info("开始清理mdImage冗余文件: 本地{}个, 数据库{}个", localMdList.size(), dbMdList.size());

        // 删除本地存在但数据库中不存在的文件
        int deletedCount = 0;
        for (File mdFile : mdFiles) {
            if (!dbMdList.contains(mdFile.getName())) {
                boolean deleted = mdFile.delete();
                if (deleted) {
                    deletedCount++;
                    log.info("已删除冗余mdImage文件: {}", mdFile.getName());
                } else {
                    log.warn("删除冗余mdImage文件失败: {}", mdFile.getName());
                }
            }
        }
        log.info("mdImage冗余文件清理完成, 共删除{}个文件", deletedCount);
    }


    private void Remove(File mdDir, List<String> localMdList, String mdImage) {
        if (mdImage != null && !mdImage.isEmpty()) {
            String mdImageName = mdImage.substring(mdImage.lastIndexOf("/") + 1);
            if (!localMdList.contains(mdImageName)) {
                File mdImageFile = new File(mdDir, mdImageName);
                if (mdImageFile.exists()) {
                    boolean deleted = mdImageFile.delete();
                    if (deleted) {
                        log.info("已删除mdImage文件: {}", mdImageFile.getAbsolutePath());
                    } else {
                        log.warn("删除mdImage文件失败: {}", mdImageFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    private void cleanupTempFile(File tempFile) {
        if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
            log.warn("清理临时文件失败: {}", tempFile.getAbsolutePath());
        }
    }
}