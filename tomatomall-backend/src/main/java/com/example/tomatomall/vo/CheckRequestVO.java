package com.example.tomatomall.vo;

import lombok.Data;

import java.util.List;

@Data
public class CheckRequestVO {
    public List<Integer> cartIds;
    public String payment_method;
    public Object shipping_address;

}
