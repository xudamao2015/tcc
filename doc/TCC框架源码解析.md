# TCC分布式事务框架实现与源码解析

### 一、核心模块Tcc-core

一个完整的 TCC 分布式事务流程如下：

　　主业务服务首先开启本地事务;

　　主业务服务向业务活动管理器申请启动分布式事务主业务活动;

　　然后针对要调用的从业务服务，主业务活动先向业务活动管理器注册从业务活动，然后调用从业务服务的 Try 接口;

　　当所有从业务服务的 Try 接口调用成功，主业务服务提交本地事务;若调用失败，主业务服务回滚本地事务;

　　若主业务服务提交本地事务，则 TCC 模型分别调用所有从业务服务的 Confirm 接口;若主业务服务回滚本地事务，则分别调用 Cancel 接口;

　　所有从业务服务的 Confirm 或 Cancel 操作完成后，全局事务结束。



#### 抽象模型：

每个服务内部需要维护一个与当前线程绑定的事务上下文，和TCC事务对象。在进行RPC调用时，需要把封装好的事务上下文传递到被调用的服务中，同时，要把各RPC服务的调用点作为整个TCC事务的参与者，添加到本地事务对象中。本地事务需要进行持久化，我们选择DB的方式进行持久化。



#### TCC事务拦截器

通过切面方式拦截业务代码中的TccBizAction注解。调用tccTransactionInterceptor进行事务切面处理。

```java
@Aspect
@Component
public class TccTransactionAspect {

    private TccTransactionInterceptor tccTransactionInterceptor;

    @Autowired
    protected void setTccTransactionInterceptor(final TccTransactionInterceptor tccTransactionInterceptor) {
        this.tccTransactionInterceptor = tccTransactionInterceptor;
    }

    /**
     * this is point cut with {@linkplain com.tcc.core.common.annotation.TccBizAction }.
     */
    @Pointcut("@annotation(com.tcc.core.common.annotation.TccBizAction)")
    public void TccInterceptor() {
    }

    @Around("TccInterceptor()")
    public Object interceptTccMethod(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return tccTransactionInterceptor.interceptor(proceedingJoinPoint);
    }

}
```

#### TCC事务处理器

（代码即注释）根据事务上下文参数判定当前切面方法是是否会开启一个TCC事务。根据事务角色判定，进行不同逻辑处理

```java
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
```



#### TCC事务初始化

```java
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
```



#### TCC参与者处理

```java
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

```



### 二、Springcloud框架胶水代码

#### 针对`TccBizAction`注解的TCC切点处理

1.获取当前线程绑定的事务上下文；

2.当前线程事务上下文不存在时，从request的header中获取rpc调用的事务上下文。

3.获取并封装事务上下文信息，并调用Tcc-core中的`tccTransactionHandler`

```java
@Component
public class SpringCloudTccTransactionInterceptor implements TccTransactionInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(SpringCloudTccTransactionInterceptor.class);

	@Autowired
	private TccTransactionHandler tccTransactionHandler;

	@Override
	public Object interceptor(final ProceedingJoinPoint pjp) throws Throwable {
		// 获取当前线程绑定的事务上下文
		TccTransactionContext tccTransactionContext = TccTransactionContextLocal.get();

		if (tccTransactionContext == null) {
			// 判断是否成为上游服务的tcc事务参与者
			RequestAttributes requestAttributes = null;
			try {
				requestAttributes = RequestContextHolder.currentRequestAttributes();
			} catch (Throwable ex) {
				logger.warn("can not acquire request info:", ex);
			}
			HttpServletRequest request = requestAttributes == null ? null
					: ((ServletRequestAttributes) requestAttributes).getRequest();
			String context = request == null ? null : request.getHeader(CommonConstant.TCC_TRANSACTION_CONTEXT);
			if (StringUtils.isNoneBlank(context)) {
				tccTransactionContext = JsonUtils.parseJson(context, TccTransactionContext.class);
			}
		}

		return tccTransactionHandler.handle(pjp, tccTransactionContext);
	}

}
```

#### 通过Feignclient进行RPC调用，传递事务上下文，并配置feignclient

rpc调用时，在http header上面添加tcc事务上下文

```java
/**
 * TccRestTemplateConfiguration.
 * 
 * @author xuyi
 */
@Configuration
public class TccFeignConfiguration {

	/**
	 * build feign.
	 *
	 * @return Feign.Builder
	 */
	@Bean
	@Scope("prototype")
	public Feign.Builder feignBuilder() {
		return Feign.builder().invocationHandlerFactory((target, dispatch) -> {
			// tcc事务切面，把rpc调用点封装到当前线程绑定的tcc事务中
			TccFeignHandler handler = new TccFeignHandler();
			handler.setHandlers(dispatch);
			return handler;
		}).requestInterceptor(request -> {
			// rpc调用时，在http header上面添加tcc事务上下文
			TccTransactionContext currentTx = TccTransactionContextLocal.get();
			if (currentTx != null) {
				TccTransactionContext paramTxContext = new TccTransactionContext();
				paramTxContext.setTransId(currentTx.getTransId());
				paramTxContext.setStatus(currentTx.getStatus());
				paramTxContext.setRole(TccRole.PARTICIPATE.getCode());
				String tccJson = JsonUtils.toJson(paramTxContext);
				request.header(CommonConstant.TCC_TRANSACTION_CONTEXT, tccJson);
			}
		}).retryer(Retryer.NEVER_RETRY);

	}

	@Bean
	public Options option() {
		// read timeout调整到10s
		return new Request.Options(5000, 1000_000);
	}

}

```



#### 通过Feignclient进行RPC调用时，把远程参与者信息加入到TCC事务中

1.拦截所有的feignclient远程调用

2.对于存在TccBizAction注解的远程调用执行以下操作：

3.获取当前线程的事务上下文信息，并把封装参与者调用点到当前线程中。

```java
/**
 * TccFeignHandler.
 * 
 * rpc调用时，把tcc参与者的调用掉封装到当前线程中
 *
 * @author xuyi
 */
public class TccFeignHandler implements InvocationHandler {

	private Map<Method, MethodHandler> handlers;

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final TccBizAction tcc = method.getAnnotation(TccBizAction.class);
		// 无事务切面时，直接进行rpc调用
		if (Objects.isNull(tcc)) {
			return this.handlers.get(method).invoke(args);
		}
		try {
			TccTransactionContext tccTransactionContext = TccTransactionContextLocal.get();
			if (tccTransactionContext != null) {
				TccTransactionManager tccTransactionManager = SpringApplicationHolder.getInstance().getAppCtx()
						.getBean(TccTransactionManager.class);
				TccParticipant participant = buildParticipant(tcc, method, args, tccTransactionContext);
				tccTransactionManager.addTccParticipant(participant);
			}
			Object invoke = this.handlers.get(method).invoke(args);

			return invoke;
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			throw throwable;
		}
	}
}
```

### 三、用于事务监控的Tcc-web

开发中。。。



### 四、dubbo框架胶水代码

开发中。。。