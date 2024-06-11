package com.bbs.cloud.admin.service.service;

import com.bbs.cloud.admin.common.contant.RabbitContant;
import com.bbs.cloud.admin.common.enums.gift.GiftEnum;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.service.contant.ServiceContant;
import com.bbs.cloud.admin.service.controller.ServiceController;
import com.bbs.cloud.admin.service.dto.ServiceGiftDTO;
import com.bbs.cloud.admin.service.dto.ServiceGoldDTO;
import com.bbs.cloud.admin.service.enums.ServiceTypeEnum;
import com.bbs.cloud.admin.service.mapper.ServiceGiftMapper;
import com.bbs.cloud.admin.service.mapper.ServiceGoldMapper;
import com.bbs.cloud.admin.service.param.OrderMessageParam;
import com.bbs.cloud.admin.service.result.ServiceInfoResult;
import com.bbs.cloud.admin.service.result.vo.GiftVO;
import com.bbs.cloud.admin.service.result.vo.GoldVO;
import com.bbs.cloud.admin.service.result.vo.ServiceGiftVO;
import com.bbs.cloud.admin.service.result.vo.ServiceGoldVO;
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
    private ServiceGiftMapper serviceGiftMapper;//操作表service_gift

    @Autowired
    private ServiceGoldMapper serviceGoldMapper;//操作表service_gold

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
     * 查询服务信息
     * 可以学习返回数据的结构层次定义--开闭原则,低耦合,高内聚
     */
    public HttpResult<ServiceInfoResult> queryService() {
        logger.info("查询服务信息");
        ServiceInfoResult serviceInfoResult = new ServiceInfoResult();
        serviceInfoResult.setServiceType(ServiceTypeEnum.getServiceTypeMap());

        logger.info("查询服务信息-----------获取礼物服务信息");
        ServiceGiftVO serviceGiftVO = new ServiceGiftVO();
        serviceGiftVO.setServiceType(ServiceTypeEnum.GIFT_MESSAGE.getType());
        List<ServiceGiftDTO> serviceGiftDTOS = serviceGiftMapper.queryGiftDTOList();
        List<GiftVO> giftVOS = new ArrayList<>();
        serviceGiftDTOS.forEach(item -> {
            GiftVO giftVO = new GiftVO();
            BeanUtils.copyProperties(item, giftVO);
            giftVOS.add(giftVO);
        });
        serviceGiftVO.setGiftList(giftVOS);
        serviceGiftVO.setGiftDescMap(GiftEnum.getGiftsJsonMap());
        serviceInfoResult.setServiceGiftVO(serviceGiftVO);

        logger.info("查询服务信息-----------获取金币服务信息");
        ServiceGoldVO serviceGoldVO = new ServiceGoldVO();
        serviceGoldVO.setServiceType(ServiceTypeEnum.RECHARGE_MESSAGE.getType());
        ServiceGoldDTO serviceGoldDTO = serviceGoldMapper.queryServiceGoldDTO(ServiceContant.SERVICE_GOLD_NAME);
        GoldVO goldVO = new GoldVO();
        BeanUtils.copyProperties(serviceGoldDTO, goldVO);
        serviceGoldVO.setGoldVO(goldVO);
        serviceInfoResult.setServiceGoldVO(serviceGoldVO);

        return new HttpResult<>(serviceInfoResult);

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

    public HttpResult<Integer> queryServiceGold() {
        logger.info("远程调用----start----获取服务组件未使用的金币额度");
        //金币只有一种, 这一点与10种类型的礼物不同
        ServiceGoldDTO serviceGoldDTO = serviceGoldMapper.queryServiceGoldDTO(ServiceContant.SERVICE_GOLD_NAME);
        if(serviceGoldDTO == null){//友好一些, 如果没有初始化,返回未使用额度为0
            return new HttpResult<>(ServiceContant.DEFAULT_SERVICE_INITIAL_GOLD);
        }
        logger.info("远程调用----获取服务组件未使用的金币额度, serviceGoldDTO: {}", serviceGoldDTO);
        return new HttpResult<>(serviceGoldDTO.getUnusedGold());//得到未使用的金币额度
    }

    /**
     * 更新金币表service_gold中的已使用的金币数量
     * @param usedGold
     * @return
     */
    public HttpResult updateServiceGold(Integer usedGold) {
        logger.info("远程调用----start----更新服务组件已使用金币的额度, usedGold: {}", usedGold);
        //金币只有一种, 这一点与10种类型的礼物不同
        ServiceGoldDTO serviceGoldDTO = serviceGoldMapper.queryServiceGoldDTO(ServiceContant.SERVICE_GOLD_NAME);
        serviceGoldDTO.setUsedGold(serviceGoldDTO.getUsedGold() + usedGold);//service_gold表中已使用额度增加
        serviceGoldDTO.setUnusedGold(serviceGoldDTO.getUnusedGold() - usedGold);//service_gold表中未使用额度减少
        serviceGoldMapper.updateServiceGold(serviceGoldDTO);
        logger.info("远程调用----更新服务组件已使用金币的额度");

        return HttpResult.ok();//更新成功返回ok
    }

}
