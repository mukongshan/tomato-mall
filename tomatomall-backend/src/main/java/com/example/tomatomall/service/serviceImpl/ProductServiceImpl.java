package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.*;
import com.example.tomatomall.service.MessageService;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.util.OssUtil;
import com.example.tomatomall.vo.MessageVO;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.SpecificationVO;
import com.example.tomatomall.vo.StockpileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class ProductServiceImpl implements ProductService {
    @Resource
    private ProductRepository productRepository;

    @Autowired
    private OssUtil ossUtil;

    @Resource
    private SpecificationRepository specificationRepository;

    @Resource
    private StockpileRepository stockpileRepository;

    @Resource
    private CartRepository cartRepository;

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private CartOrderRelationRepository cartOrderRelationRepository;

    @Resource
    private ShopRepository shopRepository;

    @Resource
    private MessageService messageService;

    private int stockpileAlert = 20;

    @Override
    public List<ProductVO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    @Override
    public List<ProductVO> getProductsByShopId(int shopId) {
        List<Product> products = productRepository.findByShopId(shopId).get();
        return products.stream().map(this::convertToVO).collect(Collectors.toList());
    }


    @Override
    public ProductVO getProductById(int id) {
        Product product = productRepository.findById(id)
                .orElseThrow(TomatoMallException::productNotExists);
        return convertToVO(product);
    }


    @Override
    public ProductVO createProduct(ProductVO productVO) {
        Product newProduct = productVO.toPO();
        productRepository.save(newProduct);
        Stockpile stockpile = new Stockpile();
        stockpile.setProductId(newProduct.getId());
        stockpile.setFrozen(0);
        stockpile.setAmount(0);
        stockpileRepository.save(stockpile);
        return convertToVO(newProduct);
    }

    @Override
    @Transactional
    public String updateProduct(ProductVO productVO) {
        Product product = productRepository.findById(productVO.getId())
                .orElseThrow(TomatoMallException::productNotExists);
        String oldCover = product.getCover();

        if (productVO.getTitle() != null) product.setTitle(productVO.getTitle());
        if (productVO.getPrice() != null) product.setPrice(productVO.getPrice());
        //if (productVO.getRate() != null) product.setRate(productVO.getRate());
        if (productVO.getDescription() != null) product.setDescription(productVO.getDescription());
        if (productVO.getCover() != null) product.setCover(productVO.getCover());
        if (productVO.getDetail() != null) product.setDetail(productVO.getDetail());

        // 处理封面更新
        if (productVO.getCover() != null && !productVO.getCover().equals(oldCover)) {
            product.setCover(productVO.getCover());
            // 删除旧封面
            if (oldCover != null && !oldCover.isEmpty()) {
                ossUtil.deleteFileByUrl(oldCover);
            }
        }

        if(productVO.getSpecifications() != null){
            specificationRepository.deleteByProductId(productVO.getId());
            List<SpecificationVO> specifications = productVO.getSpecifications();
            for (SpecificationVO specificationVO : specifications) {
                specificationRepository.save(specificationVO.toPO());
            }
        }
        productRepository.save(product);
        return "更新成功";

    }

    @Override
    @Transactional
    public String deleteProduct(int id) {
        Product product = productRepository.findById(id)
                .orElseThrow(TomatoMallException::productNotExists);        // 删除商品封面
        if (product.getCover()!=null&&!product.getCover().isEmpty()) {
            String res = ossUtil.deleteFileByUrl(product.getCover());
            System.out.println(res);
        }
        stockpileRepository.deleteByProductId(id);
        specificationRepository.deleteByProductId(id);
        productRepository.delete(product);

        return "删除成功";
    }

    @Override
    public StockpileVO getStockpile(int id) {
        Stockpile stockpile = stockpileRepository.findByProductId(id);
        if (stockpile == null) {
            throw TomatoMallException.productNotExists();
        }
        return stockpile.toVO();
    }

    @Override
    public String updateStockpile(int id,int amount) {
        Stockpile stockpile = stockpileRepository.findByProductId(id);
        if (stockpile == null) {
            throw TomatoMallException.productNotExists();
        }
        stockpile.setAmount(amount);
        stockpileRepository.save(stockpile);
        if (amount <= stockpileAlert){
            Optional<Product> opProduct = productRepository.findById(stockpile.getProductId());
            if (!opProduct.isPresent()){
                throw TomatoMallException.productNotExists();
            }
            Product product = opProduct.get();
            Optional<Shop> opShop = shopRepository.findById(product.getShopId());
            if (!opShop.isPresent()){
                throw TomatoMallException.shopNotExists();
            }
            Shop shop = opShop.get();
            int ownerId = shop.getOwnerId();

            MessageVO messageVO = new MessageVO();
            messageVO.setContent("LOW_INVENTORY");
            messageVO.setIsRead(false);
            messageVO.setToUser(ownerId);
            messageVO.setCreatedTime(LocalDateTime.now());

            messageService.sendMessage(messageVO);
        }
        return "调整库存成功";
    }

    @Override
    public String increaseStockpile(int id, int amount){
        StockpileVO stockpileVO = getStockpile(id);
        updateStockpile(id, stockpileVO.getAmount()+amount);
        return "添加库存成功";
    }

    @Override
    public String reduceStockpile(int id, int amount){
        StockpileVO stockpileVO = getStockpile(id);
        updateStockpile(id, stockpileVO.getAmount()-amount);
        return "减少库存成功";
    }


    @Override
    public String reduceStockpileByOrder(String orderIdStr) {
        int orderId = Integer.parseInt(orderIdStr);

        // 1. 获取订单下所有 cartItem 关联
        List<CartOrderRelation> relationList = cartOrderRelationRepository.findByOrderId(orderId);

        // 2. 遍历每一项关系
        for (CartOrderRelation relation : relationList) {
            Integer cartItemId = relation.getCartItemId();


            // 3. 查找对应 Cart
            Optional<Cart> opCart = cartRepository.findById(cartItemId);
            if (!opCart.isPresent()) {
                throw new RuntimeException("找不到 cartItemId = " + cartItemId);
            }
            Cart cart = opCart.get();

            // 4. 查找对应 Product
            Integer productId = cart.getProductId();
            Stockpile stockpile = stockpileRepository.findByProductId(productId);
            if (stockpile == null){
                throw new RuntimeException("找不到 stockpile 的 productId = " + productId);
            }

            // 5. 扣除库存
            int newStock = stockpile.getAmount() - cart.getQuantity();
            if (newStock < 0) {
                throw new RuntimeException("库存不足，productId = " + productId);
            }
            this.updateStockpile(stockpile.getStockpileId(), newStock);

        }

        return "订单库存已全部扣除成功";
    }





    private ProductVO convertToVO(Product product) {
        ProductVO productVO = new ProductVO();
        productVO.setId(product.getId());
        productVO.setTitle(product.getTitle());
        productVO.setPrice(product.getPrice());
        productVO.setRate(product.getRate());
        productVO.setDescription(product.getDescription());
        productVO.setCover(product.getCover());
        productVO.setDetail(product.getDetail());

        // 通过 productId 查询规格信息，并转换成 VO
        List<Specification> specs = specificationRepository.findByProductId(product.getId());

        if(!specs.isEmpty()) {
            List<SpecificationVO> specVOs = specs.stream()
                    .map(Specification::toVO)
                    .collect(Collectors.toList());
            productVO.setSpecifications(specVOs);
        }
        return productVO;
    }


}
