package com.example.authority.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @program: authority-2026.0.1
 * @ClassName: IgoreResult
 * @description: 忽略返回体，也是搭配LogAspect使用的
 * @author:dyy
 * @Version 3.0
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgoreResult {
}
