package com.tcc.core.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import com.tcc.core.common.config.TccConfig;
import com.tcc.core.domain.entity.TccTransaction;
import com.tcc.core.repository.CoordinatorRepository;
import com.tcc.core.repository.impl.DbCoordinatorRepository;
import com.tcc.core.serializer.KryoSerializer;
import com.tcc.core.service.ResourceCoordinatorService;
import com.tcc.core.utils.SpringApplicationHolder;

import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tcc 事务启动类
 * 
 * @author xuyi 2018年10月9日
 */
@NoArgsConstructor
public class TccTransactionBootstrap implements ApplicationContextAware {

	private static final Logger logger = LoggerFactory.getLogger(TccTransactionBootstrap.class);

	@Setter
	private TccConfig tccConfig;
	
	@Autowired
	private ResourceCoordinatorService coordinatorService;

	@Override
	public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
		SpringApplicationHolder.getInstance().setAppCtx((ConfigurableApplicationContext) appCtx);
		initialization(tccConfig);
	}

	/**
	 * initialization.
	 *
	 * @param tccConfig
	 *            {@linkplain TccConfig}
	 */
	private void initialization(TccConfig tccConfig) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> logger.info("tcc tx shutdown now")));
		try {
			loadSpiSupport(tccConfig);
			coordinatorService.start(tccConfig);
		} catch (Exception ex) {
			logger.error("tcc tx init exception:{}", ex);
		}
		logger.info(" tcc tx init success!");
	}

	/**
	 * load spi.
	 *
	 * @param tccConfig
	 *            {@linkplain TccConfig}
	 */
	private void loadSpiSupport(final TccConfig tccConfig) {
		// TODO config serialize
		// 序列化方式，事务持久化方式，datasource
		CoordinatorRepository repository = new DbCoordinatorRepository();
		repository.setSerialize(new KryoSerializer<TccTransaction>());
		repository.init(tccConfig);
		SpringApplicationHolder.getInstance().registerBean(CoordinatorRepository.class.getName(), repository);
	}

}
