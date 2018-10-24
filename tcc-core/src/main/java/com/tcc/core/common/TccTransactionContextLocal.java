package com.tcc.core.common;

/**
 * 把Tcc事务上下文绑定到当前线程上
 * 
 * @author xuyi 2018年10月10日
 */
public class TccTransactionContextLocal {

	private static ThreadLocal<TccTransactionContext> current = new ThreadLocal<>();

	public static TccTransactionContext get() {
		return current.get();
	}

	public static void set(TccTransactionContext tccTxContext) {
		current.set(tccTxContext);
	}

	public static void remove() {
		current.remove();
	}
}
