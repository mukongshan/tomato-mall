package com.example.tomatomall.repository;

import com.example.tomatomall.po.Cart;
import com.example.tomatomall.po.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
}
