package com.example.tomatomall.controller;

import com.example.tomatomall.service.ReviewService;
import com.example.tomatomall.vo.ReviewVO;
import com.example.tomatomall.vo.Response;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评价管理控制器
 * 提供商品和店铺评价的增删查等功能
 * 
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Resource
    private ReviewService reviewService;

    /**
     * 添加商品评价
     * 
     * @param reviewVO 评价信息
     * @return 添加结果
     */
    @PostMapping("/product")
    public Response<String> addProductReview(@RequestBody ReviewVO reviewVO) {
        // 把商店id设置为null
        reviewVO.setShopId(null);
        return Response.buildSuccess(reviewService.addProductReview(reviewVO));
    }

    /**
     * 添加店铺评价
     * 
     * @param reviewVO 评价信息
     * @return 添加结果
     */
    @PostMapping("/shop")
    public Response<String> addShopReview(@RequestBody ReviewVO reviewVO) {
        // 把商品id设置为null
        reviewVO.setProductId(null);
        return Response.buildSuccess(reviewService.addShopReview(reviewVO));
    }

    /**
     * 获取商品评价列表
     * 
     * @param productId 商品ID
     * @return 评价列表
     */
    @GetMapping("/product/{productId}")
    public Response<List<ReviewVO>> getProductReviews(@PathVariable Integer productId) {
        return Response.buildSuccess(reviewService.getProductReviews(productId));
    }

    /**
     * 获取店铺评价列表
     * 
     * @param shopId 店铺ID
     * @return 评价列表
     */
    @GetMapping("/shop/{shopId}")
    public Response<List<ReviewVO>> getShopReviews(@PathVariable Integer shopId) {
        return Response.buildSuccess(reviewService.getShopReviews(shopId));
    }

    /**
     * 删除评价
     * 
     * @param reviewId 评价ID
     * @return 删除结果
     */
    @DeleteMapping("/{reviewId}")
    public Response<String> deleteReview(@PathVariable Integer reviewId) {
        return Response.buildSuccess(reviewService.deleteReview(reviewId));
    }

}
