package com.example.tomatomall.repository;

import com.example.tomatomall.po.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Cart findByProductId(Integer productId);
    Boolean existsByProductId(Integer productId);
    List<Cart> findByUserId(Integer userId);
}
