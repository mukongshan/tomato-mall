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

/**
 * 评价服务实现类
 * 实现商品和店铺评价的增删查等功能
 *
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewRepository reviewRepository;

    /**
     * 添加商品评价
     * @param reviewVO 评价VO
     * @return 添加结果
     */
    @Override
    @Transactional
    public String addProductReview(ReviewVO reviewVO) {
        Review review = reviewVO.toPO();
        review.setType(Review.ReviewType.PRODUCT);
        review = reviewRepository.save(review);
        return "succ";
    }

    /**
     * 添加店铺评价
     * @param reviewVO 评价VO
     * @return 添加结果
     */
    @Override
    @Transactional
    public String addShopReview(ReviewVO reviewVO) {
        Review review = reviewVO.toPO();
        review.setType(Review.ReviewType.SHOP);
        review = reviewRepository.save(review);
        return "succ";
    }

    /**
     * 获取商品评价列表
     * @param productId 商品ID
     * @return 评价VO列表
     */
    @Override
    public List<ReviewVO> getProductReviews(Integer productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(Review::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取店铺评价列表
     * @param shopId 店铺ID
     * @return 评价VO列表
     */
    @Override
    public List<ReviewVO> getShopReviews(Integer shopId) {
        List<Review> reviews = reviewRepository.findByShopId(shopId);
        return reviews.stream()
                .map(Review::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 删除评价
     * @param reviewId 评价ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public String deleteReview(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(TomatoMallException::reviewNotExists);
        reviewRepository.delete(review);
        return "评价删除成功";
    }

}