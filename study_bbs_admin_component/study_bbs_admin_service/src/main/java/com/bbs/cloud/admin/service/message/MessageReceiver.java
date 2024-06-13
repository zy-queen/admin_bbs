package com.bbs.cloud.admin.service.message;

import com.bbs.cloud.admin.common.contant.RabbitContant;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.service.controller.ServiceController;
import com.bbs.cloud.admin.service.message.dto.OrderMessageDTO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * ProjectName: com.bbs.cloud.admin.service.message
 *
 * @author: 10270
 * description:监听rabbitmq消息（运维平台发送订单信息过来，这边监听接收）
 */
@Component
public class MessageReceiver {
    //日志：SpringBoot提供的一个机制，通过日志可以发现执行到哪一步，是否符合预期
    final static Logger logger = LoggerFactory.getLogger(ServiceController.class);
    @Autowired
    private List<MessageHandler> messageHandlers;//加载该接口的所有实现类

    /**
     * 绑定队列，因为在配置类中已经配置了
     * 监听订单成功消息
     */
    @RabbitListener(queues = RabbitContant.SERVICE_QUEUE_NAME)
    public void receiver(String message){
        //System.out.println(message);
        logger.info("接收到订单成功消息：{}", message);
        if(StringUtils.isEmpty(message)){
            logger.info("接收订单成功消息，消息为空{}", message);
            return;
        }
        OrderMessageDTO orderMessageDTO;
        try {//json转obj
            orderMessageDTO = JsonUtils.jsonToPojo(message,OrderMessageDTO.class);
            if(orderMessageDTO == null){
                logger.info("接收订单成功消息，消息转换为空{}", message);
                return;
            }
        }catch (Exception e){
            logger.info("接收订单成功消息，消息转换异常{}", message);
            e.printStackTrace();
            return;
        }
        Integer serviceType = orderMessageDTO.getServiceType();
        /**
         * 要与活动组件交互，获取被领取和在使用的金币/礼物，为了统计金币/礼物后，最后更新到库存中
         * 过滤器：筛选出相关服务的订单
         */
        Optional<MessageHandler> handlerOptional = messageHandlers.stream()
                //对当前元素item调用getServiceType()方法
                .filter(item -> item.getServiceType().equals(serviceType))//只有相等的元素才会被保留在流中
                .findFirst();
        handlerOptional.ifPresent(handler -> handler.handler(orderMessageDTO));
        if (!handlerOptional.isPresent()) {
            // 日志记录或者异常处理
            System.out.println("No handler found for serviceType: " + serviceType);
        }

//        messageHandlers.stream()
//                .filter(item -> item.getServiceType().equals(serviceType))
//                .findFirst()
//                .ifPresent(handler -> handler.handler(orderMessageDTO));
    }
}
