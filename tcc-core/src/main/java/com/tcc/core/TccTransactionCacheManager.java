/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.tcc.core;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;

import com.tcc.core.domain.entity.TccTransaction;
import com.tcc.core.service.AppNameService;
import com.tcc.core.service.ResourceCoordinatorService;
import com.tcc.core.utils.SpringApplicationHolder;

/**
 * use cache.
 * 
 */
public final class TccTransactionCacheManager {

	// private static final int MAX_COUNT = 1000;

	private static final ConcurrentHashMap<String, TccTransaction> LOADING_CACHE = new ConcurrentHashMap<>(10);
	// CacheBuilder.newBuilder()
	// .maximumWeight(MAX_COUNT).weigher((Weigher<String, TccTransaction>) (string,
	// tccTransaction) -> getSize())
	// .build(new CacheLoader<String, TccTransaction>() {
	// @Override
	// public TccTransaction load(final String key) {
	// return cacheTccTransaction(key);
	// }
	// });

	private static ResourceCoordinatorService coordinatorService = SpringApplicationHolder.getInstance()
			.getBean(ResourceCoordinatorService.class);

	private static AppNameService appNameService = SpringApplicationHolder.getInstance().getBean(AppNameService.class);

	private static final TccTransactionCacheManager TCC_TRANSACTION_CACHE_MANAGER = new TccTransactionCacheManager();

	private TccTransactionCacheManager() {

	}

	/**
	 * TccTransactionCacheManager.
	 *
	 * @return TccTransactionCacheManager
	 */
	public static TccTransactionCacheManager getInstance() {
		return TCC_TRANSACTION_CACHE_MANAGER;
	}

	/**
	 * cache tccTransaction.
	 *
	 * @param tccTransaction
	 *            {@linkplain TccTransaction}
	 */
	public void cacheTccTransaction(TccTransaction tccTransaction) {
		LOADING_CACHE.put(tccTransaction.getTransactionId(), tccTransaction);
	}

	/**
	 * acquire TccTransaction.
	 *
	 * @param key
	 *            this guava key.
	 * @return {@linkplain TccTransaction}
	 */
	public TccTransaction getTccTransaction(String txId) {
		TccTransaction tccTx = null;
		try {
			tccTx = LOADING_CACHE.get(txId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (tccTx == null) {
				tccTx = coordinatorService.findByTransId(txId, appNameService.getAppName());
				LOADING_CACHE.put(txId, tccTx);
			}
		}
		return tccTx;
	}

	/**
	 * remove guava cache by key.
	 * 
	 * @param key
	 *            guava cache key.
	 */
	public void removeByTransId(String txId) {
		if (!StringUtils.isEmpty(txId)) {
			// LOADING_CACHE.invalidate(txId);
			LOADING_CACHE.remove(txId);
		}
	}

}
