package com.tcc.core.domain.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.tcc.core.common.type.TccRole;
import com.tcc.core.common.type.TccStatus;

import lombok.Data;
import lombok.ToString;

/**
 * Tcc 事务
 * 
 * @author xuyi 2018/09/30
 *
 */
@Data
@ToString
public class TccTransaction implements Serializable {

	private static final long serialVersionUID = -1547458702343536277L;

	private String transactionId;

	private String appName;

	private List<TccParticipant> participants;

	private List<TccParticipant> failList;

	private volatile int retriedCount;

	/**
	 * transaction status . {@linkplain TccStatus}
	 */
	private volatile int status;

	/**
	 * transaction role . {@linkplain TccRole}
	 */
	private int role;

	private int version;

	private Date createTime;

	private Date updateTime;

	private Integer pattern;

	private String targetClass;

	private String targetMethod;

	private String confirmAction;

	private String cancelAction;

	public TccTransaction(String appName) {
		this(appName, null);
	}

	public TccTransaction(String appName, String transactionId) {
		this.transactionId = transactionId;
		this.createTime = new Date();
		if (StringUtils.isEmpty(this.transactionId)) {
			this.transactionId = String.valueOf(this.createTime.getTime()) + "_"
					+ String.valueOf(Math.abs(UUID.randomUUID().toString().hashCode()));
		}

		this.participants = new ArrayList<TccParticipant>(8);
		this.appName = appName;
	}

	public void addParticipant(TccParticipant tccParticipant) {
		this.participants.add(tccParticipant);
	}
}
