package com.tcc.demo.springcloud.order.domain.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import com.tcc.demo.springcloud.order.domain.entity.Order;

import tk.mybatis.mapper.common.Mapper;

public interface OrderMapper extends Mapper<Order> {

	@Update("update `tcc_order` o set o.status = #{status} where o.number=#{number}")
	int updateStatus(Order order);

	@Insert(" insert into `tcc_order` (create_time,number,status,product_id,total_amount,count,user_id) "
			+ " values ( #{createTime},#{number},#{status},#{productId},#{totalAmount},#{count},#{userId})")
	int create(Order order);
}