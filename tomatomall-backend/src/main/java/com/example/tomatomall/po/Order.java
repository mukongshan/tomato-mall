package com.example.tomatomall.po;

import com.example.tomatomall.enums.PaymentStatusEnum;
import com.example.tomatomall.vo.CartVO;
import com.example.tomatomall.vo.OrderVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")  // 绑定数据库表
public class Order{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum status;

    @Column(name = "create_time", nullable = false)
    private Date createTime;


    public OrderVO toVO() {
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderId(this.orderId);
        orderVO.setAccountId(this.accountId);
        orderVO.setTotalAmount(this.totalAmount);
        orderVO.setPaymentMethod(this.paymentMethod);
        orderVO.setStatus(this.status);
        orderVO.setCreateTime(this.createTime);
        return orderVO;
    }


}
