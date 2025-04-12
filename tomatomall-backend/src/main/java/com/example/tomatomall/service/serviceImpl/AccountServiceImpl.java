package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.Account;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.util.OssUtil;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.util.TokenUtil;
import com.example.tomatomall.vo.AccountVO;
import com.example.tomatomall.vo.Response;
import org.apache.coyote.http11.filters.VoidOutputFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    OssUtil ossUtil = new OssUtil();

    @Autowired
    TokenUtil tokenUtil;

    @Autowired
    SecurityUtil securityUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpServletRequest request;  // 注入 HttpServletRequest

    @Override
    public AccountVO getAccountByUsername(String username) {
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw TomatoMallException.usernameNotExists();
        }
        return account.toVO(); // 确保 Account 类有 toVO() 方法
    }


    @Override
    public String createAccount(AccountVO accountVO) {
        Account account = accountRepository.findByUsername(accountVO.getUsername());
        if (account != null) {
            throw TomatoMallException.usernameAlreadyExists();
        }
        // 获取用户输入的原始密码
        String rawPassword = accountVO.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        accountVO.setPassword(encodedPassword);

        Account newAccount = accountVO.toPO();
        accountRepository.save(newAccount);
        return "注册成功";
    }

    @Override
    public String login(AccountVO accountVO) {
        Account account = accountRepository.findByUsername(accountVO.getUsername());
        if (account == null) {
            throw TomatoMallException.usernameOrPasswordError();
        }
        // 使用 passwordEncoder 比较原始密码和数据库中加密后的密码
        boolean isPswMatch = passwordEncoder.matches(accountVO.getPassword(), account.getPassword());
        if (!isPswMatch) {
            throw TomatoMallException.usernameOrPasswordError();
        }

        request.getSession().setAttribute("currentAccount", account);
        //登录成功返回Token
        return tokenUtil.getToken(account);
    }

    @Override
    public AccountVO getAccountInfo(String username) {
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw TomatoMallException.usernameNotExists();
        }
        return account.toVO();
    }

    @Override
    public String updateAccount(AccountVO accountVO) {
        Account account = securityUtil.getCurrentAccount();
        if (account == null) {
            throw TomatoMallException.usernameNotExists();
        }

        if (!accountVO.getPassword().isEmpty()) {
            account.setPassword(passwordEncoder.encode(accountVO.getPassword()));
        }
        if (!accountVO.getName().isEmpty()) {
            account.setName(accountVO.getName());
        }
        if (!accountVO.getAvatar().isEmpty()) {
            account.setAvatar(accountVO.getAvatar());
        }
        if (accountVO.getRole() != null) {
            account.setRole(accountVO.getRole());
        }
        if (!accountVO.getTelephone().isEmpty()) {
            account.setTelephone(accountVO.getTelephone());
        }
        if (!accountVO.getEmail().isEmpty()) {
            account.setEmail(accountVO.getEmail());
        }
        if (!accountVO.getLocation().isEmpty()) {
            account.setLocation(accountVO.getLocation());
        }

        accountRepository.save(account);

        // 更新 session 中的 currentAccount
        HttpSession session = request.getSession();
        session.setAttribute("currentAccount", account);

        return "更新成功";
    }


    @Override
    public String uploadImg(MultipartFile file){
        try {
            String url = ossUtil.upload(file.getOriginalFilename(),file.getInputStream());
            return url;
        }catch (Exception e){
            e.printStackTrace();
            throw TomatoMallException.fileUploadFail();
        }
    }
}