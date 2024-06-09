package com.bbs.cloud.admin.service.message.handler;

import com.bbs.cloud.admin.common.enums.gift.GiftEnum;
import com.bbs.cloud.admin.common.error.CommonExceptionEnum;
import com.bbs.cloud.admin.common.feigh.client.ActivityFeighClient;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.CommonUtil;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.service.contant.ServiceContant;
import com.bbs.cloud.admin.service.dto.ServiceGiftDTO;
import com.bbs.cloud.admin.service.enums.ServiceTypeEnum;
import com.bbs.cloud.admin.service.mapper.ServiceGiftMapper;
import com.bbs.cloud.admin.service.message.MessageHandler;
import com.bbs.cloud.admin.service.message.dto.OrderMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    /**
     * 对于礼物订单的处理：操作的table是service_gift。对每种类型的礼物默认加100
     */
    @Override
    public void handler(OrderMessageDTO orderMessageDTO) {
        logger.info("开始处理礼物服务订单, message: {}", JsonUtils.objectToJson(orderMessageDTO));
        try{
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
                     * TODO 去查询活动使用礼物的情况，来进行礼物库存更新
                     * 该类型礼物已存在: 1、远程调用活动服务的获取待领取/正在使用状态的关于该类型礼物的数量;
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
        }catch (Exception e){
            logger.error("开始处理礼物服务订单，发生异常，message{}", JsonUtils.objectToJson(orderMessageDTO));
            e.printStackTrace();
        }

    }

    @Override
    public Integer getServiceType() {
        return ServiceTypeEnum.GIFT_MESSAGE.getType();
    }
}
