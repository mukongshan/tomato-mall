package com.example.tomatomall.controller;


import com.example.tomatomall.po.Cart;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.vo.*;
import org.springframework.web.bind.annotation.*;
import com.example.tomatomall.*;

import javax.annotation.Resource;

/**
 * 购物车管理控制器
 * 提供购物车的增删改查、结算等功能
 * 
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Resource
    private CartService cartService;

    /**
     * 添加商品到购物车
     * 
     * @param cartRequest 购物车请求信息
     * @return 购物车项信息
     */
    @PostMapping
    public Response<CartVO> addCart(@RequestBody CartRequestDTO cartRequest) {
        Integer productId = cartRequest.getProductId();
        Integer quantity = cartRequest.getQuantity();
        return Response.buildSuccess(cartService.addCartItem(productId, quantity));
    }

    /**
     * 删除购物车项
     * 
     * @param cartItemId 购物车项ID
     * @return 删除结果
     */
    @DeleteMapping("/{cartItemId}")
    public Response<String> deleteCart(@PathVariable Integer cartItemId) {
        return Response.buildSuccess(cartService.deleteCartItem(cartItemId));
    }

    /**
     * 更新购物车项数量
     * 
     * @param cartItemId 购物车项ID
     * @param cartRequest 包含新数量的请求信息
     * @return 更新结果
     */
    @PatchMapping("/{cartItemId}")
    public Response<String> updateCart(@PathVariable Integer cartItemId, @RequestBody CartRequestDTO cartRequest) {
        Integer quantity = cartRequest.getQuantity();
        return Response.buildSuccess(cartService.updateCartItem(cartItemId, quantity));

    }
    
    /**
     * 获取购物车列表
     * 
     * @return 购物车列表信息
     */
    @GetMapping
    public Response<CartListVO> getCart() {
        return Response.buildSuccess(cartService.getCart());
    }

    /**
     * 购物车结算
     * 
     * @param checkRequestVO 结算请求信息
     * @return 订单信息
     */
    @PostMapping("/checkout")
    public Response<OrderVO> checkout(@RequestBody CheckRequestVO checkRequestVO) {
        return Response.buildSuccess(cartService.check(checkRequestVO));
    }
}
