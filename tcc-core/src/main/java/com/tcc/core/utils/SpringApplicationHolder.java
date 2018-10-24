package com.tcc.core.utils;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

import lombok.Getter;
import lombok.Setter;

/**
 * spring容器 bean操作工具
 * 
 * @author xuyi 2018年10月9日
 */
public class SpringApplicationHolder {

	private static final SpringApplicationHolder INSTANCE;

	@Setter
	@Getter
	private ConfigurableApplicationContext appCtx;

	static {
		INSTANCE = new SpringApplicationHolder();
	}

	public static SpringApplicationHolder getInstance() {
		return INSTANCE;
	}

	public Object getBean(String beanName) {
		Assert.notNull(beanName, "beanName is required!");
		return appCtx.getBean(beanName);
	}

	public <T> T getBean(Class<T> type) {
		return appCtx.getBean(type);
	}

	public void registerBean(String beanName, Object singletonObject) {
		Assert.notNull(beanName, "beanName is required!");
		Assert.notNull(singletonObject, "singletonObject is required!");
		appCtx.getBeanFactory().registerSingleton(beanName, singletonObject);
	}
}
