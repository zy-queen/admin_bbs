package com.bbs.cloud.admin.service.message;

import com.bbs.cloud.admin.service.message.dto.OrderMessageDTO;

/**
 * 控制器
 * 具体哪个订单消息、消息具体的处理
 */
public interface MessageHandler {
    void handler(OrderMessageDTO orderMessageDTO);

    Integer getServiceType();
}
