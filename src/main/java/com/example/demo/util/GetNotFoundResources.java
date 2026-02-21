package com.example.demo.util;

import com.example.demo.model.ArticleResourcePath;
import com.example.demo.model.ImageRecords;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GetNotFoundResources {
    public static String path = System.getProperty("user.dir");

    public void getNotFoundImages(String path) {
//        HttpResponse<File> response = Unirest.get("http://luren.online:2345/prosy/image?filename="+path)
        //使用这个获取的文件存入路径this.path+path
        HttpResponse<File> response = Unirest.get("http://luren.online:2345/proxy/image?filename=" + path)
                .asFile(path);
        //将获取的文件保存到本地
        File file = response.getBody();
        System.out.println(response.getBody());
        if (file != null) {
            file.renameTo(new File(GetNotFoundResources.path + path));
        }
    }

    public void getNotFoundMd(String path) {
//        HttpResponse<File> response = Unirest.get("http://luren.online:2345/prosy/md?filename="+path)
        //使用这个获取的文件存入路径this.path+path
        HttpResponse<File> response = Unirest.get("http://luren.online:2345/proxy/md?filename=" + path)
                .asFile(path);
        //将获取的文件保存到本地
        File file = response.getBody();
        System.out.println(response.getBody());
        if (file != null) {
            file.renameTo(new File(GetNotFoundResources.path + path));
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
        }

        List<String> localImageList = new ArrayList<>();
        if (imageFiles != null) {
            for (File imageFile : imageFiles) {
                localImageList.add(imageFile.getName());
            }
        }
        List<String> localMdImageList = new ArrayList<>();
        if (mdImageFiles != null) {
            for (File mdImageFile : mdImageFiles) {
                localMdImageList.add(mdImageFile.getName());
            }
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

        // 删除本地存在但数据库中不存在的md文件
        if (mdFiles != null) {
            for (File mdFile : mdFiles) {
                if (!dbMdList.contains(mdFile.getName())) {
                    mdFile.delete();
                }
            }
        }

        // 删除本地存在但数据库中不存在的图片文件
        if (imageFiles != null) {
            for (File imageFile : imageFiles) {
                if (!dbImageList.contains(imageFile.getName())) {
                    imageFile.delete();
                }
            }
        }
        // 删除本地存在但数据库中不存在的mdImage文件
        if (mdImageFiles != null) {
            for (File mdImageFile : mdImageFiles) {
                if (!dbMdImageList.contains(mdImageFile.getName())) {
                    mdImageFile.delete();
                }
            }
        }
    }

    public void removeMdImages(List<ImageRecords> articles) {
        File mdDir = new File(path + "/mdImage/");
        File[] mdFiles = mdDir.listFiles();

        if (mdFiles == null || mdFiles.length == 0) {
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

        // 删除本地存在但数据库中不存在的文件
        for (File mdFile : mdFiles) {
            if (!dbMdList.contains(mdFile.getName())) {
                mdFile.delete();
            }
        }
    }


    private void Remove(File mdDir, List<String> localMdList, String mdImage) {
        if (mdImage != null && !mdImage.isEmpty()) {
            String mdImageName = mdImage.substring(mdImage.lastIndexOf("/") + 1);
            if (!localMdList.contains(mdImageName)) {
                File mdImageFile = new File(mdDir, mdImageName);
                if (mdImageFile.exists()) {
                    mdImageFile.delete();
                }
            }
        }
    }
}