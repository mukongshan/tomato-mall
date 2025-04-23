package com.example.tomatomall.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.tomatomall.po.Cart;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.vo.CartListVO;
import com.example.tomatomall.vo.CartVO;
import com.example.tomatomall.vo.Response;
import lombok.Getter;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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

}
