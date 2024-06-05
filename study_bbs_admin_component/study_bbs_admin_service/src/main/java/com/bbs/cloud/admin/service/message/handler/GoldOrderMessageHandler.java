package com.bbs.cloud.admin.service.message.handler;

import com.bbs.cloud.admin.common.util.CommonUtil;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.service.contant.ServiceContant;
import com.bbs.cloud.admin.service.dto.ServiceGoldDTO;
import com.bbs.cloud.admin.service.enums.ServiceTypeEnum;
import com.bbs.cloud.admin.service.mapper.ServiceGoldMapper;
import com.bbs.cloud.admin.service.message.MessageHandler;
import com.bbs.cloud.admin.service.message.dto.OrderMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    /**
     * 对于金币订单的处理
     */
    @Override
    public void handler(OrderMessageDTO orderMessageDTO) {
        logger.info("开始处理充值服务订单：{}", JsonUtils.objectToJson(orderMessageDTO));
        try{
            //金币没有类型之分，这里无需遍历
            ServiceGoldDTO serviceGoldDTO = serviceGoldMapper.queryServiceGoldDTO(ServiceContant.SERVICE_GOLD_NAME);
            //金币如果为空，说明这个租户没有充值过，这里需要充值
            if(serviceGoldDTO == null){
                serviceGoldDTO.setId(CommonUtil.createUUID());
                serviceGoldDTO.setName(ServiceContant.SERVICE_GOLD_NAME);//金币就这一个默认的名字
                serviceGoldDTO.setGold(ServiceContant.DEFAULT_SERVICE_GOLD);//默认的金币额度：10000
                serviceGoldDTO.setUnusedGold(ServiceContant.DEFAULT_SERVICE_UNUSED_GOLD);
                serviceGoldDTO.setUsedGold(ServiceContant.DEFAULT_SERVICE_USED_GOLD);
                serviceGoldMapper.insertServiceGold(serviceGoldDTO);
            }else{
                /**
                 * TODO 金币已存在的情况下，获取过去活动中金币的使用情况，进行金币库存更新
                 */
            }
        }catch (Exception e){
            logger.error("开始处理充值服务订单，发生异常，message{}", JsonUtils.objectToJson(orderMessageDTO));
            e.printStackTrace();
        }

    }

    @Override
    public Integer getServiceType() {
        return ServiceTypeEnum.RECHARGE_MESSAGE.getType();
    }
}
