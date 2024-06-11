package com.bbs.cloud.admin.service.message.handler;

import com.bbs.cloud.admin.common.contant.RabbitContant;
import com.bbs.cloud.admin.common.contant.RedisContant;
import com.bbs.cloud.admin.common.enums.gift.GiftEnum;
import com.bbs.cloud.admin.common.error.CommonExceptionEnum;
import com.bbs.cloud.admin.common.feigh.client.ActivityFeighClient;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.CommonUtil;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.common.util.RedisLockHelper;
import com.bbs.cloud.admin.service.contant.ServiceContant;
import com.bbs.cloud.admin.service.dto.ServiceGiftDTO;
import com.bbs.cloud.admin.service.enums.ServiceTypeEnum;
import com.bbs.cloud.admin.service.mapper.ServiceGiftMapper;
import com.bbs.cloud.admin.service.message.MessageHandler;
import com.bbs.cloud.admin.service.message.dto.OrderMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ProjectName: com.bbs.cloud.admin.service.message.handler
 *
 * @author: 10270
 * description:
 */
@Component
public class GiftOrderMessageHandler implements MessageHandler {
    //日志：SpringBoot提供的一个机制，通过日志可以发现执行到哪一步，是否符合预期
    final static Logger logger = LoggerFactory.getLogger(GiftOrderMessageHandler.class);
    @Autowired
    private ServiceGiftMapper serviceGiftMapper;//操作service_gift礼物表

    @Autowired
    private ActivityFeighClient activityFeighClient;//远程调用活动服务接口

    @Autowired
    private RedisLockHelper redisLockHelper;//redis分布式锁的的添加\释放

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 对于礼物订单的处理：操作的table是service_gift。对每种类型的礼物默认加100
     * 添加redis分布式锁解决同时下单、创建活动出现问题----锁要和福袋那边一致
     */
    @Override
    public void handler(OrderMessageDTO orderMessageDTO) {
        logger.info("开始处理礼物服务订单, message: {}", JsonUtils.objectToJson(orderMessageDTO));
        String key = RedisContant.BBS_CLOUD_LOCK_GIFT_KEY;//本系统默认只有一个租户，如果是多个租户还需要加上租户id

        try{
            if(redisLockHelper.lock(key, CommonUtil.createUUID(), 10000L)){
                Map<Integer, GiftEnum> giftsMap = GiftEnum.getGiftsMap();//礼物订单的礼物类型
                for(GiftEnum giftEnum : giftsMap.values()){//遍历礼物枚举类
                    Integer giftType = giftEnum.getGiftType();//得到礼物类型
                    //根据某种礼物类型查看该类型的礼物在数据库中是否存在
                    ServiceGiftDTO serviceGiftDTO = serviceGiftMapper.queryGiftDTO(giftType);
                    //该类型礼物为空说明: 1、这个租户之前没有买过这个类型的礼物；2、该类型的礼物已经被消耗完了(包装福袋)
                    if(serviceGiftDTO == null){
                        serviceGiftDTO = new ServiceGiftDTO();
                        serviceGiftDTO.setId(CommonUtil.createUUID());
                        serviceGiftDTO.setGiftType(giftType);
                        serviceGiftDTO.setAmount(ServiceContant.DEFAULT_SERVICE_GIFT_AMOUNT);//默认新订单消息的礼物为100
                        serviceGiftDTO.setUnusedAmount(ServiceContant.DEFAULT_SERVICE_UNUSED_GIFT_AMOUNT);
                        serviceGiftDTO.setUsedAmount(ServiceContant.DEFAULT_SERVICE_USED_GIFT_AMOUNT);
                        serviceGiftMapper.insertGiftDTO(serviceGiftDTO);//插入新的礼物订单
                    }else{
                        /**
                         * 去查询活动使用礼物的情况，来进行礼物库存更新
                         * 该类型礼物已存在: 1、远程调用活动服务的获取待领取/正在使用状态的关于该类型礼物的数量;(lucky_bag)
                         * 因为两个活动(福袋活动\积分兑换福袋活动)共用这一张表, 只涉及福袋表lucky_bag
                         * 根据礼物类别去福袋活动表lucky_bag中查询已使用的礼物总数（礼物状态: normal \ geted 的礼物总数-涉及的总行数count(*)）
                         *               2、统计礼物总数里、已使用、未使用
                         */
                        logger.info("开始处理礼物服务订单-整理库存, 礼物信息: {}", JsonUtils.objectToJson(serviceGiftDTO));
                        //远程调用活动服务查询待领取/正在使用状态的该类型的礼物总数
                        HttpResult<Integer> result = activityFeighClient.queryServiceGiftTotal(giftType);
                        if(result == null || !result.getCode().equals(CommonExceptionEnum.SUCCESS.getCode()) || result.getData() == null){
                            logger.info("远程获取活动组件礼物的使用情况, 发生异常, 礼物信息: {}", JsonUtils.objectToJson(serviceGiftDTO));
                            result.setData(0);
                        }
                        Integer usedActivityGiftAmount = result.getData();//lucky_bag种关于礼物被使用的情况的数量
                        Integer amount = serviceGiftDTO.getAmount() + ServiceContant.DEFAULT_SERVICE_GIFT_AMOUNT;
                        Integer usedAmount = usedActivityGiftAmount;//已使用
                        Integer unusedAmount = amount - usedAmount;//未使用

                        serviceGiftDTO.setAmount(amount);
                        serviceGiftDTO.setUsedAmount(usedAmount);
                        serviceGiftDTO.setUnusedAmount(unusedAmount);
                        //更新service_gift表的礼物库存状态
                        serviceGiftMapper.updateGiftDTO(serviceGiftDTO);
                    }
                }
            }else{//重新通过rabbitmq发送消息message, 因为没获取到锁, 之前监听到的数据根本没处理
                //将订单消息对象转换为JSON格式，并将其发送到RabbitMQ中预定义的交换机和路由键。消息会被存储在与该路由键绑定的队列中，等待消费者进行处理。
                rabbitTemplate.convertAndSend(RabbitContant.SERVICE_EXCHANGE_NAME,RabbitContant.SERVICE_ROUTING_KEY, JsonUtils.objectToJson(orderMessageDTO));
            }

        }catch (Exception e){
            logger.error("开始处理礼物服务订单，发生异常，message{}", JsonUtils.objectToJson(orderMessageDTO));
            e.printStackTrace();
        }finally {
            redisLockHelper.releaseLock(key);//释放锁
        }
    }

    @Override
    public Integer getServiceType() {
        return ServiceTypeEnum.GIFT_MESSAGE.getType();
    }
}
