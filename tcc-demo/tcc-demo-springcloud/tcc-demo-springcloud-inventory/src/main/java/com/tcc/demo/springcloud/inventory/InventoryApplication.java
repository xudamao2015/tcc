package com.tcc.demo.springcloud.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.tcc.core.utils.SpringApplicationHolder;

/**
 * 库存管理模块
 *
 */
// TODO 解决SpringApplicationHolder注入问题
@SpringBootApplication(scanBasePackages = { "com.tcc.springcloud", "com.tcc.spring.boot.starter",
		"com.tcc.demo.springcloud.inventory" })
@EnableDiscoveryClient
@EnableFeignClients
@EnableAspectJAutoProxy
@MapperScan("com.tcc.demo.springcloud.inventory.domain.mapper")
public class InventoryApplication implements ApplicationContextAware {
	public static void main(String[] args) {
		SpringApplication.run(InventoryApplication.class, args);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringApplicationHolder.getInstance().setAppCtx((ConfigurableApplicationContext) applicationContext);
	}
}
