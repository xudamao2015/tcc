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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.tcc.core.TccTransactionManager;
import com.tcc.core.common.TccTransactionContext;
import com.tcc.core.common.TccTransactionContextLocal;
import com.tcc.core.common.annotation.TccBizAction;
import com.tcc.core.common.type.TccStatus;
import com.tcc.core.domain.entity.TccActionInvocation;
import com.tcc.core.domain.entity.TccParticipant;
import com.tcc.core.utils.SpringApplicationHolder;

import feign.InvocationHandlerFactory.MethodHandler;

/**
 * TccFeignHandler.
 * 
 * rpc调用时，把tcc参与者的调用掉封装到当前线程中
 *
 * @author xuyi
 */
public class TccFeignHandler implements InvocationHandler {

	private Map<Method, MethodHandler> handlers;

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final TccBizAction tcc = method.getAnnotation(TccBizAction.class);
		// 无事务切面时，直接进行rpc调用
		if (Objects.isNull(tcc)) {
			return this.handlers.get(method).invoke(args);
		}
		try {
			TccTransactionContext tccTransactionContext = TccTransactionContextLocal.get();
			if (tccTransactionContext != null) {
				TccTransactionManager tccTransactionManager = SpringApplicationHolder.getInstance().getAppCtx()
						.getBean(TccTransactionManager.class);
				TccParticipant participant = buildParticipant(tcc, method, args, tccTransactionContext);
				tccTransactionManager.addTccParticipant(participant);
			}
			Object invoke = this.handlers.get(method).invoke(args);

			return invoke;
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			throw throwable;
		}
	}

	private TccParticipant buildParticipant(TccBizAction tcc, Method method, Object[] args,
			final TccTransactionContext tccTransactionContext) {
		if (TccStatus.TRYING.getCode() != tccTransactionContext.getStatus()) {
			return null;
		}
		// 获取rpc业务补偿调用点
		String confirmMethodName = StringUtils.isBlank(tcc.confirmAction()) ? method.getName() : tcc.confirmAction();
		String cancelMethodName = StringUtils.isBlank(tcc.cancelAction()) ? method.getName() : tcc.cancelAction();
		final Class<?> declaringClass = method.getDeclaringClass();
		TccActionInvocation confirmInvocation = new TccActionInvocation(declaringClass, confirmMethodName,
				method.getParameterTypes(), args);
		TccActionInvocation cancelInvocation = new TccActionInvocation(declaringClass, cancelMethodName,
				method.getParameterTypes(), args);
		// 封装调用点
		return new TccParticipant(tccTransactionContext.getTransId(), confirmInvocation, cancelInvocation);
	}

	/**
	 * set handlers.
	 *
	 * @param handlers
	 *            handlers
	 */
	public void setHandlers(final Map<Method, MethodHandler> handlers) {
		this.handlers = handlers;
	}

}
