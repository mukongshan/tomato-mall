package com.example.tomatomall.service;

import com.example.tomatomall.vo.AccountVO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

public interface AccountService {
    AccountVO getAccountByUsername(String username);
    AccountVO getAccountInfo();
    String createAccount(AccountVO accountVO);
    String updateAccount(AccountVO accountVO);
    String login(AccountVO accountVO);
    String uploadImg(MultipartFile file);
}
