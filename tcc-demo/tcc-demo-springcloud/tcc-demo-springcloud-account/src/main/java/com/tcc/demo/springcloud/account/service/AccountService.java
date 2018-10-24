package com.tcc.demo.springcloud.account.service;

import java.math.BigDecimal;

import com.tcc.demo.springcloud.account.domain.dto.AccountDTO;
import com.tcc.demo.springcloud.account.domain.dto.OrderDTO;

/**
 * description
 * 
 * @author xuyi 2018年10月15日
 */

public interface AccountService {
	BigDecimal getBalance(String userId);
	
	boolean tryPay(AccountDTO accoutDto) throws Exception;
	
	boolean payAndReduceInventory(AccountDTO accountDto) throws Exception;

	/**
	 * @param accoutDto
	 * @return
	 * @throws Exception
	 */
	boolean paymentNested(OrderDTO orderDto) throws Exception;

	/**
	 * @param orderDto
	 * @return
	 * @throws Exception
	 */
	boolean paymentNestedException(OrderDTO orderDto) throws Exception;
}
