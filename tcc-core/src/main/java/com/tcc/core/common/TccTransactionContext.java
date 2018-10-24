package com.tcc.core.common;

import com.tcc.core.common.type.TccRole;
import com.tcc.core.common.type.TccStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Tcc事务上下文，远程调用时，作为参数传递到下游服务
 * 
 * @author xuyi 2018年10月9日
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TccTransactionContext {

	private String transId;
	
	/**
	 * {@link TccStatus}
	 */
	private int status;
	
	/**
	 * {@link TccRole}
	 */
	private int role;
}
