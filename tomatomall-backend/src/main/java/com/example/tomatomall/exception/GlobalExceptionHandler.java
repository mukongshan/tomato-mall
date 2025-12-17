package com.example.tomatomall.exception;

import com.example.tomatomall.vo.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 这个类能够接住项目中所有抛出的异常，
 * 使用了 RestControllerAdvice 进行全局异常处理。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = TomatoMallException.class)
    public ResponseEntity<Response<String>> handleTomatoMallException(TomatoMallException e) {
        e.printStackTrace();

        // 根据异常类型或信息，返回不同的 HTTP 状态码
        HttpStatus status;
        switch (e.getCode()) {
            case "401":
                status = HttpStatus.UNAUTHORIZED; // 401
                break;
            case "400":
                status = HttpStatus.BAD_REQUEST; // 400
                break;
            case "403":
                status = HttpStatus.FORBIDDEN; // 403
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        }

        return ResponseEntity
                .status(status)
                .body(Response.buildFailure(e.getMessage(), String.valueOf(status.value())));
    }
}
