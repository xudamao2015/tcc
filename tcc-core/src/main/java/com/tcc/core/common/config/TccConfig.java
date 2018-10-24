package com.tcc.core.common.config;

import com.tcc.core.common.constant.CommonConstant;

import lombok.Data;

/**
 * description
 * 
 * @author xuyi 2018年10月9日
 */
@Data
public class TccConfig {

	private String tableName = CommonConstant.DB_TABLE; // 数据库中Transaction表

	private String serializer = "org.tcc.core.serializer.KryoSerializer";

	/**
	 * scheduledPool Thread size.
	 */
	private int scheduledThreadMax = Runtime.getRuntime().availableProcessors() << 1;

	/**
	 * scheduledPool scheduledDelay unit SECONDS.
	 */
	private int scheduledDelay = 60;

	/**
	 * retry max.
	 */
	private int retryMax = 3;

	/**
	 * recoverDelayTime Unit seconds (note that this time represents how many
	 * seconds after the local transaction was created before execution).
	 */
	private int recoverDelayTime = 60;

	/**
	 * Parameters when participants perform their own recovery. 1.such as RPC calls
	 * time out 2.such as the starter down machine
	 */
	private int loadFactor = 2;

	/**
	 * repositorySupport. {@linkplain RepositorySupportEnum}
	 */
	private String repositorySupport = "db";

	/**
	 * disruptor bufferSize.
	 */
	private int bufferSize = 4096 * 2 * 2;

	/**
	 * this is disruptor consumerThreads.
	 */
	private int consumerThreads = Runtime.getRuntime().availableProcessors() << 1;

	/**
	 * db config.
	 */
	private TccDataSourceProperties tccDbConfig;

}
