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

@RestController
@RequestMapping("/api/orders")
@ConfigurationProperties("alipay")
public class OrderController {

    private String alipayPublicKey;

    @Resource
    private OrderService orderService;

    @Resource
    private ProductService productService;

    @PostMapping("/{orderId}/pay")
    public Response<Map<String, Object>> requestPayment(@PathVariable Integer orderId) {
        return Response.buildSuccess(orderService.requestPayment(orderId));
    }

    @PostMapping("/alipay/notify")
    public void handleAlipayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException {
        orderService.handleAlipayNotify(request, response);
    }

    @GetMapping("/{orderId}")
    public Response<List<OrderItemVO>> getOrderItems(@PathVariable Integer orderId) {
        return Response.buildSuccess(orderService.getOrderItems(orderId));
    }

    @GetMapping("/account/{accountId}")
    public Response<List<OrderVO>> getOrders(@PathVariable Integer accountId) {
        return Response.buildSuccess(orderService.getOrders(accountId));
    }

    @PostMapping("/cancel/{orderId}")
    public Response<Void> cancelOrder(@PathVariable Integer orderId) {
        return Response.buildSuccess(orderService.cancelOrder(orderId));
    }
}
