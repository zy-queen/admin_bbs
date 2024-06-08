package com.bbs.cloud.admin.activity.dto;

/**
 * 福袋----可以获取礼物
 * 将礼物包装成福袋————随机指定一个数的礼物生成福袋
 */
public class LuckyBagDTO {

    private String id;

    private String activityId;

    private Integer status;

    /**
     * 福袋中携带的礼物
     */
    private Integer giftType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getGiftType() {
        return giftType;
    }

    public void setGiftType(Integer giftType) {
        this.giftType = giftType;
    }
}
