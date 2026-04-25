package org.hospital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {
    "org.hospital.common",
    "org.hospital.admin",
    "org.hospital.qmg",
    "org.hospital.neuroimmune",
    "org.hospital.ocr"
})
@EnableCaching
@EnableScheduling
public class HospitalApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalApplication.class, args);
    }
}