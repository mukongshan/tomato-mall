package com.example.tomatomall.po;

import com.example.tomatomall.vo.SpecificationVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "specifications")
public class Specification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "specification_id")
    private Integer specificationId;

    @Basic
    @Column(name = "item", nullable = false)
    private String item;

    @Basic
    @Column(name = "value", nullable = false)
    private String value;

    @Basic
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    public SpecificationVO toVO() {
        SpecificationVO vo = new SpecificationVO();
        vo.setItem(this.item);
        vo.setValue(this.value);
        vo.setProductId(this.productId);
        vo.setId(this.specificationId);
        return vo;
    }
}