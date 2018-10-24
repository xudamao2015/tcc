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

package com.tcc.springcloud.aspect;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.tcc.core.aspect.TccTransactionInterceptor;
import com.tcc.core.common.TccTransactionContext;
import com.tcc.core.common.TccTransactionContextLocal;
import com.tcc.core.common.constant.CommonConstant;
import com.tcc.core.service.TccTransactionHandler;
import com.tcc.core.utils.JsonUtils;

/**
 * SpringCloudTccTransactionInterceptor.
 *
 * @author xuyi
 */
@Component
public class SpringCloudTccTransactionInterceptor implements TccTransactionInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(SpringCloudTccTransactionInterceptor.class);

	@Autowired
	private TccTransactionHandler tccTransactionHandler;

	@Override
	public Object interceptor(final ProceedingJoinPoint pjp) throws Throwable {
		// 获取当前线程绑定的事务上下文
		TccTransactionContext tccTransactionContext = TccTransactionContextLocal.get();

		if (tccTransactionContext == null) {
			// 判断是否成为上游服务的tcc事务参与者
			RequestAttributes requestAttributes = null;
			try {
				requestAttributes = RequestContextHolder.currentRequestAttributes();
			} catch (Throwable ex) {
				logger.warn("can not acquire request info:", ex);
			}
			HttpServletRequest request = requestAttributes == null ? null
					: ((ServletRequestAttributes) requestAttributes).getRequest();
			String context = request == null ? null : request.getHeader(CommonConstant.TCC_TRANSACTION_CONTEXT);
			if (StringUtils.isNoneBlank(context)) {
				tccTransactionContext = JsonUtils.parseJson(context, TccTransactionContext.class);
			}
		}

		return tccTransactionHandler.handle(pjp, tccTransactionContext);
	}

}
