package com.example.tomatomall.repository;

import com.example.tomatomall.po.Cart;
import com.example.tomatomall.po.CartOrderRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface CartOrderRelationRepository extends JpaRepository<CartOrderRelation, Integer> {
    List<CartOrderRelation> findByOrderId(Integer orderId);
}
