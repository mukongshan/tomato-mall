package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.Product;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.SpecificationRepository;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.SpecificationVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class ProductServiceImpl implements ProductService {
    @Resource
    private ProductRepository productRepository;


    @Resource
    private SpecificationRepository specificationRepository;

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
    public String createProduct(ProductVO productVO) {
        Product newProduct = productVO.toPO();
        productRepository.save(newProduct);
        return "创建成功";

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
        List<SpecificationVO> specVOs = specificationRepository.findByProductId(product.getId())
                .stream()
                .map(spec -> {
                    SpecificationVO vo = new SpecificationVO();
                    vo.setId(spec.getId());
                    vo.setItem(spec.getItem());
                    vo.setValue(spec.getValue());
                    vo.setProductId(spec.getProductId());
                    return vo;
                })
                .collect(Collectors.toList());

        productVO.setSpecifications(specVOs);
        return productVO;
    }
}
