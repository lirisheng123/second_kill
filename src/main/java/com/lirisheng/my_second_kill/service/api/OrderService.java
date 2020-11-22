package com.lirisheng.my_second_kill.service.api;


import com.lirisheng.my_second_kill.pojo.Stock;

/**
 * @auther G.Fukang
 * @date 6/7 12:35
 */
public interface OrderService {

    /**
     * 清空订单表
     */
    int delOrderDBBefore();

    /**
     * 创建订单（存在超卖问题）
     *
     * @param sid
     * @return int
     */
//    int createWrongOrder(int sid) throws Exception;

    /**
     * 数据库乐观锁更新库存，解决超卖问题
     *
     * @param id
     * @return int
     */


    void createOrderWithRedis(Long id) throws Exception;


    void createOrderWithRedisAndKafaka(Long id) throws Exception;

     void createOrder(Stock stock) throws Exception;

}
