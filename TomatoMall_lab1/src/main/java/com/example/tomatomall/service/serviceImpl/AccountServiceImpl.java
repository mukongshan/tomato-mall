package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.Account;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.util.TokenUtil;
import com.example.tomatomall.vo.AccountVO;
import com.example.tomatomall.vo.Response;
import org.apache.coyote.http11.filters.VoidOutputFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TokenUtil tokenUtil;

    @Autowired
    SecurityUtil securityUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AccountVO getAccountByUsername(String username) {
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw TomatoMallException.usernameNotExists();
        }
        return account.toVO(); // 确保 Account 类有 toVO() 方法
    }


    @Override
    public Boolean createAccount(AccountVO accountVO) {
        Account account = accountRepository.findByUsername(accountVO.getUsername());
        if (account != null) {
            throw TomatoMallException.usernameAlreadyExists();
        }
        // 获取用户输入的原始密码
        String rawPassword = accountVO.getPassword();
        // 使用 BCryptPasswordEncoder 对密码进行加密
        String encodedPassword = passwordEncoder.encode(rawPassword);
        // 将加密后的密码设置到用户对象
        accountVO.setPassword(encodedPassword);

        Account newAccount = accountVO.toPO();
        accountRepository.save(newAccount);
        return true;
    }

    @Override
    public String login(String usesrname, String password) {
        Account account = accountRepository.findByUsernameAndPassword(usesrname, password);
        if (account == null) {
            throw TomatoMallException.usernameOrPasswordError();
        }

        // 使用 passwordEncoder 比较原始密码和数据库中加密后的密码
        boolean isPswMatch = passwordEncoder.matches(password, account.getPassword());
        if (!isPswMatch) {
            throw TomatoMallException.usernameOrPasswordError();
        }

        return tokenUtil.getToken(account);
    }

    @Override
    public AccountVO getAccountInfo() {
        Account account=securityUtil.getCurrentAccount();
        return account.toVO();
    }

    @Override
    public Boolean updateAccount(AccountVO accountVO) {
        Account account=securityUtil.getCurrentAccount();
        if (accountVO.getPassword()!=null){
            account.setPassword(accountVO.getPassword());
        }
        if (accountVO.getName()!=null){
            account.setName(accountVO.getName());
        }
        if (accountVO.getLocation()!=null){
            account.setLocation(accountVO.getLocation());
        }
        if (accountVO.getAvatar() != null){
            account.setAvatar(accountVO.getAvatar());
        }
        accountRepository.save(account);
        return true;
    }

}