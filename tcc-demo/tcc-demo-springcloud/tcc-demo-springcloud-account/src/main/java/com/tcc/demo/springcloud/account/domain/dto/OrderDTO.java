package com.tcc.demo.springcloud.account.domain.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * description
 * 
 * @author xuyi 2018年10月16日
 */
@Data
public class OrderDTO {

	private String userId;

	/**
	 * 冻结金额，扣款暂存余额
	 */
	private BigDecimal amount;

	private String productId;

	/**
	 * 锁定库存
	 */
	private Integer count;

}
