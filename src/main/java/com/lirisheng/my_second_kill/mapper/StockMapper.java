package com.lirisheng.my_second_kill.mapper;


import com.lirisheng.my_second_kill.pojo.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author : lirisheng
 * @date : 2020/9/15
 **/
@Mapper
public interface StockMapper {

    /**
     * 初始化 DB
     */
    @Update("UPDATE stock SET count = #{count}, sale = 0 WHERE  id=#{id} ")
    int initDatabaseById(@Param("id") Long id,@Param("count")Long  count);


    /**
     * 通过id查询商品的库存信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM stock WHERE id = #{id}")
    Stock selectStockById(@Param("id") int id);

    /**
     * 秒杀成功,扣减库存
     * @param id
     * @return
     */
    @Update("UPDATE stock SET count = count - 1, sale = sale + 1 WHERE id = #{id} ")
    int updateStockById(@Param("id")Long id);
}
