package com.bbs.cloud.admin.service.controller;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.service.param.OrderMessageParam;
import com.bbs.cloud.admin.service.result.ServiceInfoResult;
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

    /** 可以学习返回数据的结构层次定义--开闭原则,低耦合,高内聚(代码可以增添而不用修改原有的代码,例如添加用户\文章\评论)
     * 接⼝功能：查看服务信息ServiceInfoResult升级表示: Map的key: serviceXXXVO; value: 用json字符串表示    serviceType服务类型
     *     礼物服务serviceGiftVO: serviceType(100)  礼物表清单giftList(service_gift表List类型)  10种礼物信息giftDescMap(Integer+Map<String, String>礼物枚举表示[不用GiftEnum])
     *     金币服务serviceGoldVO: serviceType(101)  金币表service_gold(service_gold表类型)
     *     服务类型serviceType: 100礼物服务  101充值服务(即充值金币)------Integer+String的Map表示
     * 接⼝路径：http:127.0.0.1:8091/service/query
     */
    @GetMapping("/query")
    public HttpResult<ServiceInfoResult>  queryService(){
        return serviceService.queryService();
    }
}
