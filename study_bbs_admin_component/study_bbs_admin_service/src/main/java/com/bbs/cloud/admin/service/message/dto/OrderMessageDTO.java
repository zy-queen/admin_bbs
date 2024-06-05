package com.bbs.cloud.admin.service.message.dto;

/**
 * ProjectName: com.bbs.cloud.admin.service.param
 *
 * @author: 10270
 * description:
 */

import java.util.Date;

/**
 * 接收模拟运维管理平台过来的消息，服务组件通过rabbit转发
 * 运维平台发过来的订单实体
 */
public class OrderMessageDTO {
    /**
     *  "id": "test_f74bb2da808f",
     *  "serviceType": 31,
     *  "serviceName": "SERVICE_MONEY_NAME"
     */
    private String id;
    /**
     * {@link com.bbs.cloud.admin.service.enums.ServiceTypeEnum}
     * 请求过来，是100、101，如果两个都不是就需要过滤消息
     */
    private Integer serviceType;
    private String serviceName;

    private Date date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getServiceType() {
        return serviceType;
    }

    public void setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
