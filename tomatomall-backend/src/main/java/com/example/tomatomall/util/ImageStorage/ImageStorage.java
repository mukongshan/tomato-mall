package com.example.tomatomall.util.ImageStorage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {
    /**
     * 上传图片并返回访问URL
     * @param file 图片文件
     * @return 图片访问URL（OSS URL 或本地路径）
     */
    String upload(MultipartFile file);

    /**
     * 删除图片
     * @param url 图片URL
     */
    String delete(String url);
}