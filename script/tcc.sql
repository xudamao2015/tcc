TRUNCATE `TCC`.`tcc_inventory_service`;
TRUNCATE `TCC`.`tcc_order_service`;
TRUNCATE `TCC`.`tcc_account_service`;
TRUNCATE `tcc_account`.`account`;
TRUNCATE `tcc_order`.`order`;
TRUNCATE `tcc_stock`.`inventory`;
CREATE DATABASE `TCC`;

CREATE TABLE `tcc_transaction` (
  `trans_id` varchar(64) NOT NULL COMMENT '事务ID',
  `app_id` varchar(64) NOT NULL COMMENT '服务名称',
  `target_class` varchar(256) DEFAULT NULL COMMENT '切面类',
  `target_method` varchar(128) DEFAULT NULL COMMENT '切面方法',
  `confirm_method` varchar(128) DEFAULT NULL COMMENT '确认方法名称',
  `cancel_method` varchar(128) DEFAULT NULL COMMENT '取消方法名称',
  `retried_count` tinyint(4) NOT NULL COMMENT '重试次数',
  `create_time` datetime NOT NULL COMMENT '事务创建时间',
  `update_time` datetime NOT NULL COMMENT '事务更新时间',
  `version` tinyint(4) NOT NULL COMMENT '版本',
  `status` tinyint(4) NOT NULL COMMENT '事务状态\nPRE_TRY(0, "try 开始执行"),\nTRYING(1, "Try 执行完成"),\nCONFIRMING(2, "Comfirm 执行确认"),\nCANCELING(3, "Cancel 执行取消"),\nCONFIRMED(4, "confirmed 执行完成"),\nCANCELED(5, "Canceled 取消完成");',
  `invocation` longblob COMMENT '参与者调用点',
  `role` tinyint(4) NOT NULL COMMENT '角色',
  `pattern` tinyint(4) DEFAULT NULL COMMENT '模式',
  `faillist` longblob,
  PRIMARY KEY (`trans_id`,`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

use tcc_order;
CREATE TABLE `tcc_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime NOT NULL,
  `number` varchar(64) COLLATE utf8mb4_bin NOT NULL,
  `status` tinyint(4) NOT NULL,
  `product_id` varchar(128) COLLATE utf8mb4_bin NOT NULL,
  `total_amount` int(11) NOT NULL,
  `count` int(4) NOT NULL,
  `user_id` varchar(128) COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
use tcc_stock;
CREATE TABLE `tcc_inventory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` varchar(128) COLLATE utf8mb4_bin NOT NULL,
  `total_inventory` int(10) NOT NULL COMMENT '总库存',
  `lock_inventory` int(10) NOT NULL COMMENT '锁定库存',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
use tcc_account;
CREATE TABLE `tcc_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,tcc_account
  `user_id` varchar(128) COLLATE utf8mb4_bin NOT NULL,
  `balance` decimal(10,0) NOT NULL COMMENT '用户余额',
  `freeze_amount` decimal(10,0) NOT NULL COMMENT '冻结金额，扣款暂存余额',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

insert  into `tcc_account`.`tcc_account`(`id`,`user_id`,`balance`,`freeze_amount`,`create_time`,`update_time`) values (1,'10000',10000,0,'2017-09-18 14:54:22',NULL);
insert  into `tcc_stock`.`tcc_inventory`(`id`,`product_id`,`total_inventory`,`lock_inventory`) values (1,'1',10000,0);

commit;


select * from `tcc_account`.`tcc_account`;
select * from `tcc_stock`.`tcc_inventory`;
