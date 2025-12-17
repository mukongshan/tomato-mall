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

/**
 * 阿里云OSS对象存储实现类
 * 实现ImageStorage接口，提供基于阿里云OSS的文件上传和删除功能
 * 
 * 主要功能：
 * 1. 文件上传到阿里云OSS
 * 2. 文件从阿里云OSS删除
 * 3. 自动配置OSS连接参数
 * 
 * 使用@ConfigurationProperties注解自动绑定配置文件中的OSS配置
 */
@Component
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties("aliyun.oss")  // 自动加载application.yml中aliyun.oss配置项到当前类的属性
public class OssStorage implements ImageStorage {
    
    /**
     * OSS服务端点地址
     * 例如：oss-cn-hangzhou.aliyuncs.com
     */
    private String endpoint;
    
    /**
     * 阿里云访问密钥ID
     * 用于身份验证
     */
    private String accessKeyId;
    
    /**
     * 阿里云访问密钥Secret
     * 用于身份验证
     */
    private String accessKeySecret;
    
    /**
     * OSS存储桶名称
     * 文件将存储在此桶中
     */
    private String bucketName;

    /**
     * 上传文件到OSS
     * 
     * @param file 要上传的文件（MultipartFile格式）
     * @return 上传成功后的文件访问URL
     * @throws TomatoMallException 当上传失败时抛出异常
     */
    @Override
    public String upload(MultipartFile file) {
        try {
            // 调用私有方法进行实际的上传操作
            return upload(file.getOriginalFilename(), file.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            // 抛出自定义异常，表示文件上传失败
            throw TomatoMallException.fileUploadFail();
        }
    }

    /**
     * 私有方法：执行实际的文件上传操作
     * 
     * @param objectName 对象名称（文件名）
     * @param inputStream 文件输入流
     * @return 上传成功后的文件访问URL（不包含签名参数）
     */
    private String upload(String objectName, InputStream inputStream) {
        // 创建OSS客户端实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        
        // 创建上传请求对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
        
        try {
            // 执行文件上传
            ossClient.putObject(putObjectRequest);
        } finally {
            // 确保OSS客户端被正确关闭，释放资源
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        
        // 生成预签名URL并返回不带过期时间的URL
        // 移除?Expires参数，返回纯净的访问URL
        return ossClient.generatePresignedUrl(bucketName, objectName, new Date()).toString().split("\\?Expires")[0];
    }

    /**
     * 从OSS删除文件
     * 
     * @param url 要删除的文件URL
     * @return 删除操作的结果信息
     */
    public String delete(String url) {
        // 参数验证：检查URL是否为空
        if (url == null || url.isEmpty()) {
            return "URL不能为空";
        }
        
        // 从URL中提取出Object Key（文件在OSS中的标识符）
        String objectKey = extractObjectKeyFromUrl(url);
        System.out.println("objectKey: " + objectKey);
        
        // 验证提取的Object Key是否有效
        if (objectKey == null || objectKey.isEmpty()) {
            return "无法从URL中提取有效的Object Key";
        }

        // 创建OSS客户端实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        
        try {
            // 检查文件是否存在于OSS中
            boolean exists = ossClient.doesObjectExist(bucketName, objectKey);
            if (!exists) {
                return "文件不存在，无需删除";
            }
            
            // 执行文件删除操作
            ossClient.deleteObject(bucketName, objectKey);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "删除失败: " + e.getMessage();
        } finally {
            // 确保OSS客户端被正确关闭
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 从OSS URL中提取Object Key
     * 
     * @param url OSS文件的完整URL
     * @return 提取出的Object Key，如果提取失败返回null
     */
    private String extractObjectKeyFromUrl(String url) {
        try {
            // 移除URL中可能的查询参数（如签名、过期时间等）
            String cleanUrl = url.split("\\?")[0];
            
            // 构建可能的OSS URL前缀格式
            // 支持HTTP和HTTPS两种协议
            String ossPrefix1 = "http://" + bucketName + "." + endpoint + "/";
            String ossPrefix2 = "https://" + bucketName + "." + endpoint + "/";
            
            // 检查URL是否匹配HTTP格式的前缀
            if (cleanUrl.startsWith(ossPrefix1)) {
                // 返回前缀之后的部分，即Object Key
                return cleanUrl.substring(ossPrefix1.length());
            }
            
            // 检查URL是否匹配HTTPS格式的前缀
            if (cleanUrl.startsWith(ossPrefix2)) {
                // 返回前缀之后的部分，即Object Key
                return cleanUrl.substring(ossPrefix2.length());
            }
            
            // 如果都不匹配，返回null
            return null;
        } catch (Exception e) {
            // 发生异常时返回null
            return null;
        }
    }
}
