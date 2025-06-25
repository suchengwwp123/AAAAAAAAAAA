package com.example.authority.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @program: authority-2026.0.2
 * @ClassName: IgnoreParam
 * @description: 忽略参数注解，搭配LogAspect使用
 * @author:dyy
 * @Version 3.0
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreParam {
}
