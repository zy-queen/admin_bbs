package com.bbs.cloud.admin.service.controller;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.service.param.OrderMessageParam;
import com.bbs.cloud.admin.service.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * ProjectName: com.bbs.cloud.admin.service.controller
 *
 * @author: 10270
 * description:
 */
@RestController
@RequestMapping("service")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    /**
     * 接口功能:接受模拟运维平台订单消息并通过rabbit转发接口:id、serviceType、serviceName
     * 路径:http:127.0.0.1:8091/service/send/message
     * 理解：模拟运维平台告诉服务组件订单消息（即模拟订单成功的消息发送订单信息过来），服务组件通过rabbit转发接口
     * 接收运维平台发过来的订单消息，模拟将该消息通过rabbitmq发送消息，并接收订单消息（监听消息就在message那定义）
     */
    @PostMapping("/send/message")
    public HttpResult sendMessage(@RequestBody OrderMessageParam param){//接收请求参数

        return serviceService.sendMessage(param);
//        System.out.println("进入controller接口");
//        return "测试进入接口";
    }

    /**
     * 接⼝功能：查看服务信息
     * 接⼝路径：http:127.0.0.1:8091/service/query
     */
    @GetMapping("/service/query")
    public HttpResult queryService(){
        return null;
    }
}
