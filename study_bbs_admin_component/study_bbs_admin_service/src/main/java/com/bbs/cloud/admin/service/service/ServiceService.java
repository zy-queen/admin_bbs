package com.bbs.cloud.admin.service.service;

import com.bbs.cloud.admin.common.contant.RabbitContant;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.service.controller.ServiceController;
import com.bbs.cloud.admin.service.dto.ServiceGiftDTO;
import com.bbs.cloud.admin.service.mapper.ServiceGiftMapper;
import com.bbs.cloud.admin.service.param.OrderMessageParam;
import com.bbs.cloud.admin.service.result.GiftVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Autowired
    private ServiceGiftMapper serviceGiftMapper;//操作表serviceGiftMapper

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
        System.out.println(HttpResult.ok());
        return HttpResult.ok();
    }

    /**
     * 操作的是表：service_gift
     * @return
     */
    public HttpResult<Integer> queryServiceGiftTotal() {
        logger.info("远程调用---start---获取服务组件的礼物总数量");
        Integer total = serviceGiftMapper.queryGiftAmount();//这个得到的是unused列的数量总和
        //如果礼物数量为0
        if(total == null){
            total = 0;
        }
        logger.info("远程调用---获取到服务组件的礼物总数量");
        return new HttpResult<>(total);//返回获取到的礼物数量
    }


    /**
     * 操作的是表：service_gift
     * @return
     */
    public HttpResult<String> queryServiceGiftList() {
        logger.info("远程调用----start----获取服务组件的礼物列表");
        List<ServiceGiftDTO> serviceGiftDTOS = serviceGiftMapper.queryGiftDTOList();
        logger.info("远程调用----获取到服务组件的礼物列表, serviceGiftDTOS:{}", JsonUtils.objectToJson(serviceGiftDTOS));
        List<GiftVO> giftVOS = new ArrayList<>();
        serviceGiftDTOS.forEach(item -> {
            GiftVO giftVO = new GiftVO();
            BeanUtils.copyProperties(item, giftVO);
            giftVOS.add(giftVO);
        });
        return new HttpResult(JsonUtils.objectToJson(giftVOS));
    }
    /**
     * 远程调用————服务组件更新它的礼物列表
     * 操作的是表：service_gift
     * @return
     */
    public HttpResult updateServiceGiftList(String data) {
        try {
            logger.info("远程调用---start---更新服务组件的礼物列表，data:{}",data);
            List<ServiceGiftDTO> updateServiceGiftDTOList = JsonUtils.jsonToList(data,ServiceGiftDTO.class);
            serviceGiftMapper.updateGiftDTOList(updateServiceGiftDTOList);//数据库中更新礼物列表
            logger.info("远程调用---更新服务组件的礼物列表成功，data:{}",data);
        }catch (Exception e){
            e.printStackTrace();
            logger.info("远程调用----更新服务组件的礼物列表，发生异常，data:{}",data);
            return HttpResult.fail();
        }
        return HttpResult.ok();

    }
}
