package com.example.tomatomall.po;

import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.AccountVO;
import com.example.tomatomall.vo.CartVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Resource;
import javax.persistence.*;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "carts")  // 绑定数据库表
public class Cart{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Integer cartItemId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;


    public CartVO toVO() {
        CartVO cartVO = new CartVO();
        cartVO.setCartItemId(this.cartItemId);
        cartVO.setUserId(this.userId);
        cartVO.setProductId(this.productId);
        cartVO.setQuantity(this.quantity);
        return cartVO;
    }


}
