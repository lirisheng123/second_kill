# 1.使用的技术

- springboot框架 ,mybatis持久层,mysql数据库,redis数据库,kafka,lua脚本

# 2.操作手册

- 把该项目clone到本地的idea上,直接执行`pom.xml`

- 配置运行环境

  - mysql

    在mysql数据库中建立一个数据库,并执行在项目的sql目录下的`my_second_kill.sql`,目的是建好表

    在`application.yml`中把mysql的配置改成自己的配置

  - kafka: 在`application.yml`中把kafka的相关配置改成自己的配置

  - redis:这里使用的是Jedis客户端,在`application.yml`的`jedisPoolConfig`中修改成自己的配置

- 启动项目成功后,访问`http://localhost:8080/initDBAndRedis`来完成mysql和redis的数据初始化,默认为商品的id为1的库存初始为5000,并在redis中实现预热

- 使用JMeter来进行压力测试,下载一个JMeter安装包来,解压后,在`JMeter\apache-jmeter-5.3\bin`目录下执行`jmeter.bat`

  JMeter启动成功后,建立一个线程组

  ![image-20201125095812108](E:\typora\image\image-20201125095812108.png)

  ![image-20201125095931446](E:\typora\image\image-20201125095931446.png)

  建立http请求,测试接口

  ![image-20201125100028109](E:\typora\image\image-20201125100028109.png)

  填写好http请求的访问地址后

  建立一个测试结果统计报告

  ![image-20201125100418374](E:\typora\image\image-20201125100418374.png)

  做完上述步骤后,点击绿色按钮执行即可

- 如果你不想自己自定义数据来测试,也可以使用JMeter打开在项目目录的`my_second_kil.jmx`文件,运行即可

  ![image-20201125100857168](E:\typora\image\image-20201125100857168.png)

  

# 3.秒杀系统的分析

- 序言:本节探讨的是如何设计一个秒杀系统,讲到秒杀系统的时候,应该要规定该系统要为多少用户的秒杀做准备,本节是为5000用户的并发的秒杀来进行优化,环境为单机的的系统,在此基础上如何去优化性能,当然也会谈如果高并发,会怎样

- 秒杀系统的特点:瞬时的高并发量,购买人数远远大于库存数量,持续的时间很少

- 设计的原则:系统稳定性,限流,提高读写的并发性(特别是写)

- 如何维持系统的稳定性

  由于在短时间内涌入大量的访问量,可能会导致系统崩溃,这时可以使用消息队列来控制访问流量,让服务器平稳的去处理流量

- 如何限流

  什么是限流呢:就是当库存的数量远远小于购买人的数量的时候,真正的有效访问量是很少的,就是例如5000人访进行秒杀,但只能有50人能买到商品,所以其余人访问服务器相当于无效的,那么可以在进入服务器的时候,对于限流,即过滤掉这些访问,不使其进行业务中,去访问数据库,消耗数据库的资源

  用什么方法去限流呢?

  lua脚本+redis来进行限流

- 提高读写的并发性

  可以提前把商品的列表,库存信息缓存在redis数据库中,来增加读取的速度,减少对数据库的io读写

  异步解耦,就是把下单的流程尽量的细分,只对关键的地方进行同步操作,这样可以保持系统的高并发性,而异步下单就是解决这个问题的

- 商城的秒杀系统的流程

  ![image-20201118213427020](E:\typora\image\image-20201118213427020.png)

- 解释:

  ​    redis秒杀的逻辑是写在业务层,所以如果可以在请求在进入业务层之前,来过滤掉访问量,这样就可以不必执行controller到redis之间的代码,这可以起到节约资源,提高性能的作用,所以限流越早使用效果更明显

  ​    由于在redis中判断库存是否足够,以及勾除缓存中的库存数量是不唯一的,所以可以使用lua脚本来保持其原子性

  ​    对扣除mysql库存数量以及下订单的操作可以进行异步操作,因为这样可以去提高秒杀的并发量以及吞吐量,以及响应,因为对于秒杀后,具体的mysql库存以及订单对于用户来说,并不一定要高响应性的,可以错开秒杀的高峰,再来对这些任务进行处理



- 参考资料

  https://xie.infoq.cn/article/d3e53dfb444c7b5c0017949fd

  http://bittechblog.com/blog/article/12#3

# 4.核心代码

- 使用lua脚本来进行限流

  ```lua
  -- 计数限流
  -- 每次请求都将当前时间，精确到秒作为 key 放入 Redis 中，超时时间设置为 2s， Redis 将该 key 的值进行自增
  -- 当达到阈值时返回错误，表示请求被限流
  -- 写入 Redis 的操作用 Lua 脚本来完成，利用 Redis 的单线程机制可以保证每个 Redis 请求的原子性
  
  -- 资源唯一标志位
  local key = KEYS[1]
  redis.log(redis.LOG_DEBUG,tostring("lirisheng:key")..tostring(key))
  -- 限流大小
  local limit = tonumber(ARGV[1])
  
  -- 获取当前流量大小
  local currentLimit = tonumber(redis.call('get', key) or "0")
  
  if currentLimit + 1 > limit then
      -- 达到限流大小 返回
      return 0;
  else
      -- 没有达到阈值 value + 1
      redis.call("INCRBY", key, 1)
      -- 设置过期时间
      redis.call("EXPIRE", key, 2)
      return currentLimit + 1
  end
  ```

  

- redis中使用lua脚本使得的多步的操作保持原子性,例如:判断redis的库存是否足够步骤以及扣除redis中的库存步骤要保持原子性,如果不保持原子性,在多个用户的访问下,可能在造成修改的数据被覆盖的情况

  ```lua
  -- 进行秒杀
  -- 查询出redis中商品的库存信息,判断其是否足够,如果足够,则进行库存的删减,秒杀成功,否则,秒杀失败
  -- 写入 Redis 的操作用 Lua 脚本来完成，利用 Redis 的单线程机制可以保证每个 Redis 请求的原子性
  
  -- 获取商品的id值
  local key = KEYS[1]
  
  -- 把信息输出到redis日志中,注意:前提是在redis服务器中把日志的级别改为debug,并指定log文件的生成路径,才能看到消息的输出,否则,看不到
  redis.log(redis.LOG_DEBUG,tostring("lirisheng:key")..tostring(key))
  
  -- 获取库存数量
  local count = tonumber(redis.call('get', tostring("stock_count_")..tostring(key) ))
  
  redis.log(redis.LOG_DEBUG,tostring("lirisheng:count")..tostring(count))
  
  if count == 0 then
      -- 库存为0,秒杀失败
      return tostring("")
  else
      -- redis中的count-1
      redis.call("decr", tostring("stock_count_")..tostring(key))
      -- redis中的sale+1
      redis.call("incr", tostring("stock_sale_")..tostring(key))
      -- 返回商品名字
      return  tostring("stock_name_")..tostring(key)
  end
  ```

- 使用kafak异步下单

  异步下单就是在秒杀成功后,在返回响应给用户之前,把下单的任务(扣除mysql中商品的库存信息并创建订单)发送给kafka消息队列处理

  生产者发布消息

  ```java
     
      @Override
      public void createOrderWithRedisAndKafaka(Long id) throws Exception {
  
          //判断redis中是否有库存足够,如果,则扣除
          String result=redisLimit.secondKillWithRedis(id);
          if(result.equals("")){
              throw  new RuntimeException("库存不足,秒杀失败");
          }
          //扣除库存,创建订单,并该任务放在Kafka任务中
          Stock stock = new Stock();
          stock.setName(result);
          stock.setId(id);
          //扣除库存,创建订单,并该任务放在Kafka任务中
          kafkaTemplate.send(kafkaTopic,gson.toJson(stock));
      }
  ```

  消费者消费消息

  ```java
  @Slf4j
  @Component
  public class ConsumerListen {
  
      private Gson gson = new GsonBuilder().create();
  
      @Autowired
      private OrderService orderService;
  
      //消费主题为SECONDS-KILL-TOPIC的消息
      @KafkaListener(topics = "SECONDS-KILL-TOPIC")
      public void listen(ConsumerRecord<String, String> record) throws Exception {
          Optional<?> kafkaMessage = Optional.ofNullable(record.value());
          //获取消息 Object -> String
          String message = (String) kafkaMessage.get();
          // 反序列化
          Stock stock = gson.fromJson((String) message, Stock.class);
          // 创建订单
          orderService.createOrder(stock);
      }
  }
  ```

  