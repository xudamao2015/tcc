package com.tcc.core.service;

import com.tcc.core.common.config.TccConfig;
import com.tcc.core.domain.entity.TccTransaction;

/**
 * description
 * 
 * @author xuyi 2018年10月9日
 */
public interface ResourceCoordinatorService {
	
	public int start(TccConfig tccConfig);

	public int create(TccTransaction tccTransaction);

	/**
	 * find by transId.
	 *
	 * @param txId
	 *            事务ID
	 * @param appName
	 *            服务名称
	 * @return {@linkplain TccTransaction }
	 */
	public TccTransaction findByTransId(String txId, String appName);

	/**
	 * remove transaction.
	 *
	 * @param txId
	 *            transactionId
	 * @param appName
	 *            服务名称
	 * @return true success
	 */
	public int remove(String txId, String appName);

	/**
	 * update.
	 * 
	 * @param tccTransaction
	 *            {@linkplain TccTransaction }
	 */
	public int update(TccTransaction tccTransaction);

	/**
	 * update TccTransaction . this is only update Participant field.
	 * 
	 * @param tccTransaction
	 *            {@linkplain TccTransaction }
	 * @return rows
	 */
	public int updateParticipant(TccTransaction tccTransaction);

	/**
	 * update TccTransaction status.
	 * 
	 * @param id
	 *            pk.
	 * @param status
	 *            {@linkplain com.tcc.core.common.type.TccStatus}
	 * @return rows
	 */
	public int updateStatus(String txId, String appName, Integer status);
}
