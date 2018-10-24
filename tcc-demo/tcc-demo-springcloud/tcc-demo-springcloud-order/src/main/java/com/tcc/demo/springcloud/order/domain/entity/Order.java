package com.tcc.demo.springcloud.order.domain.entity;

import java.util.Date;
import javax.persistence.*;

import lombok.Data;

@Data
public class Order {
	@Id
	private long id;

	@Column(name = "create_time")
	private Date createTime;

	private String number;

	private int status;

	@Column(name = "product_id")
	private String productId;

	@Column(name = "total_amount")
	private long totalAmount;

	private int count;

	@Column(name = "user_id")
	private String userId;
}