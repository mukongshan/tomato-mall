package com.example.tomatomall.po;

import com.example.tomatomall.vo.AccountCouponsRelationVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "account_coupons_relation")
public class AccountCouponsRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "coupon_id", nullable = false)
    private Integer couponId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;


    public AccountCouponsRelationVO toPO() {
        AccountCouponsRelationVO pool = new AccountCouponsRelationVO();
        pool.setId(this.id);
        pool.setAccountId(this.accountId);
        pool.setCouponId(this.couponId);
        pool.setQuantity(this.quantity);
        return pool;
    }
}