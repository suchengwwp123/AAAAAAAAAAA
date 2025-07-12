package com.example.authority.annotations;



import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnvLimit {

    /**
     * 允许的环境（默认只允许 dev）
     * 可选值：dev, test, prod, demo, staging 等
     */
    String[] allow() default {"dev"};
}
