package com.tcc.demo.springcloud.inventory.domain.mapper;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.tcc.demo.springcloud.inventory.domain.entity.Inventory;

import tk.mybatis.mapper.common.Mapper;

public interface InventoryMapper extends Mapper<Inventory> {

	@Select("select total_inventory from `tcc_inventory` s where s.product_id=#{productId}")
	int getStock(String productId);

	@Update("update `tcc_inventory` s set s.total_inventory= #{totalInventory},s.lock_inventory=#{lockInventory} where s.product_id=#{productId}")
	int decrease(Inventory inventory);

	@Select("select s.total_inventory as totalInventory, s.lock_inventory as lockInventory,s.product_id as productId,s.id as id from `tcc_inventory` s where s.product_id=#{productId}")
	Inventory selectByPid(String productId);
}