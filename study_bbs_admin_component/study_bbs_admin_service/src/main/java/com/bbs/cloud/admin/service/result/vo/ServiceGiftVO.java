package com.bbs.cloud.admin.service.result.vo;

import com.bbs.cloud.admin.common.enums.gift.GiftEnum;

import java.util.List;
import java.util.Map;

/**
 * ProjectName: com.bbs.cloud.admin.service.result.vo
 *
 * @author: 10270
 * description: 服务信息结果之礼物服务
 */
public class ServiceGiftVO {
    private Integer serviceType;
    private List<GiftVO> giftList;
    //直接使用礼物枚举类会出问题: 显示不完全只有键和礼物的name
    //private Map<Integer, GiftEnum> giftDescMap;
    private Map<Integer, Map<String, String>> giftDescMap;

    public Integer getServiceType() {
        return serviceType;
    }

    public void setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
    }

    public List<GiftVO> getGiftList() {
        return giftList;
    }

    public void setGiftList(List<GiftVO> giftList) {
        this.giftList = giftList;
    }

    public Map<Integer, Map<String, String>> getGiftDescMap() {
        return giftDescMap;
    }

    public void setGiftDescMap(Map<Integer, Map<String, String>> giftDescMap) {
        this.giftDescMap = giftDescMap;
    }
}
