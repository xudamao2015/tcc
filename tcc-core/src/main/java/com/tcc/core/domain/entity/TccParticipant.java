package com.tcc.core.domain.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * tcc 事务参与者
 * 
 * @author xuyi 2018年9月30日
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TccParticipant implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8596137537720804830L;
	private String transactionId;
	private TccActionInvocation tccConfirmAction;
	private TccActionInvocation tccCancelAction;
}
