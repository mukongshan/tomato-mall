package com.example.tomatomall.controller;

import com.example.tomatomall.service.ReviewService;
import com.example.tomatomall.vo.ReviewVO;
import com.example.tomatomall.vo.Response;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Resource
    private ReviewService reviewService;

    @PostMapping("/product")
    public Response<String> addProductReview(@RequestBody ReviewVO reviewVO) {
        // 把商店id设置为null
        reviewVO.setShopId(null);
        return Response.buildSuccess(reviewService.addProductReview(reviewVO));
    }

    @PostMapping("/shop")
    public Response<String> addShopReview(@RequestBody ReviewVO reviewVO) {
        // 把商品id设置为null
        reviewVO.setProductId(null);
        return Response.buildSuccess(reviewService.addShopReview(reviewVO));
    }

    @GetMapping("/product/{productId}")
    public Response<List<ReviewVO>> getProductReviews(@PathVariable Integer productId) {
        return Response.buildSuccess(reviewService.getProductReviews(productId));
    }

    @GetMapping("/shop/{shopId}")
    public Response<List<ReviewVO>> getShopReviews(@PathVariable Integer shopId) {
        return Response.buildSuccess(reviewService.getShopReviews(shopId));
    }

    @DeleteMapping("/{reviewId}")
    public Response<String> deleteReview(@PathVariable Integer reviewId) {
        return Response.buildSuccess(reviewService.deleteReview(reviewId));
    }



}
