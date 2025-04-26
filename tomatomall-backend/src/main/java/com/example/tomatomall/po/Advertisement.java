package com.example.tomatomall.po;

import com.example.tomatomall.enums.RoleEnum;
import com.example.tomatomall.vo.AdvertisementVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "advertisements")  // 绑定数据库表
public class Advertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "title")
    private String title;

    @Basic
    @Column(name = "content")
    private String content;

    @Basic
    @Column(name = "image_url")
    private String imgUrl;

    @Basic
    @Column(name = "product_id", nullable = false)
    private Integer productId;
    
    public AdvertisementVO toVO() {
        AdvertisementVO advertisementVO = new AdvertisementVO();
        advertisementVO.setId(this.id);
        advertisementVO.setTitle(this.title);
        advertisementVO.setContent(this.content);
        advertisementVO.setImgUrl(this.imgUrl);
        advertisementVO.setProductId(this.productId);
        return advertisementVO;
    }
}
