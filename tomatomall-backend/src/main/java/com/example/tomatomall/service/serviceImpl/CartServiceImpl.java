package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.enums.PaymentStatusEnum;
import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.Cart;
import com.example.tomatomall.po.Product;
import com.example.tomatomall.repository.CartRepository;
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

    @Override
    public CartVO addProduct(Integer productId, Integer quantity) {
        CartVO cartVO = new CartVO();

        Product product = productRepository.findById(productId).get();
        cartVO.setProductId(product.getProductId());
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
    public String deleteProduct(Integer cartItemId) {
        if (!cartRepository.existsById(cartItemId)) {
            throw TomatoMallException.cartNotExists();
        }
        cartRepository.deleteById(cartItemId);
        return "删除成功";
    }
    @Override
    public String updateProduct(Integer cartItemId, Integer quantity) {
        if (!cartRepository.existsById(cartItemId)) {
            throw TomatoMallException.cartNotExists();
        }
        Integer productId = cartRepository.findById(cartItemId).get().getProductId();

        Product product = productRepository.findById(productId).get();
        if (quantity > stockpileRepository.findByProductId(productId).getAmount()) {
            throw TomatoMallException.overStock();
        }
        Cart cart = cartRepository.findByCartItemId(cartItemId);
        cart.setQuantity(quantity);
        cartRepository.save(cart);
        return "修改数量成功";
    }

    @Override
    public CartListVO getCart() {
        Integer userId = securityUtil.getCurrentAccount().getId();
        List<CartVO> itemVOList = new ArrayList<>();
        double totalAmount = 0;

        List<Cart> cartItems = cartRepository.findByAccountId(userId);
        for (Cart cart : cartItems) {
            Product product = productRepository.findById(cart.getProductId())
                    .orElseThrow(() -> new TomatoMallException("商品不存在"));

            CartVO itemVO = new CartVO();
            itemVO.setCartItemId(cart.getCartItemId());
            itemVO.setProductId(product.getProductId());
            itemVO.setTitle(product.getTitle());
            itemVO.setPrice(product.getPrice());
            itemVO.setCover(product.getCover());
            itemVO.setDescription(product.getDescription());
            itemVO.setDetail(product.getDetail());
            itemVO.setQuantity(cart.getQuantity());

            // 单项小计 = 单价 * 数量
            totalAmount += product.getPrice() * cart.getQuantity();
            itemVOList.add(itemVO);
        }
        CartListVO vo = new CartListVO();
        vo.setItems(itemVOList);
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
        return orderVO;
    }







}
