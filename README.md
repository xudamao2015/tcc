# TCC分布式事务框架

### 基于服务层补偿的Tcc分布式事务解决方案，基于java 1.8编写，目前支持springcloud应用框架。

#### 业务层只需把关注点放到资源预留的try方法，业务补偿的confirm/cancel方法的实现上。try成功或者失败后，由tcc框架进行补偿方法（confirm/cancel）调用，减少业务层重复编码和人工介入的重复劳动。



### 相关文档

- [事务与分布式事务](doc/事务与分布式事务.md)
- [TCC框架源码解析](doc/TCC框架源码解析.md)
- [TCC框架使用指南--springcloud版本](doc/分布式事务框架使用指南.md)
- TCC框架使用指南--dubbo版本



### 一、框架特性

 * 支持跨服务的嵌套事务(Nested transaction support)

 * 支持本地服务内部，多事务资源管理

 * 支持跨服务，多个服务参与者的事务

 * 基于springboot的自动配置能力，使用简单。

 * 采用面向切面编程思想，对业务代码侵入性很低。

 * 目前支持springcloud，以后会加入对dubbo的支持。

 * 本地事务持久化 : mysql。

 * 支持事务参与者列表序列化 ：hessian，kryo。

 * 内置经典的分布式事务场景demo工程，并有swagger-ui可视化界面。

 * 内置本地事务状态机，实现confirm和cancel操作不会被多次执行，放宽了幂等性要求。

 * 代码易于理解，使用简单和自定义扩展


### 二、TCC原理概述

>***TCC事务***
>
>为了解决在事务运行过程中大颗粒度资源锁定的问题，业界提出一种新的事务模型，它是基于**业务层面**的事务定义。锁粒度完全由业务自己控制。它本质是一种补偿的思路。它把事务运行过程分成 Try、Confirm / Cancel 两个阶段。在每个阶段的逻辑由**业务代码控制**。这样就事务的锁粒度可以完全自由控制。业务可以在牺牲隔离性的情况下，获取更高的性能。

- Try 阶段：尝试执行业务 

  - 完成所有业务检查( 一致性 ) 
  - 预留必须业务资源( 准隔离性 )

- Confirm 阶段：确认执行业务

  - 真正执行业务，不做任务业务检查

- Cancel阶段 ：取消执行业务

  - 释放 Try 阶段预留的业务资源

  Confirm 与 Cancel 互斥


### 三、快速开始

##### 开发环境

java8

mysql

docker



编译代码

```shell
mvn package
```

- 1.在Mysql客户端先执行script/tcc.sql，创建TCC数据库；

- 2.启动服务注册中心：`java -jar tcc-demo-springcloud-eureka-0.0.1-SNAPSHOT.jar`

- 3.启动业务服务: 

    - 库存服务：`tcc-demo-springcloud-inventory`

    - 账户服务：`tcc-demo-springcloud-account`

    - 订单服务：`tcc-demo-springcloud-order`




### 四、开发计划

1.使用docker-componse对demo工程进行服务编排，方便使用

2.在事务状态变更，持久化等操作中，使用事件驱动的方式优化性能

3.进行benchmark测试

4.添加dubbo框架的支持