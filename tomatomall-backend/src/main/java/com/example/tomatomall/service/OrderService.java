package com.example.tomatomall.service;

import com.example.tomatomall.vo.OrderVO;

import java.util.Map;

public interface OrderService {
    public Map<String, Object> requestPayment(Integer orderId);
    public Boolean updateOrderStatus(String aliOrderId, String alipayTradeNo, String amount);
}
