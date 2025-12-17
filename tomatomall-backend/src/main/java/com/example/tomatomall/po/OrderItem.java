package com.example.tomatomall.po;

import com.example.tomatomall.vo.OrderItemVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_items")  // 绑定数据库表
public class OrderItem{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id ", nullable = false)
    private Integer orderItemId ;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false)
    private double price;

    public OrderItemVO toVO() {
        OrderItemVO orderItemVO = new OrderItemVO();
        orderItemVO.setOrderItemId(this.orderItemId);
        orderItemVO.setOrderId(this.orderId);
        orderItemVO.setProductId(this.productId);
        orderItemVO.setQuantity(this.quantity);
        orderItemVO.setPrice(this.price);
        return orderItemVO;
    }
}
