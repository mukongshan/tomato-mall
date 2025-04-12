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
@Table(name = "cart_order_relation")
public class CartOrderRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "cartItemId")
    private Integer cartItemId; // 与 Cart 实体类关联

    @Column(name = "orderId")
    private Integer orderId; // 订单ID

}
