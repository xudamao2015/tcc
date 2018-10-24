package com.tcc.demo.springcloud.inventory.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.tcc.core.exception.TccRuntimeException;
import com.tcc.demo.springcloud.inventory.domain.dto.InventoryDTO;
import com.tcc.demo.springcloud.inventory.service.InventoryService;

/**
 * description
 * 
 * @author xuyi 2018年10月16日
 */
@RestController
@RequestMapping("inventory")
public class InventoryController {

	@Autowired
	private InventoryService inventoryService;

	/**
	 * 库存扣减
	 *
	 * @param inventoryDTO
	 *            实体对象
	 * @return true 成功
	 */
	@ResponseBody
	@RequestMapping(path = "decrease", method = { RequestMethod.POST })
	Boolean decrease(@RequestBody InventoryDTO inventoryDTO) {
		return inventoryService.decrease(inventoryDTO);

	}

	/**
	 * 获取商品库存
	 *
	 * @param productId
	 *            商品id
	 * @return InventoryDO
	 */
	@ResponseBody
	@RequestMapping(path = "findByProductId", method = { RequestMethod.POST, RequestMethod.GET })
	Integer findByProductId(@RequestParam("productId") String productId) {
		return inventoryService.getStock(productId);
	}

	 /**
	 * 模拟库存扣减异常
	 *
	 * @param inventoryDTO
	 * 实体对象
	 * @return true 成功
	 */
	 @RequestMapping(path = "mockWithTryException", method = { RequestMethod.POST, RequestMethod.GET })
	 Boolean mockWithTryException(@RequestBody InventoryDTO inventoryDTO) {
		 return inventoryService.decreaseWithException(inventoryDTO);
	 }
	
	 /**
	 * 模拟库存扣减超时
	 *
	 * @param inventoryDTO
	 * 实体对象
	 * @return true 成功
	 */
	 @RequestMapping(path = "mockWithTryTimeout", method = { RequestMethod.POST, RequestMethod.GET })
	 Boolean mockWithTryTimeout(@RequestBody InventoryDTO inventoryDTO) {
		 try {
			Thread.sleep(10_000);
		 } catch(InterruptedException e) {
			 e.printStackTrace();
		 }
		 return inventoryService.decrease(inventoryDTO);
	 }
}
