package com.example.tomatomall.controller;

import com.example.tomatomall.service.ShopService;
import com.example.tomatomall.vo.Response;
import com.example.tomatomall.vo.ShopVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @GetMapping("/all")
    public Response<List<ShopVO>> getAllShops() {
        return Response.buildSuccess(shopService.getAllShops());
    }



    @PostMapping("/create")
    public Response<String> createShop(@RequestBody ShopVO shopVO) {
        return Response.buildSuccess(shopService.createShop(shopVO));
    }

    @GetMapping("/detail/{shopId}")
    public Response<ShopVO> getShopDetail(@PathVariable Integer shopId) {
        return Response.buildSuccess(shopService.getShopById(shopId));
    }

    @PutMapping("/update/{shopId}")
    public Response<String> updateShop(@PathVariable Integer shopId, @RequestBody ShopVO shopVO) {
        shopVO.setShopId(shopId);
        return Response.buildSuccess(shopService.updateShop(shopVO));
    }

    @DeleteMapping("/delete/{shopId}")
    public Response<String> deleteShop(@PathVariable Integer shopId) {
        return Response.buildSuccess(shopService.deleteShop(shopId));
    }

    // 根据ownerId获得商店id
    @GetMapping("/owner/{ownerId}")
    public Response<Integer> getOwnShop(@PathVariable Integer ownerId) {
        return Response.buildSuccess(shopService.getOwnShopId(ownerId));
    }
} 