package com.example.tomatomall.vo;

import com.example.tomatomall.po.Cart;
import com.example.tomatomall.po.Review;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReviewVO {

    private Integer id;
    private Integer accountId;
    private String content;
    private BigDecimal rate;
    private String type; // "PRODUCT" or "SHOP"
    private Integer productId;
    private Integer shopId;
    private LocalDateTime createdAt;

    // 可选：额外展示字段
    private String accountName;
    private String productName;
    private String shopName;

    public Review toPO() {
        Review review = new Review();
        review.setId(id);
        review.setAccountId(accountId);
        review.setContent(content);
        review.setRate(rate);
        review.setType(Review.ReviewType.valueOf(type));
        review.setProductId(productId);
        review.setShopId(shopId);
        review.setCreatedAt(createdAt);
        return review;
    }
}