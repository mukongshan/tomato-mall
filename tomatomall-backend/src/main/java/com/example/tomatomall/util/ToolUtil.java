package com.example.tomatomall.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.tomatomall.exception.TomatoMallException;

@Component
public class ToolUtil {
    
    @Autowired
    OssUtil ossUtil;
        
        public String uploadImg(MultipartFile file) {
            try {
                return ossUtil.upload(file.getOriginalFilename(),file.getInputStream());
        }catch (Exception e){
            e.printStackTrace();
            throw TomatoMallException.fileUploadFail();
        }
    }
}
