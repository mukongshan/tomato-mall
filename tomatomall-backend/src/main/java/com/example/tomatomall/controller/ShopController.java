package com.example.tomatomall.controller;

import com.example.tomatomall.service.ShopService;
import com.example.tomatomall.vo.Response;
import com.example.tomatomall.vo.ShopVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 店铺管理控制器
 * 提供店铺的增删改查等功能
 * 
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    /**
     * 获取所有店铺列表
     * 
     * @return 所有店铺信息列表
     */
    @GetMapping("/all")
    public Response<List<ShopVO>> getAllShops() {
        return Response.buildSuccess(shopService.getAllShops());
    }

    /**
     * 创建新店铺
     * 
     * @param shopVO 店铺信息
     * @return 创建结果
     */
    @PostMapping("/create")
    public Response<String> createShop(@RequestBody ShopVO shopVO) {
        return Response.buildSuccess(shopService.createShop(shopVO));
    }

    /**
     * 获取店铺详细信息
     * 
     * @param shopId 店铺ID
     * @return 店铺详细信息
     */
    @GetMapping("/detail/{shopId}")
    public Response<ShopVO> getShopDetail(@PathVariable Integer shopId) {
        return Response.buildSuccess(shopService.getShopById(shopId));
    }

    /**
     * 更新店铺信息
     * 
     * @param shopId 店铺ID
     * @param shopVO 更新的店铺信息
     * @return 更新结果
     */
    @PutMapping("/update/{shopId}")
    public Response<String> updateShop(@PathVariable Integer shopId, @RequestBody ShopVO shopVO) {
        shopVO.setShopId(shopId);
        return Response.buildSuccess(shopService.updateShop(shopVO));
    }

    /**
     * 删除店铺
     * 
     * @param shopId 店铺ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{shopId}")
    public Response<String> deleteShop(@PathVariable Integer shopId) {
        return Response.buildSuccess(shopService.deleteShop(shopId));
    }

    /**
     * 根据店主ID获得商店ID
     * 
     * @param ownerId 店主ID
     * @return 商店ID
     */
    @GetMapping("/owner/{ownerId}")
    public Response<Integer> getOwnShop(@PathVariable Integer ownerId) {
        return Response.buildSuccess(shopService.getOwnShopId(ownerId));
    }
} 