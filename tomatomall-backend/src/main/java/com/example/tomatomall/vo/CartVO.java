package com.example.tomatomall.vo;

import com.example.tomatomall.po.Cart;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartVO {

    private Integer cartItemId;
    private Integer accountId;
    private Integer productId;
    private Integer quantity;
    private String title;
    private Double price;
    private String description;
    private String cover;
    private String detail;

    public Cart toPO(){
        Cart cart = new Cart();
        cart.setCartitemId(this.cartItemId);
        cart.setAccountId(this.accountId);
        cart.setProductId(this.productId);
        cart.setQuantity(this.quantity);
        return cart;
    }

}
