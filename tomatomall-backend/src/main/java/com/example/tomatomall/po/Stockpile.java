package com.example.tomatomall.po;

import com.example.tomatomall.vo.StockpileVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stockpiles")
public class Stockpile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Basic
    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Basic
    @Column(name = "frozen", nullable = false)
    private Integer frozen;

    public StockpileVO toVO() {
        StockpileVO vo = new StockpileVO();
        vo.setAmount(this.amount);
        vo.setFrozen(this.frozen);
        vo.setProductId(this.productId);
        vo.setId(this.id);
        return vo;
    }
}