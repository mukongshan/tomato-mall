package com.example.tomatomall.service;

import com.alipay.api.AlipayApiException;
import com.example.tomatomall.vo.OrderItemVO;
import com.example.tomatomall.vo.OrderVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface OrderService {
    public Map<String, Object> requestPayment(Integer orderId);
    public Boolean updateOrderStatus(String aliOrderId, String alipayTradeNo, String amount);
    public void handleAlipayNotify(HttpServletRequest request, HttpServletResponse response)throws IOException, AlipayApiException;
    public List<OrderItemVO> getOrderItems(Integer orderId);
    public List<OrderVO> getOrders(Integer accountId);
}
