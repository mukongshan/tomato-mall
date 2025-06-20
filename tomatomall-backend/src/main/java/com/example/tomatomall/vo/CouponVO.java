package com.example.tomatomall.vo;

import com.example.tomatomall.po.Coupon;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CouponVO {

    private Integer id;
    private String name;
    private String description;
    private Integer discountType;  // 1: 百分比折扣, 2: 固定金额折扣
    private Double discountValue;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quantity;
    private Integer usedQuantity;
    private Integer isValid;

    public Coupon toPO() {
        Coupon coupons = new Coupon();
        coupons.setId(this.id);
        coupons.setName(this.name);
        coupons.setDescription(this.description);
        coupons.setDiscountType(this.discountType);
        coupons.setDiscountValue(this.discountValue);
        coupons.setStartTime(this.startTime);
        coupons.setEndTime(this.endTime);
        coupons.setQuantity(this.quantity);
        coupons.setUsedQuantity(this.usedQuantity);
        coupons.setIsValid(this.isValid);
        return coupons;
    }

}