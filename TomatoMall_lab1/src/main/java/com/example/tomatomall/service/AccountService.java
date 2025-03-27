package com.example.tomatomall.service;

import com.example.tomatomall.vo.AccountVO;
import org.springframework.web.bind.annotation.RequestBody;

public interface AccountService {
    AccountVO getAccountByUsername(String username);
    AccountVO getAccountInfo();
    String createAccount(AccountVO accountVO);
    Boolean updateAccount(AccountVO accountVO);
    String login(AccountVO accountVO);

}
