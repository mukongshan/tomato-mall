package com.example.tomatomall.vo;

import com.example.tomatomall.enums.RoleEnum;
import com.example.tomatomall.po.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class AccountVO {
    private Integer id;
    private String username;  // 对应 Account 类中的 username
    private String name;
    private String telephone;  // 对应 Account 类中的 telephone
    private String password;
    private String avatar;  // 对应 Account 类中的 avatar
    private RoleEnum role;  // 对应 Account 类中的 role
    private String email;  // 对应 Account 类中的 email
    private String location;  // 对应 Account 类中的 location

    // 将 AccountVO 转换为 Account PO 对象
    public Account toPO() {
        Account account = new Account();
        account.setId(this.id);
        account.setUsername(this.username);
        account.setName(this.name);
        account.setTelephone(this.telephone);
        account.setPassword(this.password);
        account.setAvatar(this.avatar);
        account.setRole(this.role);
        account.setEmail(this.email);
        account.setLocation(this.location);
        return account;
    }
}
