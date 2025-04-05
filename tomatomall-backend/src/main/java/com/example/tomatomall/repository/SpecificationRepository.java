package com.example.tomatomall.repository;

import com.example.tomatomall.po.Specification;
import com.example.tomatomall.vo.SpecificationVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecificationRepository extends JpaRepository<Specification, Integer> {
    List<SpecificationVO> findByProductId(Integer productId);
}
