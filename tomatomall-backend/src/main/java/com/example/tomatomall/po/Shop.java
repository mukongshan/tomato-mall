package com.example.tomatomall.po;

import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.ShopVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shops")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Basic
    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @Basic
    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Basic
    @Column(name = "description", length = 255)
    private String description;

    @Basic
    @Column(name = "rate", precision = 3, scale = 2)
    private Double rate;

    @Basic
    @Column(name = "is_valid", nullable = false)
    private Integer isValid = 0;

    public ShopVO toVO() {
        ShopVO vo = new ShopVO();
        vo.setShopId(this.id);
        vo.setName(this.name);
        vo.setOwnerId(this.ownerId);
        vo.setIconUrl(this.iconUrl);
        vo.setDescription(this.description);
        vo.setRate(this.rate);
        vo.setIsValid(this.isValid);
        return vo;
    }
} 