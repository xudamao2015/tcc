package com.tcc.demo.springcloud.account.domain.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * description
 * 
 * @author xuyi 2018年10月15日
 */
@Data
public class AccountDTO {
	private String userId;

	/**
	 * 冻结金额，扣款暂存余额
	 */
	private BigDecimal amount;
}
