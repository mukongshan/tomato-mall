package com.example.tomatomall.controller;


import com.example.tomatomall.po.Product;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.Response;
import com.example.tomatomall.vo.StockpileVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
    public Response<ProductVO> addProduct(@RequestBody ProductVO productVO) {
        return Response.buildSuccess(productService.createProduct(productVO));
    }


    @PutMapping
    public Response<String> updateProduct(@RequestBody ProductVO productVO) {
        return Response.buildSuccess(productService.updateProduct(productVO));
    }

    @DeleteMapping("/{id}")
    public Response<String> deleteProduct(@PathVariable Integer id) {
        return Response.buildSuccess(productService.deleteProduct(id));
    }

    @GetMapping("/stockpile/{productId}")
    public Response<StockpileVO> getProductStock(@PathVariable Integer productId) {
        return Response.buildSuccess(productService.getStockpile(productId));
    }

    @PatchMapping("/stockpile/{productId}")
    public Response<String> updateProductStock(@PathVariable Integer productId,@RequestBody Map<String, Integer> body ) {
        Integer amount = (Integer) body.get("amount");
        System.out.println("amount = " + amount);
        return Response.buildSuccess(productService.updateStockpile(productId,amount));
    }
}
