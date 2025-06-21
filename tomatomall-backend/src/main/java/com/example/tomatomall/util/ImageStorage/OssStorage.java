package com.example.tomatomall.util.ImageStorage;

import com.example.tomatomall.exception.TomatoMallException;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Date;

@Component
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties("aliyun.oss")  //作用是可以加载配置文件中的值到你的bean属性中
public class OssStorage implements ImageStorage {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    @Override
    public String upload(MultipartFile file) {
        try {
            return upload(file.getOriginalFilename(), file.getInputStream());
        }catch (Exception e){
            e.printStackTrace();
            throw TomatoMallException.fileUploadFail();
        }
    }

    private String upload(String objectName, InputStream inputStream) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
        try {
            ossClient.putObject(putObjectRequest);
        }finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return ossClient.generatePresignedUrl(bucketName, objectName, new Date()).toString().split("\\?Expires")[0];
    }

    public String delete(String url) {
        if (url == null || url.isEmpty()) {
            return "URL不能为空";
        }
        // 从URL中提取出Object Key
        String objectKey = extractObjectKeyFromUrl(url);
        System.out.println("objectKey: " + objectKey);
        if (objectKey == null || objectKey.isEmpty()) {
            return "无法从URL中提取有效的Object Key";
        }

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 检查文件是否存在
            boolean exists = ossClient.doesObjectExist(bucketName, objectKey);
            if (!exists) {
                return "文件不存在，无需删除";
            }
            // 执行删除
            ossClient.deleteObject(bucketName, objectKey);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "删除失败: " + e.getMessage();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private String extractObjectKeyFromUrl(String url) {
        try {
            // 移除可能的查询参数
            String cleanUrl = url.split("\\?")[0];
            // 检查是否是有效的OSS URL格式
            String ossPrefix1 = "http://" + bucketName + "." + endpoint + "/";

            String ossPrefix2 = "https://" + bucketName + "." + endpoint + "/";
            if (cleanUrl.startsWith(ossPrefix1)) {
                return cleanUrl.substring(ossPrefix1.length());
            }
            if (cleanUrl.startsWith(ossPrefix2)) {
                return cleanUrl.substring(ossPrefix2.length());
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
