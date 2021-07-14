package org.lucifer.vbluciferpro;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author lucifer
 */
@SpringBootApplication
@MapperScan(basePackages="org.lucifer.vbluciferpro.mapper")
@EnableScheduling
public class LuciferproWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuciferproWebApplication.class, args);
    }

}
