package com.example.tomatomall.exception;

/**
 * @Author: DingXiaoyu
 * @Date: 0:26 2023/11/26
 * 你可以在这里自定义Exception
 */
public class TomatoMallException extends RuntimeException {

    private String code;

    // 构造方法，接受错误信息和错误码
    public TomatoMallException(String message, String code) {
        super(message);
        this.code = code;
    }

    // 仅接受错误信息的构造方法，默认错误码为"500"
    public TomatoMallException(String message) {
        super(message);
        this.code = "500";  // 默认的错误码，代表内部服务器错误
    }

    // 获取错误码
    public String getCode() {
        return code;
    }

    // 定义一些常见的异常
    public static TomatoMallException usernameAlreadyExists() {
        return new TomatoMallException("用户名已经存在!", "400");
    }

    public static TomatoMallException notLogin() {
        return new TomatoMallException("鉴权错误!", "401");
    }

    public static TomatoMallException usernameOrPasswordError() {
        return new TomatoMallException("用户名或密码错误!", "400");
    }

    public static TomatoMallException usernameNotExists() {
        return new TomatoMallException("用户名不存在", "404");
    }

    public static TomatoMallException shopNotExists() {
        return new TomatoMallException("商店不存在", "404");
    }

    public static TomatoMallException productNotExists() {
        return new TomatoMallException("商品不存在", "404");
    }

    public static TomatoMallException overStock() {
        return new TomatoMallException("超出库存数量" ,"400");
    }

    public static TomatoMallException cartNotExists() {
        return new TomatoMallException("商品不存在","404");
    }

    public static TomatoMallException orderNotExists() {
        return new TomatoMallException("订单不存在","404");
    }

    public static TomatoMallException AdvertisementNotExists() {
        return new TomatoMallException("广告不存在","404");
    }

    public static TomatoMallException reviewNotExists() {
        return new TomatoMallException("评论不存在");
    }

    // 提供一个静态方法用于抛出文件上传失败的异常
    public static TomatoMallException fileUploadFail() {
        return new TomatoMallException("文件上传失败，请检查文件格式或网络连接！", "500");
    }

    // 你可以根据不同的异常类型，返回不同的错误码
    public static TomatoMallException badRequest(String message) {
        return new TomatoMallException(message, "400");
    }

    public static TomatoMallException forbidden() {
        return new TomatoMallException("没有权限", "403");
    }

}
