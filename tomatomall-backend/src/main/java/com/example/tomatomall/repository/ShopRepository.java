package com.example.tomatomall.repository;

import com.example.tomatomall.po.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {
    // 可以添加自定义查询方法
    Shop findByOwnerId(Integer ownerId);
} 