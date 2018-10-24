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

import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tcc.demo.springcloud.order.domain.entity.Order;
import com.tcc.demo.springcloud.order.domain.mapper.OrderMapper;
import com.tcc.demo.springcloud.order.dto.OrderDTO;
import com.tcc.demo.springcloud.order.enums.OrderStatusEnum;
import com.tcc.demo.springcloud.order.service.OrderService;
import com.tcc.demo.springcloud.order.service.PaymentService;

@Service("orderService")
public class OrderServiceImpl implements OrderService {

	/**
	 * logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

	@Autowired
	private OrderMapper orderMapper;

	@Autowired
	private PaymentService paymentService;

	@Override
	public String orderPay(OrderDTO orderDto) {
		final Order order = buildOrder(orderDto);
		final int rows = orderMapper.create(order);

		if (rows > 0) {
			paymentService.makePayment(order);
		}
		return "success";
	}

	/**
	 * 模拟在订单支付操作中，库存在try阶段中的库存异常
	 *
	 * @param count
	 *            购买数量
	 * @param amount
	 *            支付金额
	 * @return string
	 */
	@Override
	public String mockInventoryWithTryException(OrderDTO orderDto) {
		final Order order = buildOrder(orderDto);
		final int rows = orderMapper.create(order);

		if (rows > 0) {
			paymentService.mockPaymentInventoryWithTryException(order);
		}

		return "success";
	}

	/**
	 * 模拟在订单支付操作中，库存在try阶段中的timeout
	 *
	 * @param count
	 *            购买数量
	 * @param amount
	 *            支付金额
	 * @return string
	 */
	@Override
	public String mockInventoryWithTryTimeout(OrderDTO orderDto) {
		final Order order = buildOrder(orderDto);
		final int rows = orderMapper.create(order);

		if (rows > 0) {
			paymentService.mockPaymentInventoryWithTryTimeout(order);
		}

		return "success";
	}

	private Order buildOrder(OrderDTO orderDto) {
		LOGGER.debug("构建订单对象");
		Order order = new Order();
		order.setCreateTime(new Date());
		order.setNumber(UUID.randomUUID().toString().replace("-", ""));
		// demo中的表里只有商品id为 1的数据
		order.setProductId(orderDto.getProductId());
		order.setStatus(OrderStatusEnum.NOT_PAY.getCode());
		order.setTotalAmount(Long.parseLong(orderDto.getAmount().toString()));
		order.setCount(orderDto.getCount());
		// demo中 表里面存的用户id为10000
		order.setUserId(orderDto.getUserId());
		return order;
	}

	@Override
	public String orderNestedPay(OrderDTO orderDto) {
		Order order = buildOrder(orderDto);
		String result = "success";
		final int rows = orderMapper.create(order);
		try {
			if (rows > 0) {
				paymentService.makePaymentNested(order);
			}
		} catch (Exception e) {
			result = "fail";
		}
		return result;
	}

	@Override
	public String orderNestedPayException(OrderDTO orderDto) {
		Order order = buildOrder(orderDto);
		final int rows = orderMapper.create(order);
		String result = "success";
		try {
			if (rows > 0) {
				paymentService.makePaymentNestedException(order);
			}
		} catch (Exception e) {
			result = "fail";
		}
		return result;
	}
}
