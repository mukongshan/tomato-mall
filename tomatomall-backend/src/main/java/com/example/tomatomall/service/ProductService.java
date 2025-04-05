package com.example.tomatomall.service;

import com.example.tomatomall.vo.ProductVO;

import java.util.List;

public interface ProductService {
    List<ProductVO> getAllProducts();
    ProductVO getProductById(int id);
    String createProduct(ProductVO productVO);
}
