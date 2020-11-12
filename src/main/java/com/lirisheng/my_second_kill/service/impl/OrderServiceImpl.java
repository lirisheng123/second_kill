package com.lirisheng.my_second_kill.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lirisheng.my_second_kill.StockWithRedis.RedisKeysConstant;
import com.lirisheng.my_second_kill.StockWithRedis.StockWithRedis;
import com.lirisheng.my_second_kill.mapper.StockOrderMapper;
import com.lirisheng.my_second_kill.pojo.Stock;
import com.lirisheng.my_second_kill.pojo.StockOrder;
import com.lirisheng.my_second_kill.service.api.OrderService;
import com.lirisheng.my_second_kill.util.RedisPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @auther G.Fukang
 * @date 6/7 12:44
 */
@Slf4j
@Service(value = "OrderService")
public class OrderServiceImpl implements OrderService {

    @Autowired
    private StockServiceImpl stockService;

    @Autowired
    private StockOrderMapper orderMapper;

    @Autowired
    StockWithRedis stockWithRedis;

    @Autowired
    RedisPoolUtil redisPoolUtil;

//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;

//    @Value("${spring.kafka.template.default-topic}")
//    private String kafkaTopic;

    private Gson gson = new GsonBuilder().create();

    @Override
    public int delOrderDBBefore() {
        return orderMapper.delOrderDBBefore();
    }

//    @Override
//    public int createWrongOrder(int sid) throws Exception {
//        Stock stock = checkStock(sid);
//        saleStock(stock);
//        int res = createOrder(stock);
//
//        return res;
//    }

//    @Override
//    public int createOptimisticOrder(int sid) throws Exception {
//        // 校验库存
//        Stock stock = checkStock(sid);
//        // 乐观锁更新
//        saleStockOptimstic(stock);
//        // 创建订单
//        int id = createOrder(stock);
//
//        return id;
//    }

//    @Override
//    public int createOrderWithLimitAndRedis(int sid) throws Exception {
//        // 校验库存，从 Redis 中获取
//        Stock stock = checkStockWithRedis(sid);

//        // 乐观锁更新库存和Redis
//        saleStockOptimsticWithRedis(stock);
//        // 创建订单
//        int res = createOrder(stock);
//        return res;
//    }

    @Override
    public void createOrderWithLimitAndRedis(int sid) throws Exception {
        //在redis读出缓存,库存是否足够
        Stock stock = checkStockWithRedis(sid);
        //库存足够,使用乐观锁以及创建订单,更新缓存
        saleStockOptimsticWithRedis(stock);
        //更新缓存
    }



//    @Override
//    public void createOrderWithLimitAndRedisAndKafka(int sid) throws Exception {
//        // 校验库存
//        Stock stock = checkStockWithRedis(sid);
//        // 下单请求发送至 kafka，需要序列化 stock
//        kafkaTemplate.send(kafkaTopic, gson.toJson(stock));
//        log.info("消息发送至 Kafka 成功");
//    }

//    @Override
//    public int consumerTopicToCreateOrderWithKafka(Stock stock) throws Exception {
//        // 乐观锁更新库存和 Redis
//        saleStockOptimsticWithRedis(stock);
//        int res = createOrder(stock);
//        if (res == 1) {
//            log.info("Kafka 消费 Topic 创建订单成功");
//        } else {
//            log.info("Kafka 消费 Topic 创建订单失败");
//        }
//
//        return res;
//    }

    /**
     * Redis 校验库存
     *
     * @param sid
     */
//    private Stock checkStockWithRedis(int sid) throws Exception {
//        Integer count = Integer.parseInt(RedisPoolUtil.get(RedisKeysConstant.STOCK_COUNT + sid));
//        Integer sale = Integer.parseInt(RedisPoolUtil.get(RedisKeysConstant.STOCK_SALE + sid));
//        Integer version = Integer.parseInt(RedisPoolUtil.get(RedisKeysConstant.STOCK_VERSION + sid));
//        if (count < 1) {
//            log.info("库存不足");
//            throw new RuntimeException("库存不足 Redis currentCount: " + sale);
//        }
//        Stock stock = new Stock();
//        stock.setId((long)sid);
//        stock.setCount((long)count);
//        stock.setSale((long)sale);
//        stock.setVersion((long)version);
//        // 此处应该是热更新，但是在数据库中只有一个商品，所以直接赋值
//        stock.setName("手机");
//
//        return stock;
//    }
    private Stock checkStockWithRedis(int sid) throws  Exception {
        Integer count = Integer.parseInt(redisPoolUtil.get(RedisKeysConstant.STOCK_COUNT+sid));
        Integer verson = Integer.parseInt(redisPoolUtil.get(RedisKeysConstant.STOCK_VERSION+sid));
        String name=redisPoolUtil.get(RedisKeysConstant.STOCK_NAME+sid);
        if(count<1){
            throw new RuntimeException("库存不足");
        }
        Stock stock = new Stock();
        stock.setId((long)sid);
        stock.setVersion((long)verson);
        stock.setName(name);
        return  stock;
    }

//    /**
//     * 更新数据库和 DB
//     */
//    private void saleStockOptimsticWithRedis(Stock stock) throws Exception {
//        int res = stockService.updateStockByOptimistic(stock);
//        if (res == 0) {
//            throw new RuntimeException("并发更新库存失败");
//        }
//        // 更新 Redis
//        StockWithRedis.updateStockWithRedis(stock);
//    }

    @Transactional(rollbackFor = Exception.class,
                   isolation = Isolation.READ_UNCOMMITTED)
    public void saleStockOptimsticWithRedis(Stock stock) throws Exception{
        //更新数据库
        int res=stockService.updateStockByOptimistic(stock);
        if(res==0){
            throw  new RuntimeException("库存不足,更新失败");
        }
        //创建订单
        createOrder(stock);
        //创建缓存
        stockWithRedis.updateStockWithRedis(stock);

    }


//    /**
//     * 校验库存
//     */
//    private Stock checkStock(int sid) throws Exception {
//        Stock stock = stockService.getStockById(sid);
//        if (stock.getCount() < 1) {
//            throw new RuntimeException("库存不足");
//        }
//        return stock;
//    }

//    /**
//     * 扣库存
//     */
//    private int saleStock(Stock stock) {
//        stock.setSale(stock.getSale() + 1);
//        stock.setCount(stock.getCount() - 1);
//        return stockService.updateStockById(stock);
//    }
//
//    /**
//     * 乐观锁扣库存
//     */
//    private void saleStockOptimstic(Stock stock) throws Exception {
//        int count = stockService.updateStockByOptimistic(stock);
//        if (count == 0) {
//            throw new RuntimeException("并发更新库存失败");
//        }
//    }

    /**
     * 创建订单
     */

    public void createOrder(Stock stock) throws Exception {
        StockOrder order = new StockOrder();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        order.setCreateTime(new Date());
        int res = orderMapper.insertSelective(order);
        if (res == 0) {
            throw new RuntimeException("创建订单失败");
        }
    }
}
