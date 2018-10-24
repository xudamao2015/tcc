package com.tcc.demo.springcloud.inventory.service;

import com.tcc.demo.springcloud.inventory.domain.dto.InventoryDTO;

/**
 * description
 * 
 * @author xuyi 2018年10月16日
 */
public interface InventoryService {

	public boolean decrease(InventoryDTO inventoryDto);

	public int getStock(String productId);

	/**
	 * @param inventoryDto
	 * @return
	 */
	boolean decreaseWithException(InventoryDTO inventoryDto);
}
