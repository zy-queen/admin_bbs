package com.bbs.cloud.admin.service.message.handler;

import com.bbs.cloud.admin.common.enums.gift.GiftEnum;
import com.bbs.cloud.admin.common.util.CommonUtil;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.service.contant.ServiceContant;
import com.bbs.cloud.admin.service.controller.ServiceController;
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
    private ServiceGiftMapper serviceGiftMapper;

    /**
     * 对于礼物订单的处理：操作的table是service_gift
     */
    @Override
    public void handler(OrderMessageDTO orderMessageDTO) {
        logger.info("开始处理礼物服务订单：{}", JsonUtils.objectToJson(orderMessageDTO));
        try{
            Map<Integer, GiftEnum> giftsMap = GiftEnum.getGiftsMap();//礼物订单的礼物类型
            for(GiftEnum giftEnum : giftsMap.values()){//遍历礼物枚举类
                Integer giftType = giftEnum.getGiftType();//得到礼物类型
                //根据礼物类型查看礼物在数据库中是否存在
                ServiceGiftDTO serviceGiftDTO = serviceGiftMapper.queryGiftDTO(giftType);
                //如果为空说明这个租户之前没有买过这个类型的礼物
                if(serviceGiftDTO == null){
                    serviceGiftDTO = new ServiceGiftDTO();//如果是新租户，就需要新创建一个礼物DTO
                    serviceGiftDTO.setId(CommonUtil.createUUID());
                    serviceGiftDTO.setGiftType(giftType);
                    serviceGiftDTO.setAmount(ServiceContant.DEFAULT_SERVICE_GIFT_AMOUNT);//默认新订单消息的礼物为100
                    serviceGiftDTO.setUnusedAmount(ServiceContant.DEFAULT_SERVICE_UNUSED_GIFT_AMOUNT);
                    serviceGiftDTO.setUsedAmount(ServiceContant.DEFAULT_SERVICE_USED_GIFT_AMOUNT);
                    serviceGiftMapper.insertGiftDTO(serviceGiftDTO);//插入新的礼物订单
                }else{//如果不为空，说明存在被其他活动使用的情况
                    /**
                     * TODO 去查询活动使用礼物的情况，来进行礼物库存更新
                     */
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
