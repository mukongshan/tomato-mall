package com.example.tomatomall.util.ImageStorage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class LocalStorage implements ImageStorage {

    @Value("${storage.local.ip:http://121.41.41.33/}")
    private String ip;

    @Value("${storage.local.dir:/home/img}")
    private String saveDir;

    @Override
    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            return "上传失败，文件为空";
        }

        // 设置保存路径（相对路径或绝对路径）
        String saveDir = "/home/img"; // img 文件夹
        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs(); // 创建目录
        }

        // 获取原始文件名并生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString() + suffix;

        File dest = new File(dir, newFileName);
        try {
            file.transferTo(dest);
            return ip + "img/" + newFileName; // 返回图片相对路径或可访问的路径
        } catch (IOException e) {
            e.printStackTrace();
            return "上传失败，服务器错误";
        }
    }

    @Override
    public String delete(String url) {
        try {
            // 1. 从 URL 中提取文件名（假设 URL 格式为 "http://121.41.41.33/img/filename.jpg"）
            String fileName = url.substring(url.lastIndexOf("/") + 1);

            // 2. 拼接本地文件路径
            String filePath = "/home/img/" + fileName;
            File file = new File(filePath);

            // 3. 检查文件是否存在并删除
            if (file.exists()) {
                if (file.delete()) {
                    return "删除成功: " + url;
                } else {
                    return "删除失败: 文件无法删除";
                }
            } else {
                return "删除失败: 文件不存在";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "删除失败: 服务器错误";
        }
    }
}
