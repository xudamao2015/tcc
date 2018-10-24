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

package com.tcc.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * this is {@linkplain com.tcc.core.common.annotation.TccBizAction} aspect handler.
 * 
 * @author xuyi
 */
@Aspect
@Component
public class TccTransactionAspect {

    private TccTransactionInterceptor tccTransactionInterceptor;

    @Autowired
    protected void setTccTransactionInterceptor(final TccTransactionInterceptor tccTransactionInterceptor) {
        this.tccTransactionInterceptor = tccTransactionInterceptor;
    }

    /**
     * this is point cut with {@linkplain com.tcc.core.common.annotation.TccBizAction }.
     */
    @Pointcut("@annotation(com.tcc.core.common.annotation.TccBizAction)")
    public void TccInterceptor() {
    }

    /**
     * this is around in {@linkplain com.tcc.core.common.annotation.TccBizAction }.
     * @param proceedingJoinPoint proceedingJoinPoint
     * @return Object
     * @throws Throwable  Throwable
     */
    @Around("TccInterceptor()")
    public Object interceptTccMethod(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return tccTransactionInterceptor.interceptor(proceedingJoinPoint);
    }

}
