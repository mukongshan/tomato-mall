package com.example.tomatomall.po;

import com.example.tomatomall.vo.CouponVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "coupon")  // 绑定数据库表
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "discount_type", nullable = false)  // 1: 百分比折扣, 2: 固定金额折扣
    private Integer discountType;

    @Column(name = "discount_value", nullable = false)
    private Double discountValue;

    @Column(name = "start_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING) // 格式化为"年-月-日 时:分:秒"
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING) // 格式化为"年-月-日 时:分:秒"
    private LocalDateTime endTime;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "used_quantity", nullable = false)
    private Integer usedQuantity = 0;

    @Column(name = "is_valid", nullable = false)  // 1: 可用, 0: 不可用
    private Integer isValid = 1;

    public CouponVO toVO() {
        CouponVO couponVO = new CouponVO();
        couponVO.setId(this.id);
        couponVO.setName(this.name);
        couponVO.setDescription(this.description);
        couponVO.setDiscountType(this.discountType);
        couponVO.setDiscountValue(this.discountValue);
        couponVO.setStartTime(this.startTime);
        couponVO.setEndTime(this.endTime);
        couponVO.setQuantity(this.quantity);
        couponVO.setUsedQuantity(this.usedQuantity);
        couponVO.setIsValid(this.isValid);
        return couponVO;
    }

}