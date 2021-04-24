package com.tiza.leo.gateway;

import com.tiza.leo.gateway.deamon.GatewayDeamon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

/**
 * Author: tz_wl
 * Date: 2021/4/18 7:39
 * Content:
 */


@SpringBootApplication
public class Boot implements ApplicationRunner {

    @Autowired
    GatewayDeamon gatewayDeamon;

    @Autowired
    Environment env;

    public static void main(String[] args) {
        SpringApplication.run(Boot.class,args);
    }


    /**
     *gateway:
        port: 4321
        protocol: tiza1.0
        socketprotocol: udp
     * @param args
     * @throws Exception
     * 按照配置的 socket 类型（TCP  UDP ） 启动
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        String socketPrococol = env.getRequiredProperty("gateway.socketprotocol");
        if("UDP".equalsIgnoreCase(socketPrococol)){
            gatewayDeamon.StartUDP();
        }else if("TCP".equalsIgnoreCase(socketPrococol)){
            gatewayDeamon.StartTCP();
        }
    }
}
