package com.example.tomatomall.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CartListVO {
    private List<CartVO> cartItems;
    private Integer total;
    private double totalAmount;
}
