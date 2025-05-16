package com.example.tomatomall.service.serviceImpl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.tomatomall.enums.PaymentStatusEnum;
import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.Account;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.AccountVO;
import com.example.tomatomall.vo.OrderVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;

@Service
public class    OrderServiceImpl implements OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductService productService;

    @Autowired
    CartServiceImpl cartService;

    @Value("${alipay.serverUrl}")
    private String serverUrl;

    @Value("${alipay.appId}")
    private String appId;

    @Value("${alipay.appPrivateKey}")
    private String privateKey;

    @Value("${alipay.alipayPublicKey}")
    private String alipayPublicKey;

    @Value("${alipay.notifyUrl}")
    private String notifyUrl;

    public Map<String, Object> requestPayment(Integer orderId) {
        try {
            Optional<Order> opOrder = orderRepository.findById(orderId);
            if(!opOrder.isPresent()){
                throw TomatoMallException.orderNotExists();
            }
            Order order = opOrder.get();

            AlipayClient alipayClient = new DefaultAlipayClient(
                    serverUrl,
                    appId,
                    privateKey,
                    "JSON",
                    "UTF-8",
                    alipayPublicKey,
                    "RSA2"
            );

            AlipayTradePagePayRequest request = getAlipayTradePagePayRequest(order);

            String form = alipayClient.pageExecute(request).getBody();

            Map<String, Object> result = new HashMap<>();
            result.put("paymentForm", form);
            result.put("orderId", order.getId());
            result.put("totalAmount", order.getTotalAmount());
            result.put("paymentMethod", "Alipay");

            return result;

        } catch (JsonProcessingException e) {
            // 处理 JSON 序列化错误
            throw new RuntimeException("JSON 处理异常", e);
        } catch (AlipayApiException e) {
            // 处理支付宝 API 错误
            throw new RuntimeException("支付宝 API 调用异常", e);
        }
    }

    public void handleAlipayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException {

        // 1. 解析支付宝回调参数（通常是 application/x-www-form-urlencoded）
        Map<String, String> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));

        // 2. 验证支付宝签名（防止伪造请求）
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
        if (!signVerified) {
            response.getWriter().print("fail"); // 签名验证失败，返回 fail
            return;
        }

        // 3. 处理业务逻辑（更新订单、减库存等）
        String tradeStatus = params.get("trade_status");
        if (tradeStatus.equals("TRADE_SUCCESS")) {
            String aliOrderIdStr = params.get("out_trade_no"); // 您的订单号
            String alipayTradeNo = params.get("trade_no"); // 支付宝交易号
            String amount = params.get("total_amount"); // 支付金额

            // 更新订单状态（注意幂等性，防止重复处理）
            updateOrderStatus(aliOrderIdStr, alipayTradeNo, amount);

            // 扣减库存（建议加锁或乐观锁）
            productService.reduceStockpileByOrder(aliOrderIdStr);

            //删除购物车
            cartService.deleteCartItemByOrder(aliOrderIdStr);
        }

        // 4. 必须返回纯文本的 "success"（支付宝要求）
        response.getWriter().print("success");
    }


    @Override
    public Boolean updateOrderStatus(String aliOrderId, String alipayTradeNo, String amount) {
        try {
            int orderId = Integer.parseInt(aliOrderId);
            Order order = orderRepository.findById(orderId).orElse(null);

            if (order == null) {
                return false; // 订单不存在
            }

            // 检查订单是否已支付过
            if (order.getStatus() == PaymentStatusEnum.SUCCESS) {
                return true; // 已支付，不重复处理
            }

            // 校验金额是否一致
            double paidAmount = Double.parseDouble(amount); // 你这边 totalAmount 是 Integer
            if (order.getTotalAmount() != paidAmount) {
                return false; // 金额不一致
            }

            // 更新状态为已支付
            order.setStatus(PaymentStatusEnum.SUCCESS);
            // 如果你想记录 alipayTradeNo，请在 Order 类中加字段再 set
            orderRepository.save(order);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private AlipayTradePagePayRequest getAlipayTradePagePayRequest(Order order) throws JsonProcessingException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
//            request.setReturnUrl(returnUrl);  // 设置支付完成后的跳转页面
        request.setNotifyUrl(notifyUrl);  // 支付宝异步通知回调地址

        Map<String, String> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", String.valueOf(order.getId()));
        bizContent.put("total_amount", String.valueOf(order.getTotalAmount()));
        bizContent.put("subject", "订单编号：" + order.getId());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        // 序列化 Map 为 JSON 字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String bizContentJson = objectMapper.writeValueAsString(bizContent);

        request.setBizContent(bizContentJson);
        return request;
    }
}
