package org.hospital.qmg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("org.hospital.qmg.mapper")
@EnableScheduling
public class QmgApplication {

    public static void main(String[] args) {
        SpringApplication.run(QmgApplication.class, args);
    }

}
