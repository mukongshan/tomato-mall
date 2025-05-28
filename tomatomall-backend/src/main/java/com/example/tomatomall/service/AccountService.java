package com.example.tomatomall.service;

import com.example.tomatomall.vo.AccountVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AccountService {

    AccountVO getAccountInfo(String username);
    String createAccount(AccountVO accountVO);
    String updateAccount(AccountVO accountVO);
    String updateAccountWithoutPassword(AccountVO accountVO);
    String login(AccountVO accountVO);
    String uploadImg(MultipartFile file);
    String updateRole(Integer id, String role);
    String getRole(Integer id);
    List<AccountVO> getShopStaff(Integer shopId);
}
