package com.example.tomatomall.controller;


import com.example.tomatomall.po.Cart;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.vo.*;
import org.springframework.web.bind.annotation.*;
import com.example.tomatomall.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Resource
    private CartService cartService;

    @PostMapping
    public Response<CartVO> addCart(@RequestBody CartRequestDTO cartRequest) {
        Integer productId = cartRequest.getProductId();
        Integer quantity = cartRequest.getQuantity();
        return Response.buildSuccess(cartService.addCartItem(productId, quantity));
    }

    @DeleteMapping("/{cartItemId}")
    public Response<String> deleteCart(@PathVariable Integer cartItemId) {
        return Response.buildSuccess(cartService.deleteCartItem(cartItemId));
    }

    @PatchMapping("/{cartItemId}")
    public Response<String> updateCart(@PathVariable Integer cartItemId, @RequestBody CartRequestDTO cartRequest) {
        Integer quantity = cartRequest.getQuantity();
        return Response.buildSuccess(cartService.updateCartItem(cartItemId, quantity));

    }
    @GetMapping
    public Response<CartListVO> getCart() {
        return Response.buildSuccess(cartService.getCart());
    }

    @PostMapping("/checkout")
    public Response<OrderVO> checkout(@RequestBody CheckRequestVO checkRequestVO) {
        return Response.buildSuccess(cartService.check(checkRequestVO));
    }
}
