package com.bbs.cloud.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ProjectName: com.bbs.cloud.admin
 *
 * @author: 10270
 * description:
 */
@SpringBootApplication
//eureka服务节点：表示它是一个具体的服务
@EnableEurekaClient
//支持feign客户端：远程调用
@EnableFeignClients
public class Application {
    public static void main(String[] args){
        SpringApplication.run(Application.class,args);
    }
}
