package com.lirisheng.my_second_kill.mapper;


import com.lirisheng.my_second_kill.pojo.StockOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author : lirisheng
 * @date : 2020/9/15
 **/
@Mapper()
public interface StockOrderMapper {

    /**
     * 插入订单
     * @param order
     * @return
     */
    @Insert("INSERT INTO stock_order (id, sid, name, create_time) VALUES " +
            "(#{id, jdbcType = INTEGER}, #{sid, jdbcType = INTEGER}, #{name, jdbcType = VARCHAR}, #{createTime, jdbcType = TIMESTAMP})")
    int insertSelective(StockOrder order);


    /**
     * 清空订单表
     * 成功为 0，失败为 -1
   */
    @Update("TRUNCATE TABLE stock_order")
    int delOrderDBBefore();
}
