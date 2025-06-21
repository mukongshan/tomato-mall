package com.example.tomatomall.util;

import com.example.tomatomall.util.ImageStorage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageStorageFactory {
    @Autowired
    private LocalStorage localStorage;  // Spring 管理的 Bean

    @Autowired
    private OssStorage ossStorage;

    @Value("${storage.type:local}")  // 默认值为 "local"
    private String storageType;  // 值为 "oss" 或 "local"

    /**
     * 从配置动态选择（推荐）
     */
    public ImageStorage getStorage() {
        // 默认从配置读取，storage.type=oss或者local
        if ("oss".equalsIgnoreCase(storageType)) {
            return ossStorage;
        } else if ("local".equalsIgnoreCase(storageType)) {
            return localStorage;
        } else {
            throw new IllegalArgumentException("Invalid storage.type: " + storageType);
        }
    }
}