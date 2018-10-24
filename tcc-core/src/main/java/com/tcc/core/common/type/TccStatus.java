package com.tcc.core.common.type;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;

/**
 * Tcc事务 执行状态
 * 
 * @author xuyi 2018年9月30日
 */
@Getter
public enum TccStatus {
	
	PRE_TRY(0, "try 开始执行"),

	TRYING(1, "Try 执行完成"),

	CONFIRMING(2, "Comfirm 执行确认"),

	CANCELING(3, "Cancel 执行取消"),

	CONFIRMED(4, "confirmed 执行完成"),
	
	CANCELED(5, "Canceled 取消完成");
	
	private int code;

	private String desc;

	private TccStatus(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static Optional<TccStatus> getStatus(int code) {
		return Arrays.stream(TccStatus.values()).filter(c -> c.getCode() == code).findFirst();
	}
}
