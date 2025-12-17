package com.example.tomatomall.service;

import com.example.tomatomall.vo.ReviewVO;
import java.util.List;

public interface ReviewService {
    String addProductReview(ReviewVO reviewVO);
    String addShopReview(ReviewVO reviewVO);
    List<ReviewVO> getProductReviews(Integer productId);
    List<ReviewVO> getShopReviews(Integer shopId);
    String deleteReview(Integer reviewId);
}