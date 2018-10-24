package com.tcc.demo.springcloud.account.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tcc.core.common.annotation.TccBizAction;
import com.tcc.demo.springcloud.account.domain.dto.InventoryDTO;
import com.tcc.springcloud.feign.TccFeignConfiguration;

/**
 * description
 * 
 * @author xuyi 2018年10月16日
 */
@FeignClient(value = "inventory-service", configuration = TccFeignConfiguration.class)
public interface InventoryService {

	/**
	 * 库存扣减
	 *
	 * @param inventoryDTO
	 *            实体对象
	 * @return true 成功
	 */
	@RequestMapping("/inventory-service/inventory/decrease")
	@TccBizAction
	Boolean decrease(@RequestBody InventoryDTO inventoryDTO);

	/**
	 * 获取商品库存
	 *
	 * @param productId
	 *            商品id
	 * @return InventoryDO
	 */
	@RequestMapping("/inventory-service/inventory/findByProductId")
	Integer findByProductId(@RequestParam("productId") String productId);

	/**
	 * 模拟库存扣减异常
	 *
	 * @param inventoryDTO
	 *            实体对象
	 * @return true 成功
	 */
	@TccBizAction
	@RequestMapping("/inventory-service/inventory/mockWithTryException")
	Boolean mockWithTryException(@RequestBody InventoryDTO inventoryDTO);

	/**
	 * 模拟库存扣减超时
	 *
	 * @param inventoryDTO
	 *            实体对象
	 * @return true 成功
	 */
	@TccBizAction
	@RequestMapping("/inventory-service/inventory/mockWithTryTimeout")
	Boolean mockWithTryTimeout(@RequestBody InventoryDTO inventoryDTO);
}
