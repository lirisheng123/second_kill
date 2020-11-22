package com.lirisheng.my_second_kill.kafka;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lirisheng.my_second_kill.pojo.Stock;
import com.lirisheng.my_second_kill.service.api.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class ComsumerListenTest {
    private Gson gson = new GsonBuilder().create();

    //进行SECONDS-KILL-TOPIC该主题消息队列的消息的消费
    @KafkaListener(topics = "SECONDS-KILL-TOPIC")
    public void listen(ConsumerRecord<String, String> record) throws Exception {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        // Object -> String
        String message = (String) kafkaMessage.get();
        // 反序列化
        Map<String,String> stock = gson.fromJson((String) message,  Map.class);
    }
}
