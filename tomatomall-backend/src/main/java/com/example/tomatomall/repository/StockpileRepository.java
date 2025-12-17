package com.example.tomatomall.repository;


import com.example.tomatomall.po.Stockpile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockpileRepository extends JpaRepository<Stockpile, Integer> {
    Stockpile findByProductId(int product_id);
    void deleteByProductId(int product_id);
}
