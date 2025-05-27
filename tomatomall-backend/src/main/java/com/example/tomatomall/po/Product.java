package com.example.tomatomall.po;

import com.example.tomatomall.vo.ProductVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name = "title", nullable = false)
    private String title;

    @Basic
    @Column(name = "price", nullable = false)
    private Double price;

    @Basic
    @Column(name = "rate", nullable = false)
    private Double rate;

    @Basic
    @Column(name = "description")
    private String description;

    @Basic
    @Column(name = "cover")
    private String cover;

    @Basic
    @Column(name = "detail")
    private String detail;

    @Basic
    @Column(name = "shop_id", nullable = false)
    private Integer shopId;

    public ProductVO toVO() {
        ProductVO vo = new ProductVO();
        vo.setId(this.id);
        vo.setTitle(this.title);
        vo.setPrice(this.price);
        vo.setRate(this.rate);
        vo.setDescription(this.description);
        vo.setCover(this.cover);
        vo.setShopId(this.shopId);
        vo.setDetail(this.detail);
        return vo;
    }

}