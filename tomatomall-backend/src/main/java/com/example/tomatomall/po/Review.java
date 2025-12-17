package com.example.tomatomall.po;

import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.ReviewVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType type;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "shop_id")
    private Integer shopId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ReviewType {
        PRODUCT,
        SHOP
    }

    public ReviewVO toVO() {
        ReviewVO vo = new ReviewVO();
        vo.setId(id);
        vo.setAccountId(accountId);
        vo.setContent(content);
        vo.setRate(rate);
        vo.setType(type.toString());
        vo.setProductId(productId);
        vo.setShopId(shopId);
        vo.setCreatedAt(createdAt);
        return vo;
    }
}