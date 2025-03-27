package com.example.tomatomall.service;

import com.example.tomatomall.vo.AccountVO;

public interface AccountService {
    AccountVO getAccountByUsername(String username);
    AccountVO getAccountInfo();
    Boolean createAccount(AccountVO accountVO);
    Boolean updateAccount(AccountVO accountVO);
    String login(String username,String password);

}
