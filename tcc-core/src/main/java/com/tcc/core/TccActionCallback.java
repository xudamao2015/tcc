package com.tcc.core;

import com.tcc.core.domain.entity.TccTransaction;

/**
 * description
 * 
 * @author xuyi 2018年10月10日
 */
public interface TccActionCallback {

	void update(TccTransaction tccTx);
}
