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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.tomatomall.enums.RoleEnum.*;

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
    public ShopVO getOwnShop(){
        int ownerId = securityUtil.getCurrentAccount().getId();
        Shop shop = shopRepository.findByOwnerId(ownerId);
        if (shop == null){
            return null;
        }
        return shop.toVO();
    }

    @Override
    @Transactional
    public String createShop(ShopVO shopVO) {
        try {
            Account account = securityUtil.getCurrentAccount();
            if ((account.getRole() != CUSTOMER) &&
                    (account.getRole() != admin)) {
                // 只有普通用户和管理员可以创建店铺
                throw TomatoMallException.forbidden();
            }

            shopVO.setIsValid(0);
            Shop shop = shopVO.toPO();
            System.out.println("=====1=====");
            shopRepository.save(shop);
            System.out.println("=====2=====");
            return "创建成功";
        } catch (Exception e) {
            return "创建失败";
        }
    }

    @Override
    public ShopVO getShopById(Integer shopId) {
        Optional<Shop> opShop = shopRepository.findById(shopId);
        if (!opShop.isPresent()) {
            throw TomatoMallException.shopNotExists();
        }
        return opShop.get().toVO();
    }

    @Override
    public String updateShop(ShopVO shopVO) {
        try {
            Optional<Shop> opShop = shopRepository.findById(shopVO.getShopId());
            if (!opShop.isPresent()) {
                throw TomatoMallException.shopNotExists();
            }
            Shop shop = opShop.get();
            Account account = securityUtil.getCurrentAccount();
            // 只有店主或管理员可以更新店铺信息
            if ((account.getRole() != SHOPKEEPER&&account.getRole()!=admin) || !Objects.equals(account.getId(), shop.getOwnerId())){
                throw TomatoMallException.forbidden();
            }
            if (shopVO.getName() != null) shop.setName(shopVO.getName());
            if (shopVO.getOwnerId() != null) shop.setOwnerId(shopVO.getOwnerId());
            if (shopVO.getDescription() != null) shop.setDescription(shopVO.getDescription());
            if (shopVO.getIconUrl() != null) shop.setIconUrl(shopVO.getIconUrl());
            if (shopVO.getIsValid() != null) shop.setIsValid(shopVO.getIsValid());
            shopRepository.save(shop);
            return "更新成功";
        } catch (Exception e) {
            throw new RuntimeException("更新失败");
        }
    }

    @Override
    public String deleteShop(Integer shopId) {
        try {
            Optional<Shop> opShop = shopRepository.findById(shopId);
            if (!opShop.isPresent()) {
                throw TomatoMallException.shopNotExists();
            }
            Shop shop = opShop.get();
            shopRepository.delete(shop);
            return "删除成功";
        } catch (Exception e) {
            throw new RuntimeException("删除失败");
        }
    }
} 