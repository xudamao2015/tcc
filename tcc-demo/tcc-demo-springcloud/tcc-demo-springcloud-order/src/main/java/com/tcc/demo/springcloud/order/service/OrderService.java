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

package com.tcc.demo.springcloud.order.service;

import com.tcc.demo.springcloud.order.dto.OrderDTO;

public interface OrderService {

	String orderNestedPay(OrderDTO orderDto);

	/**
	 * 创建订单并且进行扣除账户余额支付，并进行库存扣减操作.
	 *
	 * @param orderDto
	 *            订单内容
	 * @return string
	 */
	String orderPay(OrderDTO orderDto);

	/**
	 * 模拟在订单支付操作中，库存在try阶段中的库存异常.
	 *
	 * @param orderDto
	 *            订单内容
	 * @return string
	 */
	String mockInventoryWithTryException(OrderDTO orderDto);

	/**
	 * 模拟在订单支付操作中，库存在try阶段中的timeout.
	 *
	 * @param orderDto
	 *            订单内容
	 * @return string
	 */
	String mockInventoryWithTryTimeout(OrderDTO orderDto);

	/**
	 * @param count
	 * @param amount
	 * @return
	 */
	String orderNestedPayException(OrderDTO orderDto);

}
