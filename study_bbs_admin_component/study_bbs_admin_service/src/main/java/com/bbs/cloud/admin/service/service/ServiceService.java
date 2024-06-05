package com.bbs.cloud.admin.service.service;

import com.bbs.cloud.admin.common.contant.RabbitContant;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.service.controller.ServiceController;
import com.bbs.cloud.admin.service.param.OrderMessageParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * ProjectName: com.bbs.cloud.admin.service.service
 *
 * @author: 10270
 * description:
 */
@Service
public class ServiceService {
    //日志：SpringBoot提供的一个机制，通过日志可以发现执行到哪一步，是否符合预期
    final static Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 按理来说这应该是运维端在发送订单成功消息通过rabbitmq转发的，但是由于没有实现这一端
     * 因此在服务端写了发送消息的代码，然后再messageReceiver中监听接收消息
     * 使用rabbitmq发送消息
     * @param param
     * @return
     */
    public HttpResult sendMessage(OrderMessageParam param){

        logger.info("进入接收订单接口，请求参数{}", JsonUtils.objectToJson(param));
        param.setDate(new Date());
        rabbitTemplate.convertAndSend(RabbitContant.SERVICE_EXCHANGE_NAME,RabbitContant.SERVICE_ROUTING_KEY, JsonUtils.objectToJson(param));
        return HttpResult.ok();
    }
}
