package com.lirisheng.my_second_kill.service.impl;


import com.lirisheng.my_second_kill.mapper.StockMapper;
import com.lirisheng.my_second_kill.pojo.Stock;
import com.lirisheng.my_second_kill.service.api.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @auther G.Fukang
 * @date 6/7 12:45
 */
@Service(value = "StockService")
@Transactional(rollbackFor = Exception.class)
public class StockServiceImpl implements StockService {

    @Autowired
    private StockMapper stockMapper;

    @Override
    public long getStockCount(int id) {
        Stock stock = stockMapper.selectByPrimaryKey(id);
        return  stock.getCount();
    }

    @Override
    public Stock getStockById(int id) {

        return stockMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateStockById(Stock stock) {

        return stockMapper.updateByPrimaryKeySelective(stock);
    }

    @Override
    public int updateStockByOptimistic(Stock stock) throws Exception{

        return stockMapper.updateByOptimistic(stock);
    }

    @Override
    public int initDBBefore() {

        return stockMapper.initDBBefore();
    }
}
