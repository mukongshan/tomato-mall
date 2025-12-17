package com.example.tomatomall.repository;

import com.example.tomatomall.enums.RoleEnum;
import com.example.tomatomall.po.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Account findByUsername(String username);
    Account findByUsernameAndPassword(String username, String password);

    List<Account> findByShopId(Integer shopId);

    Account findByRole(RoleEnum role);
}
