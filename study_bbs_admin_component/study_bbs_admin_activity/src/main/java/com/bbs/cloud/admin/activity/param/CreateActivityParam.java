package com.bbs.cloud.admin.activity.param;

/**
 * ProjectName: com.bbs.cloud.admin.activity.param
 *
 * @author: 10270
 * description:接收创建活动的请求参数体
 * param用处：接收请求参数、通过mq发送/接收消息，监听消息就在message定义
 */
public class CreateActivityParam {
    /**
     * 请求参数：
     * "name": "test_82154d542c34",
     *  "content": "test_38784640a49c",
     *  "activityType": 40,
     *  "amount": 82,
     *  "quota": 54
     */
    private String name;//活动名称
    private String content;//活动内容
    //活动类型：1 LUCKY_BAG 福袋活动；2 RED_PACKET 红包活动；3 SCORECARD_LUCKY_BAG 积分兑换福袋活动；4 SCORE_EXCHANGE_GOLD 积分兑换金币活动
    private Integer activityType;
    private Integer amount;//福袋/红包的数量
    private Integer quota;//红包总额

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getActivityType() {
        return activityType;
    }

    public void setActivityType(Integer activityType) {
        this.activityType = activityType;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }
}
