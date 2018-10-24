package com.tcc.demo.springcloud.inventory.domain.entity;

import javax.persistence.*;

public class Inventory {
    @Id
    private Long id;

    @Column(name = "product_id")
    private String productId;

    /**
     * 总库存
     */
    @Column(name = "total_inventory")
    private Integer totalInventory;

    /**
     * 锁定库存
     */
    @Column(name = "lock_inventory")
    private Integer lockInventory;

    /**
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return product_id
     */
    public String getProductId() {
        return productId;
    }

    /**
     * @param productId
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * 获取总库存
     *
     * @return total_inventory - 总库存
     */
    public Integer getTotalInventory() {
        return totalInventory;
    }

    /**
     * 设置总库存
     *
     * @param totalInventory 总库存
     */
    public void setTotalInventory(Integer totalInventory) {
        this.totalInventory = totalInventory;
    }

    /**
     * 获取锁定库存
     *
     * @return lock_inventory - 锁定库存
     */
    public Integer getLockInventory() {
        return lockInventory;
    }

    /**
     * 设置锁定库存
     *
     * @param lockInventory 锁定库存
     */
    public void setLockInventory(Integer lockInventory) {
        this.lockInventory = lockInventory;
    }
}