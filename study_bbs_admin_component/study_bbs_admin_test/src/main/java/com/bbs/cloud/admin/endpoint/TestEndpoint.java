package com.bbs.cloud.admin.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProjectName: com.bbs.cloud.admin.endpoint
 *
 * @author: 10270
 * description:
 */
@RestController
@RequestMapping("endpoint")
public class TestEndpoint {
    @GetMapping("/feigh1")
    public String testFeigh(){
        System.out.println("进入feigh");
        //测试抛出异常
        //throw new RuntimeException("异常。。。。。。。。。。。。。。");
        return "hello world!";
    }
}
