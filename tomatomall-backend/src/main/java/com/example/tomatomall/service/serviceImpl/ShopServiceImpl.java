package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Account;
import com.example.tomatomall.po.Shop;
import com.example.tomatomall.repository.ShopRepository;
import com.example.tomatomall.service.ShopService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.vo.ShopVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.tomatomall.exception.TomatoMallException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.tomatomall.enums.RoleEnum.SHOPKEEPER;

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @Override
    public List<ShopVO> getAllShops() {
        List<Shop> shops = shopRepository.findAll();
        return shops.stream()
                .map(Shop::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String createShop(ShopVO shopVO) {
        try {
            Account account = securityUtil.getCurrentAccount();
            if (account.getRole() != SHOPKEEPER){
                throw TomatoMallException.forbidden();
            }
            Shop shop = shopVO.toPO();
            shop.setIsValid(0);
            shopRepository.save(shop);
            return "创建成功";
        } catch (Exception e) {
            throw new RuntimeException("创建失败");
        }
    }

    @Override
    public ShopVO getShopById(Integer shopId) {
        Shop shop = shopRepository.findByShopId(shopId);
        if (shop == null) {
            throw TomatoMallException.shopNotExists();
        }
        return shop.toVO();
    }

    @Override
    public String updateShop(ShopVO shopVO) {
        try {
            Shop shop = shopRepository.findByShopId(shopVO.getShopId());
            if (shop == null) {
                throw TomatoMallException.shopNotExists();
            }
            Account account = securityUtil.getCurrentAccount();
            if (account.getRole() != SHOPKEEPER || !Objects.equals(account.getId(), shop.getOwnerId())){
                throw TomatoMallException.forbidden();
            }
            if (shopVO.getName() != null) shop.setName(shopVO.getName());
            if (shopVO.getOwnerId() != null) shop.setOwnerId(shopVO.getOwnerId());
            if (shopVO.getDescription() != null) shop.setDescription(shopVO.getDescription());
            if (shopVO.getIconUrl() != null) shop.setIconUrl(shopVO.getIconUrl());
            shopRepository.save(shop);
            return "更新成功";
        } catch (Exception e) {
            throw new RuntimeException("更新失败");
        }
    }

    @Override
    public String deleteShop(Integer shopId) {
        try {
            Shop shop = shopRepository.findByShopId(shopId);
            if (shop == null) {
                throw TomatoMallException.shopNotExists();
            }
            shopRepository.delete(shop);
            return "删除成功";
        } catch (Exception e) {
            throw new RuntimeException("删除失败");
        }
    }
} 