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

        String loginToken = tokenUtil.getToken(account);
        System.out.println("loginToken: " +loginToken);
        return loginToken;
    }

    @Override
    public AccountVO getAccountInfo() {
        Account account=securityUtil.getCurrentAccount();
        return account.toVO();
    }

    @Override
    public String updateAccount(AccountVO accountVO) {
        Account account = securityUtil.getCurrentAccount();
        System.out.println("raw password: "+account.getPassword());
        boolean needNewToken = false;

        if (accountVO.getPassword() != null) {
            account.setPassword(passwordEncoder.encode(accountVO.getPassword()));  // 记得加密密码
            needNewToken = true; // 如果修改了密码，需要重新生成 Token
        }
        if (accountVO.getName() != null) {
            account.setName(accountVO.getName());
        }
        if (accountVO.getLocation() != null) {
            account.setLocation(accountVO.getLocation());
        }
        if (accountVO.getAvatar() != null) {
            account.setAvatar(accountVO.getAvatar());
        }

        accountRepository.save(account);

        // 更新 session 中的 currentAccount
        HttpSession session = request.getSession();
        session.setAttribute("currentAccount", securityUtil.getCurrentAccount());


        System.out.println("updated password: "+securityUtil.getCurrentAccount().getPassword());

        // 如果密码变了，就重新生成 Token
        if (needNewToken) {
            return tokenUtil.getToken(account);
        }

        return "更新成功";
    }


    @Override
    public String uploadImg(MultipartFile file){
        try {
            String url = ossUtil.upload(file.getOriginalFilename(),file.getInputStream());
            Account account=securityUtil.getCurrentAccount();
            if (url != null){
                account.setAvatar(url);
            }
            accountRepository.save(account);
            return url;
        }catch (Exception e){
            e.printStackTrace();
            throw TomatoMallException.fileUploadFail();
        }
    }

}