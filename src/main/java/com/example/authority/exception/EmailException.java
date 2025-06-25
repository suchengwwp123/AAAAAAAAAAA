package com.example.authority.exception;

/**
 * @program: authority
 * @ClassName:EmailException
 * @description: 邮箱验证码异常
 * @author:dyy
 * @Version 3.0
 **/
public class EmailException extends RuntimeException {


    public EmailException(String e) {
        super(e);
    }
}