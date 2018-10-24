package com.tcc.demo.springcloud.order.dto;

import java.math.BigDecimal;

import javax.ws.rs.DefaultValue;

import lombok.Data;

/**
 * description
 * 
 * @author xuyi 2018年10月16日
 */
@Data
public class OrderDTO {

	@DefaultValue(value="10000")
	private String userId;

	/**
	 * 冻结金额，扣款暂存余额
	 */
	private BigDecimal amount;
	
	@DefaultValue(value="1")
	private String productId;

	/**
	 * 锁定库存
	 */
	private Integer count;

}
