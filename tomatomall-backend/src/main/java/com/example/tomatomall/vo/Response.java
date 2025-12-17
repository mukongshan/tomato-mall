package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class Response<T> {
    private String code;
    private String msg;
    private T data;

    public static <T> Response<T> buildSuccess(T data) {
        Response<T> response = new Response<>();
        response.setCode("200");
        response.setMsg(null);
        response.setData(data);
        return response;
    }

    public static <T> Response<T> buildError(String code, String msg) {
        Response<T> response = new Response<>();
        response.setCode(code);
        response.setMsg(msg);
        response.setData(null);
        return response;
    }

    public static <T> Response<T> buildFailure(String msg, String code) {
        return buildError(code, msg);
    }
}
