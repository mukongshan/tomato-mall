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
import com.example.tomatomall.po.OrderItem;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.repository.OrderItemRepository;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.AccountVO;
import com.example.tomatomall.vo.OrderItemVO;
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
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;

/**
 * 订单服务实现类
 * 实现订单的支付、状态更新、订单项查询等功能
 * 集成支付宝支付功能，包括支付请求、异步通知处理等
 *
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    ProductService productService;

    @Autowired
    CartServiceImpl cartService;

    // ==================== 支付宝配置参数 ====================
    // 从application.yml配置文件中读取支付宝相关配置
    
    @Value("${alipay.serverUrl}")
    private String serverUrl;        // 支付宝网关地址，如：https://openapi.alipay.com/gateway.do

    @Value("${alipay.appId}")
    private String appId;            // 支付宝应用ID，在支付宝开放平台申请获得

    @Value("${alipay.appPrivateKey}")
    private String privateKey;       // 应用私钥，用于签名请求参数

    @Value("${alipay.alipayPublicKey}")
    private String alipayPublicKey;  // 支付宝公钥，用于验证支付宝回调签名

    @Value("${alipay.notifyUrl}")
    private String notifyUrl;        // 支付宝异步通知回调地址

    /**
     * 请求支付宝支付
     * 生成支付宝支付表单，用户可以在表单中完成支付
     * 
     * @param orderId 订单ID
     * @return 包含支付表单HTML的Map对象
     */
    public Map<String, Object> requestPayment(Integer orderId) {
        try {
            // 1. 验证订单是否存在
            Optional<Order> opOrder = orderRepository.findById(orderId);
            if(!opOrder.isPresent()){
                throw TomatoMallException.orderNotExists();
            }
            Order order = opOrder.get();

            // 2. 创建支付宝客户端
            // DefaultAlipayClient是支付宝SDK提供的客户端，用于与支付宝API交互
            AlipayClient alipayClient = new DefaultAlipayClient(
                    serverUrl,        // 支付宝网关地址
                    appId,            // 应用ID
                    privateKey,       // 应用私钥
                    "JSON",           // 请求格式
                    "UTF-8",          // 字符编码
                    alipayPublicKey,  // 支付宝公钥
                    "RSA2"            // 签名算法
            );

            // 3. 构建支付宝支付请求
            AlipayTradePagePayRequest request = getAlipayTradePagePayRequest(order);

            // 4. 调用支付宝API，获取支付表单HTML
            // pageExecute方法会返回一个包含支付表单的HTML页面
            String form = alipayClient.pageExecute(request).getBody();

            // 5. 封装返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("paymentForm", form);           // 支付表单HTML
            result.put("orderId", order.getOrderId()); // 订单ID
            result.put("totalAmount", order.getTotalAmount()); // 订单总金额
            result.put("paymentMethod", "Alipay");     // 支付方式

            return result;

        } catch (JsonProcessingException e) {
            // 处理 JSON 序列化错误
            throw new RuntimeException("JSON 处理异常", e);
        } catch (AlipayApiException e) {
            // 处理支付宝 API 错误
            throw new RuntimeException("支付宝 API 调用异常", e);
        }
    }

    /**
     * 处理支付宝异步通知
     * 支付宝在用户支付完成后，会向notifyUrl发送POST请求，通知支付结果
     * 此方法用于处理支付宝的回调通知，更新订单状态
     * 
     * @param request HTTP请求对象，包含支付宝回调参数
     * @param response HTTP响应对象，需要返回处理结果给支付宝
     * @throws IOException IO异常
     * @throws AlipayApiException 支付宝API异常
     */
    public void handleAlipayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException {
        // 1. 解析支付宝回调参数
        // 支付宝回调参数是form表单格式，需要转换为Map
        Map<String, String> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));

        // 2. 验证支付宝签名
        // 使用支付宝公钥验证回调参数的签名，确保请求来自支付宝
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
        if (!signVerified) {
            // 签名验证失败，返回fail给支付宝
            response.getWriter().print("fail");
            return;
        }

        // 3. 处理业务逻辑（更新订单、减库存等）
        String tradeStatus = params.get("trade_status"); // 交易状态
        if (tradeStatus.equals("TRADE_SUCCESS")) {
            // 支付成功，处理订单
            String aliOrderIdStr = params.get("out_trade_no"); // 商户订单号（我们的订单ID）
            String alipayTradeNo = params.get("trade_no");     // 支付宝交易号
            String amount = params.get("total_amount");        // 支付金额

            // 更新订单状态
            updateOrderStatus(aliOrderIdStr, alipayTradeNo, amount);

            // 扣减商品库存
            productService.reduceStockpileByOrder(aliOrderIdStr);

            // 删除购物车中已购买的商品
            cartService.deleteCartItemByOrder(aliOrderIdStr);
        }

        // 4. 必须返回纯文本的 "success"（支付宝要求）
        // 支付宝要求异步通知处理成功后必须返回"success"字符串
        response.getWriter().print("success");
    }

    /**
     * 更新订单状态为已支付
     * 在收到支付宝支付成功通知后调用，更新订单状态
     * 
     * @param aliOrderId 订单号字符串
     * @param alipayTradeNo 支付宝交易号
     * @param amount 支付金额字符串
     * @return 是否更新成功
     */

    private Boolean updateOrderStatus(String aliOrderId, String alipayTradeNo, String amount) {
        try {
            // 1. 将订单号字符串转换为整数
            int orderId = Integer.parseInt(aliOrderId);
            
            // 2. 查询订单是否存在
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return false; // 订单不存在
            }

            // 3. 检查订单是否已支付（防止重复处理）
            if (order.getStatus() == PaymentStatusEnum.SUCCESS) {
                return true; // 已支付，不重复处理
            }

            // 4. 校验支付金额是否与订单金额一致
            double paidAmount = Double.parseDouble(amount);
            if (order.getTotalAmount() != paidAmount) {
                return false; // 金额不一致，可能存在异常
            }

            // 5. 更新订单状态为已支付
            order.setStatus(PaymentStatusEnum.SUCCESS);

            // 注意：如需记录支付宝交易号，请在Order类中添加相应字段
            orderRepository.save(order);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 构建支付宝支付请求对象
     * 根据订单信息构建支付宝API所需的请求参数
     * 
     * @param order 订单对象
     * @return 支付宝支付请求对象
     * @throws JsonProcessingException JSON处理异常
     */
    private AlipayTradePagePayRequest getAlipayTradePagePayRequest(Order order) throws JsonProcessingException {
        // 创建支付宝页面支付请求对象
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        
        // 设置支付宝异步通知回调地址
        // 用户支付完成后，支付宝会向此地址发送POST请求
        request.setNotifyUrl(notifyUrl);

        // 构建业务参数
        Map<String, String> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", String.valueOf(order.getOrderId())); // 商户订单号
        bizContent.put("total_amount", String.valueOf(order.getTotalAmount())); // 订单总金额
        bizContent.put("subject", "订单编号：" + order.getOrderId()); // 订单标题
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY"); // 产品代码，固定值

        // 将业务参数序列化为JSON字符串
        // 支付宝API要求bizContent必须是JSON格式的字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String bizContentJson = objectMapper.writeValueAsString(bizContent);

        // 设置业务参数
        request.setBizContent(bizContentJson);
        return request;
    }

    /**
     * 获取订单项列表
     * @param orderId 订单ID
     * @return 订单项VO列表
     */
    @Override
    public List<OrderItemVO> getOrderItems(Integer orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        List<OrderItemVO> orderItemsVO = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            orderItemsVO.add(orderItem.toVO());
        }
        return orderItemsVO;
    }

    /**
     * 根据账户ID获取订单列表
     * @param accountId 账户ID
     * @return 订单VO列表
     */
    @Override
    public List<OrderVO> getOrders(Integer accountId) {
        List<Order> orders = orderRepository.findByAccountId(accountId);
        List<OrderVO> ordersVO = new ArrayList<>();
        for (Order order : orders) {
            ordersVO.add(order.toVO());
        }
        return ordersVO;
    }

    @Override
    public Void cancelOrder(Integer orderId) {
        Optional<Order> opOrder = orderRepository.findById(orderId);
        if(!opOrder.isPresent()){
            throw TomatoMallException.orderNotExists();
        }
        Order order = opOrder.get();
        order.setStatus(PaymentStatusEnum.FAILED);
        orderRepository.save(order);
        return null;
    }
}
