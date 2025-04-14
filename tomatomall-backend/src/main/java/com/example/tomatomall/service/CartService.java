package com.example.tomatomall.service;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.vo.CartListVO;
import com.example.tomatomall.vo.CartVO;
import com.example.tomatomall.vo.OrderVO;

import java.util.List;

public interface CartService {
    public CartVO addCartItem(Integer productId, Integer quantity);
    public String deleteCartItem(Integer cartItemId);
    public String updateCartItem(Integer productId, Integer quantity);
    public CartListVO getCart();
    public OrderVO check();
}
