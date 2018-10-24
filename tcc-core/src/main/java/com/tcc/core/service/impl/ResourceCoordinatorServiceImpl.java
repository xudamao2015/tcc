package com.tcc.core.service.impl;

import org.springframework.stereotype.Service;

import com.tcc.core.common.config.TccConfig;
import com.tcc.core.domain.entity.TccTransaction;
import com.tcc.core.repository.CoordinatorRepository;
import com.tcc.core.service.ResourceCoordinatorService;
import com.tcc.core.utils.SpringApplicationHolder;

/**
 * description
 * 
 * @author xuyi 2018年10月9日
 */
@Service(value = "coordinatorService")
public class ResourceCoordinatorServiceImpl implements ResourceCoordinatorService {

	private static CoordinatorRepository coordinatorRepository;

	@Override
	public int create(TccTransaction tccTransaction) {
		return coordinatorRepository.create(tccTransaction);
	}

	@Override
	public TccTransaction findByTransId(String txId, String appName) {
		return coordinatorRepository.findById(txId, appName);
	}

	@Override
	public int remove(String txId, String appName) {
		return coordinatorRepository.remove(txId, appName);
	}

	@Override
	public int update(TccTransaction tccTransaction) {
		return coordinatorRepository.update(tccTransaction);
	}

	@Override
	public int updateParticipant(TccTransaction tccTransaction) {
		return coordinatorRepository.updateParticipant(tccTransaction);
	}

	@Override
	public int updateStatus(String txId, String appName, Integer status) {
		coordinatorRepository.updateStatus(txId, appName, status);
		return 0;
	}

	@Override
	public int start(TccConfig tccConfig) {
		coordinatorRepository = SpringApplicationHolder.getInstance().getBean(CoordinatorRepository.class);
		return 0;
	}
}
