package com.example.tomatomall.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class ImageUtil {

    String IP = "http://121.41.41.33/";

    public String saveImg(MultipartFile file) {
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
            return IP + "img/" + newFileName; // 返回图片相对路径或可访问的路径
        } catch (IOException e) {
            e.printStackTrace();
            return "上传失败，服务器错误";
        }
    }
}
