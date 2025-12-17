package com.example.tomatomall.vo;

import lombok.Data;

import java.util.List;

@Data
public class CheckRequestVO {
    public List<Integer> cartItemIds;
    public OrderInfoVO orderInfo;
    public String paymentMethod;
}
