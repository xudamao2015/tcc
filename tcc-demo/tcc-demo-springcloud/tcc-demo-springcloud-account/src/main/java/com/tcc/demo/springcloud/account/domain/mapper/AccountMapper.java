package com.tcc.demo.springcloud.account.domain.mapper;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.tcc.demo.springcloud.account.domain.entity.Account;
import tk.mybatis.mapper.common.Mapper;

public interface AccountMapper extends Mapper<Account> {

	@Select("select c.balance as balance, c.user_id as userId, c.freeze_amount as freezeAmount from `tcc_account` c where c.user_id=#{userId}")
	Account getAccount(@Param("userId") String userId);

	@Select("select c.balance from `tcc_account` c where c.user_id=#{userId}")
	BigDecimal getBalance(@Param("userId") String userId);

	@Update("update `tcc_account` c set c.freeze_amount=c.freeze_amount-#{freezeAmount} where c.user_id=#{userId}")
	BigDecimal confirm(Account account);

	@Update("update `tcc_account` c set c.balance=c.balance+#{freezeAmount}, c.freeze_amount=c.freeze_amount-#{freezeAmount} where c.user_id=#{userId}")
	BigDecimal cancel(Account account);

	@Update("update `tcc_account` c set c.balance=#{balance}, c.freeze_amount=#{freezeAmount},"
			+ "c.update_time=#{updateTime} where c.user_id=#{userId}")
	int payMoney(Account account);
}