package com.example.tomatomall.controller;

import com.example.tomatomall.util.ImageStorage.ImageStorage;
import com.example.tomatomall.util.ImageStorageFactory;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/image")
public class ImageController {
    @Autowired
    private ImageStorageFactory storageFactory;  // 注入工厂

    @PostMapping()
    public Response<String> uploadImage(@RequestParam MultipartFile file){
        return Response.buildSuccess(storageFactory.getStorage().upload(file));
    }

}