/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tcc.demo.springcloud.order.service.impl;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tcc.core.common.annotation.TccBizAction;
import com.tcc.core.exception.TccRuntimeException;
import com.tcc.demo.springcloud.order.client.AccountClient;
import com.tcc.demo.springcloud.order.client.InventoryClient;
import com.tcc.demo.springcloud.order.domain.entity.Order;
import com.tcc.demo.springcloud.order.domain.mapper.OrderMapper;
import com.tcc.demo.springcloud.order.dto.AccountDTO;
import com.tcc.demo.springcloud.order.dto.InventoryDTO;
import com.tcc.demo.springcloud.order.dto.OrderDTO;
import com.tcc.demo.springcloud.order.enums.OrderStatusEnum;
import com.tcc.demo.springcloud.order.service.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

	/**
	 * logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

	@Autowired
	private OrderMapper orderMapper;

	@Autowired
	private AccountClient accountClient;

	@Autowired
	private InventoryClient inventoryClient;

	@Override
	@TccBizAction(confirmAction = "confirmAction", cancelAction = "cancelAction")
	public void makePayment(Order order) {
		order.setStatus(OrderStatusEnum.PAYING.getCode());
		orderMapper.updateStatus(order);
		// 扣除用户余额
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setAmount(order.getTotalAmount());
		accountDTO.setUserId(order.getUserId());
		logger.debug("===========调用accout服务：冻结资金接口==========");
		boolean isSuccess = accountClient.payment(accountDTO);
		if (!isSuccess) {
			throw new TccRuntimeException("调用冻结账号金额接口失败！");
		}
		// 进入扣减库存操作
		InventoryDTO inventoryDTO = new InventoryDTO();
		inventoryDTO.setCount(order.getCount());
		inventoryDTO.setProductId(order.getProductId());
		logger.debug("===========调用inventory服务：锁定库存接口==========");
		isSuccess = inventoryClient.decrease(inventoryDTO);
		if (!isSuccess) {
			throw new TccRuntimeException("调用锁定扣减库存接口失败！");
		}
	}

	/**
	 * 测试嵌套事务
	 */
	@Override
	@TccBizAction(confirmAction = "confirmAction", cancelAction = "cancelAction")
	public void makePaymentNested(Order order) {
		order.setStatus(OrderStatusEnum.PAYING.getCode());
		orderMapper.updateStatus(order);
		// 扣除用户余额(在账户服务内部锁定库存）
		OrderDTO orderdto = new OrderDTO();
		orderdto.setAmount(BigDecimal.valueOf(order.getTotalAmount()));
		orderdto.setCount(order.getCount());
		orderdto.setProductId(order.getProductId());
		orderdto.setUserId(order.getUserId());
		boolean isSuccess = accountClient.payNested(orderdto);
		if (!isSuccess) {
			throw new TccRuntimeException("调用冻结账号金额接口失败！");
		}
	}

	@Override
	@TccBizAction(confirmAction = "confirmAction", cancelAction = "cancelAction")
	public String mockPaymentInventoryWithTryException(Order order) {
		logger.debug("===========执行springcloud  mockPaymentInventoryWithTryException 扣减资金接口==========");
		order.setStatus(OrderStatusEnum.PAYING.getCode());
		orderMapper.updateStatus(order);
		// 扣除用户余额
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setAmount(order.getTotalAmount());
		accountDTO.setUserId(order.getUserId());
		boolean isSuccess = accountClient.payment(accountDTO);
		if (!isSuccess) {
			throw new TccRuntimeException("调用冻结账号金额接口失败！");
		}
		InventoryDTO inventoryDTO = new InventoryDTO();
		inventoryDTO.setCount(order.getCount());
		inventoryDTO.setProductId(order.getProductId());
		isSuccess = inventoryClient.mockWithTryException(inventoryDTO);
		if (!isSuccess) {
			throw new TccRuntimeException("调用锁定扣减库存接口失败！");
		}
		return "success";
	}

	@Override
	@TccBizAction(confirmAction = "confirmAction", cancelAction = "cancelAction")
	public String mockPaymentInventoryWithTryTimeout(Order order) {
		logger.debug("===========执行springcloud  mockPaymentInventoryWithTryTimeout 扣减资金接口==========");
		order.setStatus(OrderStatusEnum.PAYING.getCode());
		orderMapper.updateStatus(order);
		// 扣除用户余额
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setAmount(order.getTotalAmount());
		accountDTO.setUserId(order.getUserId());
		boolean isSuccess = accountClient.payment(accountDTO);
		if (!isSuccess) {
			throw new TccRuntimeException("调用锁定扣减库存接口失败！");
		}
		InventoryDTO inventoryDTO = new InventoryDTO();
		inventoryDTO.setCount(order.getCount());
		inventoryDTO.setProductId(order.getProductId());
		isSuccess = inventoryClient.mockWithTryTimeout(inventoryDTO);
		if (!isSuccess) {
			throw new TccRuntimeException("调用锁定扣减库存接口失败！");
		}
		return "success";
	}

	public void confirmAction(Order order) {
		order.setStatus(OrderStatusEnum.PAY_SUCCESS.getCode());
		orderMapper.updateStatus(order);
		logger.info("=========进行订单confirm操作完成================");
	}

	public void cancelAction(Order order) {
		order.setStatus(OrderStatusEnum.PAY_FAIL.getCode());
		orderMapper.updateStatus(order);
		logger.info("=========进行订单cancel操作完成================");
	}

	@Override
	public void makePaymentNestedException(Order order) {
		order.setStatus(OrderStatusEnum.PAYING.getCode());
		orderMapper.updateStatus(order);
		// 扣除用户余额(在账户服务内部锁定库存）
		OrderDTO orderdto = new OrderDTO();
		orderdto.setAmount(BigDecimal.valueOf(order.getTotalAmount()));
		orderdto.setCount(order.getCount());
		orderdto.setProductId(order.getProductId());
		orderdto.setUserId(order.getUserId());
		boolean isSuccess = accountClient.payNestedException(orderdto);
		if (!isSuccess) {
			throw new TccRuntimeException("调用冻结账号金额接口失败！");
		}
	}

}
