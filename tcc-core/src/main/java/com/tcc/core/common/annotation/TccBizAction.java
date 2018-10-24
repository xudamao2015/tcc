package com.tcc.core.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tcc事务核心注解，用于拦截器构造Tcc事务对象
 * 
 * @author xuyi 2018年9月30日
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TccBizAction {

	public String confirmAction() default "";

	public String cancelAction() default "";
}
