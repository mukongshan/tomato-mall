package com.example.tomatomall.po;

import com.example.tomatomall.vo.CartVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "carts")  // 绑定数据库表
public class Cart{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;


    public CartVO toVO() {
        CartVO cartVO = new CartVO();
        cartVO.setCartItemId(this.id);
        cartVO.setAccountId(this.accountId);
        cartVO.setProductId(this.productId);
        cartVO.setQuantity(this.quantity);
        return cartVO;
    }

}
