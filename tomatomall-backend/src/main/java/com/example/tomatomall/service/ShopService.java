package com.example.tomatomall.service;

import com.example.tomatomall.vo.ShopVO;
import java.util.List;

public interface ShopService {
    List<ShopVO> getAllShops();
    Integer getOwnShopId(Integer ownerId);
    String createShop(ShopVO shopVO);
    ShopVO getShopById(Integer shopId);
    String updateShop(ShopVO shopVO);
    String deleteShop(Integer shopId);

} 