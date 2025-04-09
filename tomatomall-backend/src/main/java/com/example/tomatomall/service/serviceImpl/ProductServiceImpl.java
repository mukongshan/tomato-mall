package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.Specification;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.SpecificationRepository;
import com.example.tomatomall.repository.StockpileRepository;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.util.OssUtil;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.SpecificationVO;
import com.example.tomatomall.vo.StockpileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class ProductServiceImpl implements ProductService {
    @Resource
    private ProductRepository productRepository;

    @Autowired
    private OssUtil ossUtil = new OssUtil();

    @Resource
    private SpecificationRepository specificationRepository;

    @Resource
    private StockpileRepository stockpileRepository;

    @Override
    public List<ProductVO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::convertToVO).collect(Collectors.toList());

    }


    @Override
    public ProductVO getProductById(int id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            throw TomatoMallException.productNotExists();
        }
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
                .orElse(null);
        if (product == null) {
            throw TomatoMallException.productNotExists();
        }
        String oldCover = product.getCover();

        if (productVO.getTitle() != null) product.setTitle(productVO.getTitle());
        if (productVO.getPrice() != null) product.setPrice(productVO.getPrice());
        if (productVO.getRate() != null) product.setRate(productVO.getRate());
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
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            throw TomatoMallException.productNotExists();
        }
        // 删除商品封面
        if (product.getCover()!=null&&!product.getCover().isEmpty()) {
            String res  = ossUtil.deleteFileByUrl(product.getCover());
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
        return "调整库存成功";
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
