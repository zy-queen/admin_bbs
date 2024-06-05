package com.bbs.cloud.admin.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * ProjectName: com.bbs.cloud.admin.server
 *
 * @author: 10270
 * description:
 */
@SpringBootApplication
//表示该服务是一个注册中心，而client表示是一个节点
@EnableEurekaServer
public class Application {
    public static void main(String[] args){
        SpringApplication.run(Application.class,args);

    }
}
