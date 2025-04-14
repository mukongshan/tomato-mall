package com.example.tomatomall.service.serviceImpl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.tomatomall.enums.PaymentStatusEnum;
import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.Account;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.vo.AccountVO;
import com.example.tomatomall.vo.OrderVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Value("${alipay.serverUrl}")
    private String gatewayUrl;

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
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("订单不存在"));

            AlipayClient alipayClient = new DefaultAlipayClient(
                    gatewayUrl,
                    appId,
                    privateKey,
                    "json",
                    "UTF-8",
                    alipayPublicKey,
                    "RSA2"
            );

            AlipayTradePagePayRequest request = getAlipayTradePagePayRequest(order);

            String form = alipayClient.pageExecute(request).getBody();

            Map<String, Object> result = new HashMap<>();
            result.put("paymentForm", form);
            result.put("orderId", order.getOrderId());
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

    private AlipayTradePagePayRequest getAlipayTradePagePayRequest(Order order) throws JsonProcessingException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
//            request.setReturnUrl(returnUrl);  // 设置支付完成后的跳转页面
        request.setNotifyUrl(notifyUrl);  // 支付宝异步通知回调地址

        Map<String, String> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", String.valueOf(order.getOrderId()));
        bizContent.put("total_amount", String.valueOf(order.getTotalAmount()));
        bizContent.put("subject", "订单编号：" + order.getOrderId());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        // 序列化 Map 为 JSON 字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String bizContentJson = objectMapper.writeValueAsString(bizContent);

        request.setBizContent(bizContentJson);
        return request;
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
            int paidAmount = Integer.parseInt(amount); // 你这边 totalAmount 是 Integer
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

}
