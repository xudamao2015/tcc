package com.tcc.core.domain.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * tcc事务confirm，concel行为触发器
 * 
 * @author xuyi 2018年10月9日
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TccActionInvocation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3235675918838453411L;
	
	private Class<?> targetClazz;
	
	private String targetMethod;
	
	private Class<?>[] paramTypes;
	
	private Object[] params;
}
