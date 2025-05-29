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
@Table(name = "carts_orders_relation")
public class CartOrderRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carts_orders_relation_id")
    private Integer cartsOrdersRelationId; // 关联ID

    @Column(name = "cartitem_id")
    private Integer cartItemId; // 与 Cart 实体类关联

    @Column(name = "order_id")
    private Integer orderId; // 订单ID

}
