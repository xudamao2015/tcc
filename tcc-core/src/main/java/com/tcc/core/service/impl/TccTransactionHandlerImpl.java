package com.tcc.core.service.impl;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.tcc.core.TccTransactionCacheManager;
import com.tcc.core.TccTransactionManager;
import com.tcc.core.common.TccTransactionContext;
import com.tcc.core.common.TccTransactionContextLocal;
import com.tcc.core.common.annotation.TccBizAction;
import com.tcc.core.common.type.TccRole;
import com.tcc.core.common.type.TccStatus;
import com.tcc.core.domain.entity.TccActionInvocation;
import com.tcc.core.domain.entity.TccParticipant;
import com.tcc.core.domain.entity.TccTransaction;
import com.tcc.core.service.TccTransactionHandler;

/**
 * description
 * 
 * @author xuyi 2018年10月9日
 */
@Service("tccTransactionHandler")
public class TccTransactionHandlerImpl implements TccTransactionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(TccTransactionHandlerImpl.class);

	@Autowired
	private TccTransactionManager tccTranactionManager;
	
	private TccTransactionCacheManager tccTxCacheManager = TccTransactionCacheManager.getInstance();

	private static final Executor executor = Executors.newFixedThreadPool(8);

	@Override
	public Object handle(ProceedingJoinPoint pjp, TccTransactionContext tccTxContext) throws Throwable {
		if (tccTxContext == null) {
			// Tcc事务发起者
			return initiatorHandler(pjp, tccTxContext);
		} else {
			int role = tccTxContext.getRole();
			if (role == TccRole.INITIATOR.getCode()) {
				// Tcc事务发起方，rpc调用时添加参与者
				return rootHandler(pjp, tccTxContext);
			} else if (role == TccRole.PARTICIPATE.getCode()) {
				// Tcc事务参与者
				return participantHandler(pjp, tccTxContext);
			} else {
				// 其他rpc client，事务消费时，直接调用
				return pjp.proceed();
			}
		}
	}
	
	private Object rootHandler(ProceedingJoinPoint pjp, TccTransactionContext tccTxContext) throws Throwable {
		if (TccStatus.TRYING.getCode() == tccTxContext.getStatus()) {
			TccParticipant participant = buildParticipant(pjp, tccTxContext);
			tccTranactionManager.addTccParticipant(participant);
		}
		return pjp.proceed();
	}
	
//	private Object localHandler(ProceedingJoinPoint pjp, TccTransactionContext tccTxContext) throws Throwable {
//		if (TccStatus.TRYING.getCode() == tccTxContext.getStatus()) {
//			TccParticipant participant = buildParticipant(pjp, tccTxContext);
//			tccTranactionManager.registerByNested(tccTxContext.getTransId(), participant);
//		}
//		return pjp.proceed();
//	}

	private Object initiatorHandler(ProceedingJoinPoint pjp, TccTransactionContext tccTxContext) throws Throwable {
		Object returnValue;
		try {
			TccTransaction tccTransaction = tccTranactionManager.beginRootTx(pjp);
			try {
				// execute try
				returnValue = pjp.proceed();
				tccTransaction.setStatus(TccStatus.TRYING.getCode());
				tccTranactionManager.updateStatus(tccTransaction);
			} catch (Throwable t) {
				TccTransaction currentTransaction = tccTranactionManager.getTccTx();
				logger.warn("trying phase exception in initiator service tccId:{}", currentTransaction.getTransactionId(), t);
				executor.execute(() -> tccTranactionManager.cancel(currentTransaction));
				throw t;
			}
			// execute confirm
			final TccTransaction currentTransaction = tccTranactionManager.getTccTx();
			executor.execute(() -> tccTranactionManager.confirm(currentTransaction));
		} finally {
			tccTranactionManager.removeCurrentTcc();
			TccTransactionContextLocal.remove();
		}
		return returnValue;
	}

	private Object participantHandler(ProceedingJoinPoint pjp, TccTransactionContext tccTxContext) throws Throwable {
		TccTransaction tccTransaction = null;
		TccTransaction currentTransaction;
		String transId = tccTxContext.getTransId();
		boolean ccflag = false;
		TccStatus tccStatus = TccStatus.getStatus(tccTxContext.getStatus()).orElse(TccStatus.TRYING);
		switch (tccStatus) {
		case TRYING:
			try {
				tccTransaction = tccTranactionManager.beginParticipant(tccTxContext, pjp);
				transId = tccTransaction.getTransactionId();
				final Object proceed = pjp.proceed();
				tccTransaction.setStatus(TccStatus.TRYING.getCode());
				// update log status to try
				
				tccTranactionManager.updateStatus(tccTransaction);
				return proceed;
			} catch (Throwable throwable) {
				// TODO: how to handle exception in participant service??
				// deleteTransaction(tccTransaction);
				throw throwable;
			} finally {
				tccTxCacheManager.removeByTransId(transId);
				tccTranactionManager.removeCurrentTcc();
			}
		case CONFIRMING:
			currentTransaction = tccTxCacheManager.getTccTransaction(transId);
			ccflag = tccTranactionManager.confirm(currentTransaction);
			break;

		case CANCELING:
			currentTransaction = tccTxCacheManager.getTccTransaction(transId);
			ccflag = tccTranactionManager.cancel(currentTransaction);
			break;
			
		default:
			break;
		}
		return ccflag;
	}

	private TccParticipant buildParticipant(ProceedingJoinPoint pjp, TccTransactionContext tccTxContext) {
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		Method method = signature.getMethod();
		Class<?> clazz = pjp.getTarget().getClass();
		Object[] args = pjp.getArgs();
		TccBizAction tcc = method.getAnnotation(TccBizAction.class);
		TccActionInvocation confirmInvocation = null;
		String confirmMethodName = tcc.confirmAction();
		String cancelMethodName = tcc.cancelAction();
		if (!StringUtils.isEmpty(confirmMethodName)) {
			confirmInvocation = new TccActionInvocation(clazz, confirmMethodName, method.getParameterTypes(), args);
		}
		TccActionInvocation cancelInvocation = null;
		if (!StringUtils.isEmpty(cancelMethodName)) {
			cancelInvocation = new TccActionInvocation(clazz, cancelMethodName, method.getParameterTypes(), args);
		}
		TccParticipant participant = new TccParticipant(tccTxContext.getTransId(), confirmInvocation, cancelInvocation);
		return participant;
	}
}
