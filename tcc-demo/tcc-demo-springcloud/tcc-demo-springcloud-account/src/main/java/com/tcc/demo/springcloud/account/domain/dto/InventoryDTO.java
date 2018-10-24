package com.tcc.demo.springcloud.account.domain.dto;

import lombok.Data;

/**
 * description
 * 
 * @author xuyi 2018年10月16日
 */
@Data
public class InventoryDTO {
	private String productId;

	/**
	 * 锁定库存
	 */
	private Integer count;
}
