package com.example.tomatomall.controller;


import com.example.tomatomall.po.Cart;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.vo.CartListVO;
import com.example.tomatomall.vo.CartVO;
import com.example.tomatomall.vo.Response;
import lombok.Getter;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {


    @Resource
    private CartService cartService;


    @PostMapping()
    public Response<CartVO> addCart(@RequestBody Integer productId, @RequestBody Integer quantity) {
        return Response.buildSuccess(cartService.addProduct(productId, quantity));
    }


    @DeleteMapping("/{cartItemId}")
    public Response<String> deleteCart(@PathVariable Integer cartItemId) {
        return Response.buildSuccess(cartService.deleteProduct(cartItemId));
    }

    @PatchMapping("/{cartItemId}")
    public Response<String> updateCart(@PathVariable Integer cartItemId, @RequestBody Integer quantity) {
        return Response.buildSuccess(cartService.updateProduct(cartItemId,quantity));
    }
    @GetMapping
    public Response<CartListVO> getCart() {
        return Response.buildSuccess(cartService.getCart());

    }




}
