package com.example.tomatomall.controller;


import com.example.tomatomall.po.Product;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.Response;
import com.example.tomatomall.vo.StockpileVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 商品管理控制器
 * 提供商品的增删改查、库存管理等功能
 * 
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("api/products")
public class ProductController {
    @Resource
    private ProductService productService;

    /**
     * 获取所有商品列表
     * 
     * @return 所有商品信息列表
     */
    @GetMapping
    public Response<List<ProductVO>> getAllProduct() {
        return Response.buildSuccess(productService.getAllProducts());
    }

    /**
     * 根据商品ID获取商品详情
     * 
     * @param id 商品ID
     * @return 商品详细信息
     */
    @GetMapping("/{id}")
    public Response<ProductVO> getProductById(@PathVariable Integer id) {
        return Response.buildSuccess(productService.getProductById(id));
    }
    
    /**
     * 根据店铺ID获取商品列表
     * 
     * @param shopId 店铺ID
     * @return 店铺商品列表
     */
    @GetMapping("/shop/{shopId}")
    public Response<List<ProductVO>> getProductsByShopId(@PathVariable Integer shopId) {
        return Response.buildSuccess(productService.getProductsByShopId(shopId));
    }

    /**
     * 添加新商品
     * 
     * @param productVO 商品信息
     * @return 新创建的商品信息
     */
    @PostMapping
    public Response<ProductVO> addProduct(@RequestBody ProductVO productVO) {
        return Response.buildSuccess(productService.createProduct(productVO));
    }

    /**
     * 更新商品信息
     * 
     * @param productVO 更新的商品信息
     * @return 更新结果
     */
    @PutMapping
    public Response<String> updateProduct(@RequestBody ProductVO productVO) {
        return Response.buildSuccess(productService.updateProduct(productVO));
    }

    /**
     * 删除商品
     * 
     * @param id 商品ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Response<String> deleteProduct(@PathVariable Integer id) {
        return Response.buildSuccess(productService.deleteProduct(id));
    }

    /**
     * 获取商品库存信息
     * 
     * @param productId 商品ID
     * @return 库存信息
     */
    @GetMapping("/stockpile/{productId}")
    public Response<StockpileVO> getProductStock(@PathVariable Integer productId) {
        return Response.buildSuccess(productService.getStockpile(productId));
    }

    /**
     * 更新商品库存
     * 
     * @param productId 商品ID
     * @param body 包含库存数量的请求体
     * @return 更新结果
     */
    @PatchMapping("/stockpile/{productId}")
    public Response<String> updateProductStock(@PathVariable Integer productId,@RequestBody Map<String, Integer> body ) {
        Integer amount = (Integer) body.get("amount");
        return Response.buildSuccess(productService.updateStockpile(productId,amount));
    }

}
