package com.bbs.cloud.admin.controller;

import com.bbs.cloud.admin.common.contant.RabbitContant;
import com.bbs.cloud.admin.common.feigh.client.TestFeighClient;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.JedisUtil;
import com.bbs.cloud.admin.common.util.RedisOperator;
import com.bbs.cloud.admin.service.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProjectName: com.bbs.cloud.admin.controller
 *
 * @author: 10270
 * description:
 */
@RestController
@RequestMapping("test")
public class TestController {
    //日志：SpringBoot提供的一个机制
    final static Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private TestService testService;

    @Autowired
    private JedisUtil jedisUtil;//redis客户端，用于操作redis数据库

    @Autowired
    private RedisOperator redisOperator;//spring自带的redis，用于操作redis数据库

    @Autowired
    private RabbitTemplate rabbitTemplate;//用于生产消息

    @Autowired
    @Qualifier("testFeighClientFallback")//一个远程实现、一个common的TestFeighClientFallback对接口TestClient的实现
    private TestFeighClient testFeighClient;

    @GetMapping("/hello")
    public HttpResult hello(){
        logger.info("进入hello接口");//在控制台打印日志
        //直接返回一个Htpp的success状态的结果：200、ok
        return new HttpResult("hello world! 欢迎大家学习java项目实战课程");
    }

    @GetMapping("/db")
    public HttpResult db(){
        logger.info("进入db测试接口");//在控制台打印日志
        //直接返回一个Htpp的success状态的结果：200、ok
        return testService.queryTest();
    }

    @GetMapping("/redis")
    public HttpResult redis(){
        logger.info("进入redis测试接口");//在控制台打印日志
        jedisUtil.set("test-jedis", "test-jedis-value");
        logger.info("jedis输出{}",jedisUtil.get("test-jedis"));//{}占位符

        redisOperator.set("test-redis-operator", "test-redis-operator-value");
        logger.info("redis输出{}",redisOperator.get("test-redis-operator"));//{}占位符
        //直接返回一个Htpp的success状态的结果：200、ok
        return HttpResult.ok();
    }

    @GetMapping("/mq")
    public HttpResult mq(){
        logger.info("进入mq测试接口");//在控制台打印日志
        rabbitTemplate.convertAndSend(RabbitContant.TEST_EXCHANGE_NAME, RabbitContant.TEST_ROUTING_KEY,"hello world, 欢迎每个同学都来报名");
        return HttpResult.ok();
    }

    @GetMapping("/feigh")
    public HttpResult feigh(){
        logger.info("进入feigh测试接口");//在控制台打印日志
        //直接返回一个Htpp的success状态的结果：200、ok
        return new HttpResult(testFeighClient.testFeigh());
    }
}
