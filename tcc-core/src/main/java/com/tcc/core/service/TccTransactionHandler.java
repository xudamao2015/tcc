package com.tcc.core.service;

import org.aspectj.lang.ProceedingJoinPoint;

import com.tcc.core.common.TccTransactionContext;

/**
 * description
 * 
 * @author xuyi 2018年10月9日
 */
@FunctionalInterface
public interface TccTransactionHandler {

	public Object handle(ProceedingJoinPoint point, TccTransactionContext tccTransactionContext) throws Throwable;

}
