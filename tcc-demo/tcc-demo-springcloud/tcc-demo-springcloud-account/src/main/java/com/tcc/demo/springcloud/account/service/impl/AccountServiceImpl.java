package com.tcc.demo.springcloud.account.service.impl;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tcc.core.common.annotation.TccBizAction;
import com.tcc.demo.springcloud.account.client.InventoryService;
import com.tcc.demo.springcloud.account.domain.dto.AccountDTO;
import com.tcc.demo.springcloud.account.domain.dto.InventoryDTO;
import com.tcc.demo.springcloud.account.domain.dto.OrderDTO;
import com.tcc.demo.springcloud.account.domain.entity.Account;
import com.tcc.demo.springcloud.account.domain.mapper.AccountMapper;
import com.tcc.demo.springcloud.account.service.AccountService;

/**
 * description
 * 
 * @author xuyi 2018年10月16日
 */
@Service
public class AccountServiceImpl implements AccountService {

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private InventoryService inventoryService;

	@Override
	public BigDecimal getBalance(String userId) {
		return accountMapper.getBalance(userId);
	}

	@Override
	@Transactional
	@TccBizAction(confirmAction = "confirm", cancelAction = "cancel")
	public boolean tryPay(AccountDTO accoutDto) throws Exception {
		Account current = accountMapper.getAccount(accoutDto.getUserId());
		BigDecimal balance = current.getBalance().subtract(accoutDto.getAmount());
		if (balance.compareTo(BigDecimal.ZERO) < 0) {
			throw new Exception("余额不足，冻结金额失败！");
		}
		Account account = new Account();
		account.setFreezeAmount(current.getFreezeAmount().add(accoutDto.getAmount()));
		account.setUpdateTime(new Date());
		account.setBalance(balance);
		account.setUserId(accoutDto.getUserId());
		int r = accountMapper.payMoney(account);
		boolean result = r == 1 ? true : false;
		return result;
	}

	// 接口不需要满足幂等性、由tcc框架保证调用且仅调用一次
	@Transactional
	public boolean confirm(AccountDTO accoutDto) {
		Account current = accountMapper.getAccount(accoutDto.getUserId());
		Account account = new Account();
		account.setBalance(current.getBalance());// 余额保持不变
		account.setFreezeAmount(current.getFreezeAmount().subtract(accoutDto.getAmount()));// 扣减冻结金额
		account.setUpdateTime(new Date());
		account.setUserId(accoutDto.getUserId());
		int r = accountMapper.payMoney(account);
		boolean result = r == 1 ? true : false;
		return result;
	}

	// ： 接口不需要满足幂等性、由tcc框架保证调用且仅调用一次
	@Transactional
	public boolean cancel(AccountDTO accoutDto) {
		Account current = accountMapper.getAccount(accoutDto.getUserId());
		Account account = new Account();
		account.setFreezeAmount(current.getFreezeAmount().subtract(accoutDto.getAmount()));// 扣减冻结金额
		account.setBalance(current.getBalance().add(accoutDto.getAmount()));// 回复冻结金额到余额
		account.setUpdateTime(new Date());
		account.setUserId(accoutDto.getUserId());
		int r = accountMapper.payMoney(account);
		boolean result = r == 1 ? true : false;
		return result;
	}

	@Override
	@Transactional
	@TccBizAction(confirmAction = "confirmOrder", cancelAction = "cancelOrder")
	public boolean paymentNested(OrderDTO orderDto) throws Exception {
		Account current = accountMapper.getAccount(orderDto.getUserId());
		BigDecimal balance = current.getBalance().subtract(orderDto.getAmount());
		if (balance.compareTo(BigDecimal.ZERO) < 0) {
			throw new Exception("余额不足，冻结金额失败！");
		}
		int totalInventory = inventoryService.findByProductId(orderDto.getProductId());
		if (totalInventory < orderDto.getCount()) {
			throw new Exception("库存不足，冻结库存失败！");
		}
		Account account = new Account();
		account.setUserId(orderDto.getUserId());
		account.setFreezeAmount(current.getFreezeAmount().add(orderDto.getAmount()));
		account.setUpdateTime(new Date());
		account.setBalance(balance);
		int r = accountMapper.payMoney(account);
		if (r != 1) {
			throw new Exception("冻结金额失败");
		}
		InventoryDTO inventoryDTO = new InventoryDTO();
		inventoryDTO.setCount(orderDto.getCount());
		inventoryDTO.setProductId(orderDto.getProductId());
		boolean result = inventoryService.decrease(inventoryDTO);
		return result;
	}

	public boolean confirmOrder(OrderDTO orderDto) throws Exception {
		AccountDTO accountDto = new AccountDTO();
		accountDto.setAmount(orderDto.getAmount());
		accountDto.setUserId(orderDto.getUserId());
		return this.confirm(accountDto);
	}

	public boolean cancelOrder(OrderDTO orderDto) throws Exception {
		AccountDTO accountDto = new AccountDTO();
		accountDto.setAmount(orderDto.getAmount());
		accountDto.setUserId(orderDto.getUserId());
		return this.cancel(accountDto);
	}

	@Override
	@Transactional
	@TccBizAction(confirmAction = "confirm", cancelAction = "cancel")
	public boolean paymentNestedException(OrderDTO orderDto) throws Exception {
		InventoryDTO inventoryDTO = new InventoryDTO();
		inventoryDTO.setCount(orderDto.getCount());
		inventoryDTO.setProductId(orderDto.getProductId());
		boolean result = inventoryService.mockWithTryException(inventoryDTO);
		return result;
	}

	@Override
	public boolean payAndReduceInventory(AccountDTO accountDto) {
		return false;
	}
}
