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

package com.tcc.springcloud.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.tcc.core.common.TccTransactionContext;
import com.tcc.core.common.TccTransactionContextLocal;
import com.tcc.core.common.constant.CommonConstant;
import com.tcc.core.common.type.TccRole;
import com.tcc.core.utils.JsonUtils;

import feign.Feign;
import feign.Request;
import feign.Request.Options;
import feign.Retryer;

/**
 * TccRestTemplateConfiguration.
 * 
 * @author xuyi
 */
@Configuration
public class TccFeignConfiguration {

	/**
	 * build feign.
	 *
	 * @return Feign.Builder
	 */
	@Bean
	@Scope("prototype")
	public Feign.Builder feignBuilder() {
		return Feign.builder().invocationHandlerFactory((target, dispatch) -> {
			// tcc事务切面，把rpc调用点封装到当前线程绑定的tcc事务中
			TccFeignHandler handler = new TccFeignHandler();
			handler.setHandlers(dispatch);
			return handler;
		}).requestInterceptor(request -> {
			// rpc调用时，在http header上面添加tcc事务上下文
			TccTransactionContext currentTx = TccTransactionContextLocal.get();
			if (currentTx != null) {
				TccTransactionContext paramTxContext = new TccTransactionContext();
				paramTxContext.setTransId(currentTx.getTransId());
				paramTxContext.setStatus(currentTx.getStatus());
				paramTxContext.setRole(TccRole.PARTICIPATE.getCode());
				String tccJson = JsonUtils.toJson(paramTxContext);
				request.header(CommonConstant.TCC_TRANSACTION_CONTEXT, tccJson);
			}
		}).retryer(Retryer.NEVER_RETRY);

	}

	@Bean
	public Options option() {
		// read timeout调整到10s
		return new Request.Options(5000, 1000_000);
	}

}
