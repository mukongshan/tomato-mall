package com.example.tomatomall.controller;


import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("api/products")
public class ProductController {
    @Resource
    private ProductService productService;


    @GetMapping
    public Response<List<ProductVO>> getAllProduct() {
        return Response.buildSuccess(productService.getAllProducts());
    }


    @GetMapping("/{id}")
    public Response<ProductVO> getProductById(@PathVariable Integer id) {
        return Response.buildSuccess(productService.getProductById(id));
    }


    @PostMapping
    public Response<String> addProduct(@RequestBody ProductVO productVO) {
        return Response.buildSuccess(productService.createProduct(productVO));
    }



}
