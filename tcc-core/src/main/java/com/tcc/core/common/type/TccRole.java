package com.tcc.core.common.type;

import lombok.Getter;

/**
 * Tcc事务 角色定义
 * 
 * @author xuyi 2018年9月30日
 */
@Getter
public enum TccRole {

	INITIATOR(1, "事务发起者"), PARTICIPATE(2, "事务参与者"), LOCAL(3, "本地事务"), RPC(6, "远程调用"), CONSUME(4, "消费事务");

	private int code;

	private String desc;

	private TccRole(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
}
