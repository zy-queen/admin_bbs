package com.bbs.cloud.admin.service.message.handler;

import com.bbs.cloud.admin.common.contant.RabbitContant;
import com.bbs.cloud.admin.common.contant.RedisContant;
import com.bbs.cloud.admin.common.error.CommonExceptionEnum;
import com.bbs.cloud.admin.common.feigh.client.ActivityFeighClient;
import com.bbs.cloud.admin.common.feigh.client.ServiceFeighClient;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.CommonUtil;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.common.util.RedisLockHelper;
import com.bbs.cloud.admin.service.contant.ServiceContant;
import com.bbs.cloud.admin.service.dto.ServiceGoldDTO;
import com.bbs.cloud.admin.service.enums.ServiceTypeEnum;
import com.bbs.cloud.admin.service.mapper.ServiceGoldMapper;
import com.bbs.cloud.admin.service.message.MessageHandler;
import com.bbs.cloud.admin.service.message.dto.OrderMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ProjectName: com.bbs.cloud.admin.service.message.handler
 *
 * @author: 10270
 * description:
 */
@Component
public class GoldOrderMessageHandler implements MessageHandler {
    //日志：SpringBoot提供的一个机制，通过日志可以发现执行到哪一步，是否符合预期
    final static Logger logger = LoggerFactory.getLogger(GoldOrderMessageHandler.class);
    @Autowired
    private ServiceGoldMapper serviceGoldMapper;
    @Autowired
    private ActivityFeighClient activityFeighClient;//远程调用接口的客户端————需要远程调用活动组件暴露的接口

    @Autowired
    private RedisLockHelper redisLockHelper;//redis分布式锁的的添加\释放

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 对于金币订单的处理：操作的table是service_gold
     * 添加redis分布式锁解决同时下单、创建活动出现问题----锁要和红包那边一致
     */
    @Override
    public void handler(OrderMessageDTO orderMessageDTO) {
        logger.info("开始处理充值服务订单：{}", JsonUtils.objectToJson(orderMessageDTO));
        String key = RedisContant.BBS_CLOUD_LOCK_GOLD_KEY;//本系统默认只有一个租户，如果是多个租户还需要加上租户id

        try{
            if(redisLockHelper.lock(key, CommonUtil.createUUID(), 10000L)){//redis分布式锁设置3s失效
                //金币没有类型之分，这里无需遍历
                ServiceGoldDTO serviceGoldDTO = serviceGoldMapper.queryServiceGoldDTO(ServiceContant.SERVICE_GOLD_NAME);
                //金币如果为空，说明这个租户没有充值过，这里需要充值
                if(serviceGoldDTO == null){
                    serviceGoldDTO = new ServiceGoldDTO();
                    String id = CommonUtil.createUUID();
                    serviceGoldDTO.setId(id);
                    String name = ServiceContant.SERVICE_GOLD_NAME;
                    serviceGoldDTO.setName(name);//金币就这一个默认的名字
                    serviceGoldDTO.setGold(ServiceContant.DEFAULT_SERVICE_GOLD);//默认的金币额度：10000
                    serviceGoldDTO.setUnusedGold(ServiceContant.DEFAULT_SERVICE_UNUSED_GOLD);
                    serviceGoldDTO.setUsedGold(ServiceContant.DEFAULT_SERVICE_USED_GOLD);
                    serviceGoldMapper.insertServiceGold(serviceGoldDTO);
                }else{
                    /**
                     * 金币已存在: 1、远程调用活动服务的获取待领取/正在使用状态的关于该类型金币的数量;(两张表red_packet\activity_gold)
                     * 因为有两种活动(红包活动\积分兑换金币活动)都会消耗金币
                     *     red_packet表中状态为normal \ geted的quota总额;
                     *     activity_gold表中状态为del的used_quota已使用额度 \ 状态为normal的quota总额
                     *
                     *  2、统计金币总数量、已使用、未使用
                     */
                    logger.info("开始处理金币服务订单-整理库存, 金币信息: {}", JsonUtils.objectToJson(serviceGoldDTO));
                    //远程调用活动服务查询活动中金币的使用情况: red_packet表中状态为normal \ geted的quota总额; activity_gold表中状态为del的used_quota已使用额度 \ 状态为normal的quota总额
                    HttpResult<Integer> result = activityFeighClient.queryUsedGold();
                    logger.info("开始处理金币服务订单-整理库存--远程调用---获取活动中待使用和已使用的金币总额, result: {}", JsonUtils.objectToJson(result));
                    if(result == null || !result.getCode().equals(CommonExceptionEnum.SUCCESS.getCode()) || result.getData() == null){
                        logger.info("开始处理金币服务订单-整理库存--远程调用---获取活动中待使用和已使用的金币总额发生异常, result: {}", JsonUtils.objectToJson(result));
                        result.setData(0);
                    }
                    Integer usedGold = result.getData();//red_packet中关于金币被使用的情况的数量
                    serviceGoldDTO.setUsedGold(usedGold);
                    serviceGoldDTO.setGold(serviceGoldDTO.getGold() + ServiceContant.DEFAULT_SERVICE_GOLD);//红包金币总额
                    serviceGoldDTO.setUnusedGold(serviceGoldDTO.getGold() - serviceGoldDTO.getUsedGold());
                    //更新red_packet表的金币库存状态
                    serviceGoldMapper.updateServiceGold(serviceGoldDTO);

                }
            }else{//重新通过rabbitmq发送消息message, 因为没获取到锁, 之前监听到的数据根本没处理
                rabbitTemplate.convertAndSend(RabbitContant.SERVICE_EXCHANGE_NAME,RabbitContant.SERVICE_ROUTING_KEY, JsonUtils.objectToJson(orderMessageDTO));
            }
        }catch (Exception e){
            logger.error("开始处理充值服务订单，发生异常，message{}", JsonUtils.objectToJson(orderMessageDTO));
            e.printStackTrace();
        }finally {
            redisLockHelper.releaseLock(key);//释放锁
        }
    }

    @Override
    public Integer getServiceType() {
        return ServiceTypeEnum.RECHARGE_MESSAGE.getType();
    }
}
