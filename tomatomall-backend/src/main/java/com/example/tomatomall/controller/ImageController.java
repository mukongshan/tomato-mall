package com.example.tomatomall.controller;

import com.example.tomatomall.util.ImageUtil;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Autowired
    private ImageUtil imageUtil;

    @PostMapping()
    public Response<String> saveImg(@RequestParam MultipartFile file){
        return Response.buildSuccess(imageUtil.saveImg(file));
    }

}