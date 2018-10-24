package com.tcc.demo.springcloud.inventory.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tcc.core.common.annotation.TccBizAction;
import com.tcc.core.exception.TccRuntimeException;
import com.tcc.demo.springcloud.inventory.domain.dto.InventoryDTO;
import com.tcc.demo.springcloud.inventory.domain.entity.Inventory;
import com.tcc.demo.springcloud.inventory.domain.mapper.InventoryMapper;
import com.tcc.demo.springcloud.inventory.service.InventoryService;

/**
 * description
 * 
 * @author xuyi 2018年10月16日
 */
@Service
public class InventoryServiceImpl implements InventoryService {
	
	private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

	@Autowired
	private InventoryMapper inventoryMapper;

	@Override
	@Transactional
	@TccBizAction(confirmAction = "confirmAction", cancelAction = "cancelAction")
	public boolean decrease(InventoryDTO inventoryDto) {
		Inventory inventory = new Inventory();
		Inventory current = inventoryMapper.selectByPid(inventoryDto.getProductId());
		if(current.getTotalInventory() - inventoryDto.getCount() <=0 ) {
			logger.warn("库存不足！锁库失败");
			return false;
		}
		inventory.setProductId(inventoryDto.getProductId());
		inventory.setLockInventory(current.getLockInventory() + inventoryDto.getCount());
		inventory.setTotalInventory(current.getTotalInventory() - inventoryDto.getCount());
		int result = inventoryMapper.decrease(inventory);
		return result == 1 ? true : false;
	}

	@Override
	@Transactional
	@TccBizAction(confirmAction = "confirmAction", cancelAction = "cancelAction")
	public boolean decreaseWithException(InventoryDTO inventoryDto) {
		Inventory inventory = new Inventory();
		Inventory current = inventoryMapper.selectByPid(inventoryDto.getProductId());
		inventory.setProductId(inventoryDto.getProductId());
		inventory.setLockInventory(current.getLockInventory() + inventoryDto.getCount());
		inventory.setTotalInventory(current.getTotalInventory() - inventoryDto.getCount());
		inventoryMapper.decrease(inventory);
		throw new TccRuntimeException("扣减库存异常");
	}

	public boolean confirmAction(InventoryDTO inventoryDto) {
		Inventory inventory = new Inventory();
		Inventory current = inventoryMapper.selectByPid(inventoryDto.getProductId());
		inventory.setTotalInventory(current.getTotalInventory());// 总库存不变
		inventory.setProductId(inventoryDto.getProductId());//
		inventory.setLockInventory(current.getLockInventory() - inventoryDto.getCount());// 扣减锁定库存数量
		boolean result = inventoryMapper.decrease(inventory) == 1 ? true : false;
		return result;
	}

	public boolean cancelAction(InventoryDTO inventoryDto) {
		Inventory inventory = new Inventory();
		Inventory current = inventoryMapper.selectByPid(inventoryDto.getProductId());
		inventory.setProductId(inventoryDto.getProductId());
		inventory.setTotalInventory(current.getTotalInventory() + inventoryDto.getCount());// 归还锁定库存到总库存
		inventory.setLockInventory(current.getLockInventory() - inventoryDto.getCount());// 扣减锁定库存数量
		boolean result = inventoryMapper.decrease(inventory) == 1 ? true : false;
		return result;
	}

	@Override
	public int getStock(String productId) {
		return inventoryMapper.getStock(productId);
	}

}
