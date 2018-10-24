package com.tcc.core;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.tcc.core.common.TccTransactionContext;
import com.tcc.core.common.TccTransactionContextLocal;
import com.tcc.core.common.annotation.TccBizAction;
import com.tcc.core.common.type.TccRole;
import com.tcc.core.common.type.TccStatus;
import com.tcc.core.domain.entity.TccActionInvocation;
import com.tcc.core.domain.entity.TccParticipant;
import com.tcc.core.domain.entity.TccTransaction;
import com.tcc.core.service.AppNameService;
import com.tcc.core.service.ResourceCoordinatorService;
import com.tcc.core.utils.SpringApplicationHolder;

/**
 * description
 * 
 * @author xuyi 2018年10月9日
 */
@Component
public class TccTransactionManager {
	private static final Logger logger = LoggerFactory.getLogger(TccTransactionManager.class);

	private static ThreadLocal<TccTransaction> tccTxHolder = new ThreadLocal<>();

	private static AppNameService appNameService;
	private TccTransactionCacheManager tccTxCacheManager = TccTransactionCacheManager.getInstance();
	static {
		appNameService = SpringApplicationHolder.getInstance().getBean(AppNameService.class);
	}

	@Autowired
	public ResourceCoordinatorService resourceCoordinatorService;

	public TccTransaction beginRootTx(ProceedingJoinPoint pjp) {
		logger.debug(".....tcc transaction！start: {}....", appNameService.getAppName());
		// build tccTransaction
		final TccTransaction tccTransaction = buildTccTransaction(pjp, TccRole.INITIATOR.getCode(), null,
				appNameService.getAppName());
		// save tccTransaction in threadLocal
		tccTxHolder.set(tccTransaction);
		// publishEvent
		resourceCoordinatorService.create(tccTransaction);
		// set TccTransactionContext this context transfer remote
		TccTransactionContext context = new TccTransactionContext();
		// set action is try
		context.setStatus(TccStatus.TRYING.getCode());
		context.setTransId(tccTransaction.getTransactionId());
		context.setRole(TccRole.LOCAL.getCode());
		TccTransactionContextLocal.set(context);
		return tccTransaction;
	}

	/**
	 * this is Participant transaction begin.
	 *
	 * @param context
	 *            transaction context.
	 * @param point
	 *            cut point
	 * @return TccTransaction
	 */
	public TccTransaction beginParticipant(final TccTransactionContext context, final ProceedingJoinPoint point) {
		logger.debug("...Participant tcc transaction ！start..：{}", context.toString());
		final TccTransaction tccTransaction = buildTccTransaction(point, TccRole.PARTICIPATE.getCode(),
				context.getTransId(), appNameService.getAppName());
		// save tccTransaction in threadLocal
		tccTxHolder.set(tccTransaction);
		TccTransactionCacheManager.getInstance().cacheTccTransaction(tccTransaction);
		resourceCoordinatorService.create(tccTransaction);
		// Nested transaction support
		context.setRole(TccRole.LOCAL.getCode());
		TccTransactionContextLocal.set(context);
		return tccTransaction;
	}

	public TccTransaction getTccTx() {
		return tccTxHolder.get();
	}

	public void updateStatus(TccTransaction tccTransaction) {
		tccTxCacheManager.cacheTccTransaction(tccTransaction);
		resourceCoordinatorService.updateStatus(tccTransaction.getTransactionId(), tccTransaction.getAppName(),
				tccTransaction.getStatus());
	}

	public void removeCurrentTcc() {
		tccTxHolder.remove();
	}

	public boolean confirm(TccTransaction tccTransaction) {
		boolean success = true;
		logger.debug("tcc confirm .......！start");
		if (Objects.isNull(tccTransaction) || CollectionUtils.isEmpty(tccTransaction.getParticipants())) {
			return success;
		}
		// 若当前服务的confirm方法已经被执行，则不再执行confirm9
		if (tccTransaction.getStatus() == TccStatus.CONFIRMED.getCode()) {
			return success;
		}
		tccTransaction.setStatus(TccStatus.CONFIRMING.getCode());
		updateStatus(tccTransaction);
		List<TccParticipant> participants = tccTransaction.getParticipants();
		List<TccParticipant> failList = Lists.newArrayListWithCapacity(participants.size());

		if (!CollectionUtils.isEmpty(participants)) {
			for (TccParticipant participant : participants) {
				try {
					TccTransactionContext tccTxcontext = new TccTransactionContext();
					tccTxcontext.setStatus(TccStatus.CONFIRMING.getCode());
					tccTxcontext.setRole(TccRole.CONSUME.getCode());
					tccTxcontext.setTransId(participant.getTransactionId());
					TccTransactionContextLocal.set(tccTxcontext);
					executeParticipantMethod(participant.getTccConfirmAction());
				} catch (Exception e) {
					logger.error("execute confirm :{}", e);
					success = false;
					failList.add(participant);
				}
			}
			// 确认成功，则变更成【已经确认】状态
			if (success) {
				tccTransaction.setStatus(TccStatus.CONFIRMED.getCode());
				updateStatus(tccTransaction);
			}
			executeHandler(success, tccTransaction, failList);
		}
		return success;
	}

	/**
	 * cancel transaction.
	 *
	 * @param currentTransaction
	 *            {@linkplain TccTransaction}
	 */
	public boolean cancel(TccTransaction tccTransaction) {
		boolean success = true;
		logger.debug("tcc(transid:{}) cancel start!", tccTransaction.getTransactionId());
		if (Objects.isNull(tccTransaction) || CollectionUtils.isEmpty(tccTransaction.getParticipants())) {
			return success;
		}
		// 若当前事务已经被取消不执行cancelling操作
		if (tccTransaction.getStatus() == TccStatus.CANCELED.getCode()) {
			return success;
		}
		List<TccParticipant> participants = tccTransaction.getParticipants();
		List<TccParticipant> failList = Lists.newArrayListWithCapacity(participants.size());
		if (!CollectionUtils.isEmpty(participants)) {
			tccTransaction.setStatus(TccStatus.CANCELING.getCode());
			// update cancel
			updateStatus(tccTransaction);
			for (TccParticipant participant : participants) {
				try {
					TccTransactionContext context = new TccTransactionContext();
					context.setStatus(TccStatus.CANCELING.getCode());
					context.setTransId(participant.getTransactionId());
					context.setRole(TccRole.CONSUME.getCode());
					TccTransactionContextLocal.set(context);
					executeParticipantMethod(participant.getTccCancelAction());
				} catch (Throwable e) {
					logger.warn("execute cancel ex.", e);
					success = false;
					failList.add(participant);
				}
			}
			// 确认成功，则变更成【已经确认】状态
			if (success) {
				tccTransaction.setStatus(TccStatus.CANCELED.getCode());
				updateStatus(tccTransaction);
			}
			executeHandler(success, tccTransaction, failList);
		}
		return success;
	}

	private void executeHandler(boolean success, TccTransaction currentTransaction, List<TccParticipant> failList) {
		TccTransactionContextLocal.remove();
		TccTransactionCacheManager.getInstance().removeByTransId(currentTransaction.getTransactionId());
		if (!CollectionUtils.isEmpty(failList)) {
			currentTransaction.setFailList(failList);
			updateTccParticipant(currentTransaction);
			//TODO 是否需要抛出异常
//			throw new TccRuntimeException(failList.toString());
		}

	}

	/**
	 * add participant.
	 *
	 * @param participant
	 *            {@linkplain TccParticipant}
	 */
	public void addTccParticipant(TccParticipant tccParticipant) {
		if (Objects.isNull(tccParticipant)) {
			return;
		}
		Optional.ofNullable(getTccTx()).ifPresent(c -> {
			c.addParticipant(tccParticipant);
			updateTccParticipant(c);
		});
	}

	/**
	 * when nested transaction add participant.
	 *
	 * @param transId
	 *            key
	 * @param participant
	 *            {@linkplain Participant}
	 */
	public void registerByNested(String transId, TccParticipant participant) {
		if (Objects.isNull(participant) || Objects.isNull(participant.getTccConfirmAction())
				|| Objects.isNull(participant.getTccCancelAction())) {
			return;
		}
		final TccTransaction tccTransaction = TccTransactionCacheManager.getInstance().getTccTransaction(transId);
		Optional.ofNullable(tccTransaction).ifPresent(c -> {
			c.addParticipant(participant);
			updateTccParticipant(c);
		});
	}

	public int updateTccParticipant(TccTransaction tccTx) {
		tccTxCacheManager.cacheTccTransaction(tccTx);
		return resourceCoordinatorService.updateParticipant(tccTx);
	}

	private TccTransaction buildTccTransaction(ProceedingJoinPoint point, int role, String transId, String appName) {
		TccTransaction tccTransaction;
		tccTransaction = new TccTransaction(appName, transId);
		tccTransaction.setUpdateTime(new Date());
		tccTransaction.setStatus(TccStatus.PRE_TRY.getCode());
		tccTransaction.setRole(role);
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		Class<?> clazz = point.getTarget().getClass();
		Object[] args = point.getArgs();
		final TccBizAction tcc = method.getAnnotation(TccBizAction.class);
		tccTransaction.setTargetClass(clazz.getName());
		tccTransaction.setTargetMethod(method.getName());
		TccActionInvocation confirmInvocation = null;
		String confirmMethodName = tcc.confirmAction();
		String cancelMethodName = tcc.cancelAction();
		if (StringUtils.isNoneBlank(confirmMethodName)) {
			tccTransaction.setConfirmAction(confirmMethodName);
			confirmInvocation = new TccActionInvocation(clazz, confirmMethodName, method.getParameterTypes(), args);
		}
		TccActionInvocation cancelInvocation = null;
		if (StringUtils.isNoneBlank(cancelMethodName)) {
			tccTransaction.setCancelAction(cancelMethodName);
			cancelInvocation = new TccActionInvocation(clazz, cancelMethodName, method.getParameterTypes(), args);
		}
		final TccParticipant participant = new TccParticipant(tccTransaction.getTransactionId(), confirmInvocation,
				cancelInvocation);
		tccTransaction.addParticipant(participant);
		return tccTransaction;
	}

	private static void executeParticipantMethod(TccActionInvocation tccInvocation) throws Exception {
		if (Objects.nonNull(tccInvocation)) {
			Class<?> clazz = tccInvocation.getTargetClazz();
			String method = tccInvocation.getTargetMethod();
			Object[] args = tccInvocation.getParams();
			Class<?>[] parameterTypes = tccInvocation.getParamTypes();
			Object bean = SpringApplicationHolder.getInstance().getBean(clazz);
			MethodUtils.invokeMethod(bean, method, args, parameterTypes);
		}
	}
}
