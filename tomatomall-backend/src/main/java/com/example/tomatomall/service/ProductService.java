package com.example.tomatomall.service;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.StockpileVO;

import java.util.List;

public interface ProductService {
    List<ProductVO> getAllProducts();
    ProductVO getProductById(int id);
    ProductVO createProduct(ProductVO productVO);
    String updateProduct(ProductVO productVO);
    String deleteProduct(int id);
    StockpileVO getStockpile(int id);
    String updateStockpile(int id,int amount);
    String increaseStockpile(int id, int amount);
    String reduceStockpile(int id, int amount);
    String reduceStockpileByOrder(String orderId);
}

