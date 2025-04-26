package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.enums.PaymentStatusEnum;
import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.*;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Resource
    private ProductRepository productRepository;

    @Resource
    private CartRepository cartRepository;

    @Resource
    private StockpileRepository stockpileRepository;

    @Resource
    private CartOrderRelationRepository cartOrderRelationRepository;

    @Autowired
    SecurityUtil securityUtil;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public CartVO addCartItem(Integer productId, Integer quantity) {
        CartVO cartVO = new CartVO();

        Product product = productRepository.findById(productId)
                .orElseThrow(TomatoMallException::productNotExists);
        cartVO.setProductId(product.getId());
        cartVO.setDescription(product.getDescription());
        cartVO.setTitle(product.getTitle());
        cartVO.setPrice(product.getPrice());
        cartVO.setCover(product.getCover());
        cartVO.setDetail(product.getDetail());
        cartVO.setAccountId(securityUtil.getCurrentAccount().getId());
        if (stockpileRepository.findByProductId(productId) == null) {
            throw TomatoMallException.productNotExists();
        }
        if (quantity > stockpileRepository.findByProductId(productId).getAmount()) {
            throw TomatoMallException.overStock();
        }
        cartVO.setQuantity(quantity);
        cartRepository.save(cartVO.toPO());
        return cartVO;
    }

    @Override
    public String deleteCartItem(Integer cartItemId) {
        if (cartRepository.findByCartItemId(cartItemId) == null) {
            throw TomatoMallException.cartNotExists();
        }
        cartRepository.deleteById(cartItemId);
        return "删除成功";
    }

    @Override
    public String updateCartItem(Integer cartItemId, Integer quantity) {
        Cart cartItem = cartRepository.findByCartItemId(cartItemId);
        if (cartItem == null) {
            throw TomatoMallException.cartNotExists();
        }
        Integer productId = cartItem.getProductId();
        if (quantity > stockpileRepository.findByProductId(productId).getAmount()) {
            throw TomatoMallException.overStock();
        }
        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem);
        return "修改数量成功";
    }

    @Override
    public CartListVO getCart() {
        Integer accountId = securityUtil.getCurrentAccount().getId();
        List<CartVO> cartItemListVO = new ArrayList<>();
        double totalAmount = 0;

        List<Cart> cartItems = cartRepository.findByAccountId(accountId);
        for (Cart cart : cartItems) {
            Integer productId = cart.getProductId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(TomatoMallException::productNotExists);
            CartVO itemVO = new CartVO();
            itemVO.setCartItemId(cart.getCartItemId());
            itemVO.setProductId(product.getId());
            itemVO.setTitle(product.getTitle());
            itemVO.setPrice(product.getPrice());
            itemVO.setCover(product.getCover());
            itemVO.setDescription(product.getDescription());
            itemVO.setDetail(product.getDetail());
            itemVO.setQuantity(cart.getQuantity());

            // 单项小计 = 单价 * 数量
            totalAmount += product.getPrice() * cart.getQuantity();
            cartItemListVO.add(itemVO);
        }
        CartListVO vo = new CartListVO();
        vo.setCartItems(cartItemListVO);
        vo.setTotalAmount(totalAmount);
        return vo;
    }

    @Override
    public OrderVO check(CheckRequestVO checkRequestVO) {
        OrderVO orderVO = new OrderVO();
        orderVO.setAccountId(securityUtil.getCurrentAccount().getId());
        orderVO.setTotalAmount(getCart().getTotalAmount());
        orderVO.setStatus(PaymentStatusEnum.PENDING);
        orderVO.setPaymentMethod("ALIPAY");
        orderVO.setCreateTime(new Date());
        Order order = orderRepository.save(orderVO.toPO());
        for (int i = 0; i < checkRequestVO.cartItemIds.size(); i++){
            Integer cartItemId = checkRequestVO.cartItemIds.get(i);
            CartOrderRelationVO cartOrderRelationVO = new CartOrderRelationVO();
            cartOrderRelationVO.setOrderId(order.getOrderId());
            cartOrderRelationVO.setCartItemId(cartItemId);
            cartOrderRelationRepository.save(cartOrderRelationVO.toPO());
        }
        return order.toVO();
    }

    @Override
    public String deleteCartItemByOrder(String orderIdStr) {
        int orderId = Integer.parseInt(orderIdStr);

        // 1. 获取订单下所有 cartItem 关联
        List<CartOrderRelation> relationList = cartOrderRelationRepository.findByOrderId(orderId);

        // 2. 遍历每一项关系
        for (CartOrderRelation relation : relationList) {
            Integer cartItemId = relation.getCartItemId();

            cartOrderRelationRepository.delete(relation);
            deleteCartItem(cartItemId);

        }

        return "删除购物车成功";
    }

}
