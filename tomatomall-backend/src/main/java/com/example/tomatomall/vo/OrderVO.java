package com.example.tomatomall.vo;

import com.example.tomatomall.enums.PaymentStatusEnum;
import com.example.tomatomall.po.Order;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class OrderVO {
    private Integer orderId;
    private Integer accountId;
    private Integer totalAmount;
    private String paymentMethod;
    private PaymentStatusEnum status;
    private Date createTime;

    public Order toPO() {
        Order order = new Order();
        order.setOrderId(this.orderId);
        order.setAccountId(this.accountId); // 修正为使用 accountId
        order.setTotalAmount(this.totalAmount);
        order.setPaymentMethod(this.paymentMethod);
        order.setStatus(this.status);
        order.setCreateTime(this.createTime);
        return order;
    }
}
