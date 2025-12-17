package com.example.tomatomall.vo;

import com.example.tomatomall.po.CartOrderRelation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartOrderRelationVO {
    private Integer cartItemId;
    private Integer orderId;
    private Integer id;
    public CartOrderRelation toPO(){
        CartOrderRelation cartOrderRelation = new CartOrderRelation();
        cartOrderRelation.setOrderId(orderId);
        cartOrderRelation.setCartItemId(cartItemId);
        return cartOrderRelation;
    }
}
