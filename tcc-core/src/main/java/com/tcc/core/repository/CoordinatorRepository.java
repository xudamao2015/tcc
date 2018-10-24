package com.tcc.core.repository;

import com.tcc.core.common.config.TccConfig;
import com.tcc.core.domain.entity.TccTransaction;
import com.tcc.core.serializer.ObjectSerializer;

/**
 * 事务协调者仓库
 * 
 * @author xuyi 2018年10月9日
 */
public interface CoordinatorRepository {

	public int create(TccTransaction tccTransaction);
	
	public TccTransaction findById(String txId, String appName);

	public int remove(String txId, String appName);
	
	public int update(TccTransaction tccTransaction);

	public int updateParticipant(TccTransaction tccTransaction);

	public int updateStatus(String txId, String appName, Integer status);
	
	public void setSerialize(ObjectSerializer<?> serializer);

	/**
	 * @param tccConfig
	 */
	void init(TccConfig tccConfig);

	/**
	 * @param tccTransaction
	 * @return
	 */
	int updateFailList(TccTransaction tccTransaction);
}
