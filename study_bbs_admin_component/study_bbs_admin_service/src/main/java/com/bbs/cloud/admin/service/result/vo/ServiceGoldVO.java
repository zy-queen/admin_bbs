package com.bbs.cloud.admin.service.result.vo;

/**
 * ProjectName: com.bbs.cloud.admin.service.result.vo
 *
 * @author: 10270
 * description: 服务信息结果之金币服务
 */
public class ServiceGoldVO {
    private Integer serviceType;
    private GoldVO goldVO;

    public Integer getServiceType() {
        return serviceType;
    }

    public void setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
    }

    public GoldVO getGoldVO() {
        return goldVO;
    }

    public void setGoldVO(GoldVO goldVO) {
        this.goldVO = goldVO;
    }
}
