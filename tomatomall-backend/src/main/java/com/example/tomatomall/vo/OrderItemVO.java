package com.example.tomatomall.vo;

import com.example.tomatomall.po.OrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemVO {
    private Integer orderItemId;
    private Integer orderId;
    private Integer productId;
    private Integer quantity;
    private double price;
    public OrderItem toPO(){
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(this.orderItemId);
        orderItem.setOrderId(this.orderId);
        orderItem.setProductId(this.productId);
        orderItem.setQuantity(this.quantity);
        orderItem.setPrice(this.price);
        return orderItem;
    }
}
