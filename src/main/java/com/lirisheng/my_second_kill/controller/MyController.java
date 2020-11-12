package com.lirisheng.my_second_kill.controller;

import com.lirisheng.my_second_kill.StockWithRedis.StockWithRedis;
import com.lirisheng.my_second_kill.pojo.Stock;
import com.lirisheng.my_second_kill.service.api.OrderService;
import com.lirisheng.my_second_kill.service.api.StockService;
import com.lirisheng.my_second_kill.util.ResBean;
import com.sun.org.apache.xml.internal.resolver.readers.ExtendedXMLCatalogReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class MyController {

    @Resource(name="OrderService")
    OrderService orderService;

    @Resource(name = "StockService")
    StockService stockService;

    @Autowired
    StockWithRedis stockWithRedis;

    @GetMapping("/initDBAndRedis")
    public ResBean initDBAndRedis(){
        //初始化库存里面的商品值,库存为5000,sale =0,version=0,name=tomato;
        try {
            stockService.initDBBefore();
            //清除订单记录
            orderService.delOrderDBBefore();
            //重置redis里面的缓存
            Stock stock = new Stock();
            stock.setCount(5000L);
            stock.setSale(0L);
            stock.setVersion(0L);
            stock.setName("tomato");
            stockWithRedis.resetRedis(stock);
        }catch (Exception e){
            return ResBean.Error("初始化数据库和重置redis缓存失败");
        }

        return ResBean.OK("初始化数据库和重置redis缓存成功");
    }

    @PostMapping("/orderWithOptAndRedis")
    public ResBean orderWithOptAndRedis(@RequestParam("sid") Integer sid){
        try{
            orderService.createOrderWithLimitAndRedis(sid);
        }catch (Exception e){
            return ResBean.Error("订单下达失败");
        }
        return ResBean.OK("订单下单成功");
    }
}
