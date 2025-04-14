package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.enums.PaymentStatusEnum;
import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.Cart;
import com.example.tomatomall.po.Product;
import com.example.tomatomall.repository.CartRepository;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.StockpileRepository;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.vo.CartListVO;
import com.example.tomatomall.vo.CartVO;
import com.example.tomatomall.vo.OrderVO;
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
    public OrderVO check() {
        OrderVO orderVO = new OrderVO();
        orderVO.setAccountId(securityUtil.getCurrentAccount().getId());
        orderVO.setTotalAmount(getCart().getTotalAmount());
        orderVO.setStatus(PaymentStatusEnum.PENDING);
        orderVO.setPaymentMethod("ALIPAY");
        orderVO.setCreateTime(new Date());
        orderRepository.save(orderVO.toPO());
        return orderVO;
    }

}
