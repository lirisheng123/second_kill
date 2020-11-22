package com.lirisheng.my_second_kill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class MySecondKillApplication {

    public static void main(String[] args) {
        SpringApplication.run(MySecondKillApplication.class, args);
    }

}
