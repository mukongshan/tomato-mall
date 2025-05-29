package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.Review;
import com.example.tomatomall.repository.ReviewRepository;
import com.example.tomatomall.service.ReviewService;
import com.example.tomatomall.vo.ReviewVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewRepository reviewRepository;

    @Override
    @Transactional
    public String addProductReview(ReviewVO reviewVO) {
        Review review = reviewVO.toPO();
        review.setType(Review.ReviewType.PRODUCT);
        review = reviewRepository.save(review);
        return "succ";
    }

    @Override
    @Transactional
    public String addShopReview(ReviewVO reviewVO) {
        Review review = reviewVO.toPO();
        review.setType(Review.ReviewType.SHOP);
        review = reviewRepository.save(review);
        return "succ";
    }

    @Override
    public List<ReviewVO> getProductReviews(Integer productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(Review::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewVO> getShopReviews(Integer shopId) {
        List<Review> reviews = reviewRepository.findByShopId(shopId);
        return reviews.stream()
                .map(Review::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String deleteReview(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(TomatoMallException::reviewNotExists);
        reviewRepository.delete(review);
        return "评论删除成功";
    }

}