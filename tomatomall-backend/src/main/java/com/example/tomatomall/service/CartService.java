package com.example.tomatomall.service;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.vo.CartListVO;
import com.example.tomatomall.vo.CartVO;

import java.util.List;

public interface CartService {
    public CartVO addProduct(Integer productId, Integer quantity);
    public String deleteProduct(Integer cartItemId);
    public String updateProduct(Integer productId, Integer quantity);
    public CartListVO getCart();
}
