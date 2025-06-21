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


import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.tomatomall.enums.RoleEnum.*;

/**
 * 店铺服务实现类
 * 实现店铺的增删改查等功能
 *
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@Service
public class ShopServiceImpl implements ShopService {
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 获取所有店铺列表
     * @return 店铺VO列表
     */
    @Override
    public List<ShopVO> getAllShops() {
        List<Shop> shops = shopRepository.findAll();
        return shops.stream()
                .map(Shop::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据店主ID获取店铺ID
     * @param ownerId 店主ID
     * @return 店铺ID
     */
    @Override
    public Integer getOwnShopId(Integer ownerId) {
        Shop shop = shopRepository.findByOwnerId(ownerId);
        if (shop==null) {
            return 0; // 如果没有找到店铺，返回0
        }
        return shop.getId();  // 返回第一个店铺的ID
    }

    /**
     * 创建新店铺
     * @param shopVO 店铺VO
     * @return 创建结果
     */
    @Override
    @Transactional
    public String createShop(ShopVO shopVO) {
        try {
            Account account = securityUtil.getCurrentAccount();
            if (account.getRole() != CUSTOMER) {
                // 只有普通用户可以申请创建店铺
                throw TomatoMallException.forbidden();
            }
            shopVO.setIsValid(0);
            Shop shop = shopVO.toPO();
            shopRepository.save(shop);
            return "创建成功";
        } catch (Exception e) {
            return "创建失败";
        }
    }

    /**
     * 根据店铺ID获取店铺详情
     * @param shopId 店铺ID
     * @return 店铺VO
     */
    @Override
    public ShopVO getShopById(Integer shopId) {
        Optional<Shop> opShop = shopRepository.findById(shopId);
        if (!opShop.isPresent()) {
            throw TomatoMallException.shopNotExists();
        }
        return opShop.get().toVO();
    }

    /**
     * 更新店铺信息
     * @param shopVO 店铺VO
     * @return 更新结果
     */
    @Override
    public String updateShop(ShopVO shopVO) {
        try {
            Optional<Shop> opShop = shopRepository.findById(shopVO.getShopId());
            if (!opShop.isPresent()) {
                throw TomatoMallException.shopNotExists();
            }
            Shop shop = opShop.get();
            Account account = securityUtil.getCurrentAccount();
            if ((account.getRole() != SHOPKEEPER && account.getRole()!=admin)){
                throw TomatoMallException.forbidden();
            }
            // 如果是店主且不是店主本人，也不能更新店铺信息
            if (account.getRole() == SHOPKEEPER && !Objects.equals(account.getId(), shop.getOwnerId())){
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

    /**
     * 删除店铺
     * @param shopId 店铺ID
     * @return 删除结果
     */
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