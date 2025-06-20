package com.example.tomatomall.repository;

import com.example.tomatomall.po.AccountCouponsRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountCouponsRelationRepository extends JpaRepository<AccountCouponsRelation, Integer> {
    Optional<AccountCouponsRelation> findById(Integer id);
    List<AccountCouponsRelation> findByAccountId(Integer accountId);
    AccountCouponsRelation findByAccountIdAndCouponId(Integer accountId, Integer couponId);
    void deleteByCouponId(int couponId);
}