package com.example.tomatomall.exception;

/**
 * @Author: DingXiaoyu
 * @Date: 0:26 2023/11/26
 * 你可以在这里自定义Exception
*/
public class TomatoMallException extends RuntimeException{

    // 构造方法
    public TomatoMallException(String message) {
        super(message);
    }

    public static TomatoMallException usernameAlreadyExists(){
        return new TomatoMallException("用户名已经存在!");
    }

    public static TomatoMallException notLogin(){
        return new TomatoMallException("未登录!");
    }

    public static TomatoMallException usernameOrPasswordError(){
        return new TomatoMallException("用户名或密码错误!");
    }

    public static TomatoMallException usernameNotExists() { return new TomatoMallException("用户名不存在"); }

    // 提供一个静态方法用于抛出文件上传失败的异常
    public static TomatoMallException fileUploadFail() throws TomatoMallException {
        throw new TomatoMallException("文件上传失败，请检查文件格式或网络连接！");
    }


}
