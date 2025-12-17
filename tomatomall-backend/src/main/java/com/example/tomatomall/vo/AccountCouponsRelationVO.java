package com.example.tomatomall.vo;

import com.example.tomatomall.po.AccountCouponsRelation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountCouponsRelationVO {

    private Integer id;
    private Integer accountId;
    private Integer couponId;
    private Integer quantity;

    public AccountCouponsRelation toPO() {
        AccountCouponsRelation pool = new AccountCouponsRelation();
        pool.setId(this.id);
        pool.setAccountId(this.accountId);
        pool.setCouponId(this.couponId);
        pool.setQuantity(this.quantity);
        return pool;
    }
}