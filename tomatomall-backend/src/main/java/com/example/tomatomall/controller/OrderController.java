package com.example.tomatomall.controller;

import com.alipay.api.AlipayApiException;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 订单管理控制器
 * 提供订单查询、支付处理等功能
 * 
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/orders")
@ConfigurationProperties("alipay")
public class OrderController {

    private String alipayPublicKey;

    @Resource
    private OrderService orderService;

    @Resource
    private ProductService productService;

    /**
     * 请求支付
     * 
     * @param orderId 订单ID
     * @return 支付相关信息
     */
    @PostMapping("/{orderId}/pay")
    public Response<Map<String, Object>> requestPayment(@PathVariable Integer orderId) {
        return Response.buildSuccess(orderService.requestPayment(orderId));
    }

    /**
     * 处理支付宝回调通知
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @throws IOException IO异常
     * @throws AlipayApiException 支付宝API异常
     */
    @PostMapping("/alipay/notify")
    public void handleAlipayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException {
        orderService.handleAlipayNotify(request, response);
    }

    /**
     * 获取订单商品项列表
     * 
     * @param orderId 订单ID
     * @return 订单商品项列表
     */
    @GetMapping("/{orderId}")
    public Response<List<OrderItemVO>> getOrderItems(@PathVariable Integer orderId) {
        return Response.buildSuccess(orderService.getOrderItems(orderId));
    }

    /**
     * 根据账户ID获取订单列表
     * 
     * @param accountId 账户ID
     * @return 订单列表
     */
    @GetMapping("/account/{accountId}")
    public Response<List<OrderVO>> getOrders(@PathVariable Integer accountId) {
        return Response.buildSuccess(orderService.getOrders(accountId));
    }
}
