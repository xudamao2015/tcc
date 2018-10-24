package com.tcc.demo.springcloud.account.controller;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.tcc.demo.springcloud.account.domain.dto.AccountDTO;
import com.tcc.demo.springcloud.account.domain.dto.OrderDTO;
import com.tcc.demo.springcloud.account.service.AccountService;

/**
 * description
 * 
 * @author xuyi 2018年10月15日
 */
@RestController
@RequestMapping("account")
public class AccountController {

	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

	@Autowired
	private AccountService accountService;

	/**
	 * 用户账户付款
	 *
	 * @param accountDO
	 *            实体类
	 * @return true 成功
	 */
	@ResponseBody
	@RequestMapping(value = "pay", method = { RequestMethod.POST })
	Boolean pay(@RequestBody AccountDTO accountDto) {
		boolean result = false;
		try {
			result = accountService.tryPay(accountDto);
		} catch (Exception e) {
			logger.error("用户账号付款失败:{}", e.getMessage());
		}
		return result;
	}

	/**
	 * 用户账户付款
	 *
	 * @param accountDO
	 *            实体类
	 * @return true 成功
	 */
	@ResponseBody
	@RequestMapping(value = "payNested", method = { RequestMethod.POST })
	Boolean payNested(@RequestBody OrderDTO orderDto) {
		boolean result = false;
		try {
			result = accountService.paymentNested(orderDto);
		} catch (Exception e) {
			logger.error("用户账号付款失败:{}", e.getMessage());
		}
		return result;
	}

	/**
	 * 用户账户付款
	 *
	 * @param accountDO
	 *            实体类
	 * @return true 成功
	 */
	@ResponseBody
	@RequestMapping(value = "payNestedException", method = { RequestMethod.POST })
	Boolean payNestedException(@RequestBody OrderDTO orderDto) {
		boolean result = false;
		try {
			result = accountService.paymentNested(orderDto);
		} catch (Exception e) {
			logger.error("用户账号付款失败:{}", e.getMessage());
		}
		return result;
	}
	
	/**
	 * 获取用户账户信息
	 *
	 * @param userId
	 *            用户id
	 * @return AccountDO
	 */
	@RequestMapping(value = "/findByUserId", method = { RequestMethod.POST, RequestMethod.GET })
	BigDecimal findByUserId(@RequestParam("userId") String userId) {
		return accountService.getBalance(userId);
	}
}
