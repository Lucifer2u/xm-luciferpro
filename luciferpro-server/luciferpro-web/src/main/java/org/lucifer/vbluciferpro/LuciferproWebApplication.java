package org.lucifer.vbluciferpro;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages="org.lucifer.vbluciferpro.mapper")
public class LuciferproWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuciferproWebApplication.class, args);
    }

}
