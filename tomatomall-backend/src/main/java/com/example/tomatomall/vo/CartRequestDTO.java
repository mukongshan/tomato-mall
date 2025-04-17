package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class CartRequestDTO {
    private Integer productId;
    private Integer quantity;
}
