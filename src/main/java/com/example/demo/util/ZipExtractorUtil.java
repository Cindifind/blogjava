package com.example.demo.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * ZIP解压工具类
 * 用于解压ZIP文件并将包含中文括号的文件名转换为英文括号
 */
public class ZipExtractorUtil {

    /**
     * 解压指定目录下的所有ZIP文件，并重命名包含中文括号的文件
     * @param targetDirectory 目标目录路径
     * @throws IOException IO异常
     */
    public static void extractAndRenameZipFiles(String targetDirectory) throws IOException {
        Path directory = Paths.get(targetDirectory);
        
        if (!Files.exists(directory)) {
            throw new IOException("目标目录不存在: " + targetDirectory);
        }
        
        if (!Files.isDirectory(directory)) {
            throw new IOException("目标路径不是目录: " + targetDirectory);
        }
        
        // 遍历目录中的所有ZIP文件
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().toLowerCase().endsWith(".zip"))
                 .forEach(zipPath -> {
                     try {
                         extractZip(zipPath.toString(), targetDirectory);
                     } catch (IOException e) {
                         System.err.println("解压文件失败: " + zipPath + ", 错误: " + e.getMessage());
                     }
                 });
        }
    }

    /**
     * 解压单个ZIP文件
     * @param zipFilePath ZIP文件路径
     * @param destDirectory 解压目标目录
     * @throws IOException IO异常
     */
    private static void extractZip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        // 尝试不同的字符集解决中文文件名问题
        Charset[] charsets = {Charset.forName("UTF-8"), Charset.forName("GBK"), Charset.defaultCharset()};
        ZipFile zipFile = null;
        
        for (Charset charset : charsets) {
            try {
                zipFile = new ZipFile(zipFilePath, charset);
                break; // 成功则跳出循环
            } catch (Exception e) {
                // 继续尝试下一个字符集
            }
        }
        
        if (zipFile == null) {
            // 如果都失败，默认使用系统字符集
            zipFile = new ZipFile(zipFilePath);
        }

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String fileName = entry.getName();
            
            // 重命名包含中文括号的文件
            String renamedFileName = renameChineseBracketsToEnglish(fileName);
            
            File entryFile = new File(destDirectory, renamedFileName);
            
            // 确保父目录存在
            File parent = entryFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            
            if (entry.isDirectory()) {
                entryFile.mkdirs();
            } else {
                // 写入文件内容
                try (InputStream inputStream = zipFile.getInputStream(entry);
                     FileOutputStream outputStream = new FileOutputStream(entryFile)) {
                    
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
            }
        }
        
        zipFile.close();
    }

    /**
     * 将文件名中的中文括号替换为英文括号
     * @param fileName 原始文件名
     * @return 替换后的文件名
     */
    private static String renameChineseBracketsToEnglish(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }
        
        return fileName.replace('（', '(')
                      .replace('）', ')')
                      .replace('【', '[')
                      .replace('】', ']')
                      .replace('《', '<')
                      .replace('》', '>');
    }
}