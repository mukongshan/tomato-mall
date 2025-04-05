package com.example.tomatomall.vo;

import com.example.tomatomall.po.Stockpile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockpileVO {
    private Integer id;
    private Integer product_id;
    private Integer amount;
    private Integer frozen;

    public Stockpile toPO(){
        Stockpile stockpile = new Stockpile();
        stockpile.setId(id);
        stockpile.setProduct_id(product_id);
        stockpile.setAmount(amount);
        stockpile.setFrozen(frozen);
        return stockpile;
    }

}
