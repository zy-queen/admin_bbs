package com.bbs.cloud.admin.result.vo;

import com.bbs.cloud.admin.common.enums.activity.ActivityStatusEnum;
import com.bbs.cloud.admin.common.enums.activity.ActivityTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * ProjectName: com.bbs.cloud.admin.result.vo
 *
 * @author: 10270
 * description: 用于定义导出xlsx表的返回结果的结构层次, 只有属性: id name content createDate
 *              又用作活动分页查询的其中一个结果: activity表的全部属性
 */
public class ActivityVO {

    private String id;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 活动内容
     */
    private String content;

    /**
     * 活动状态
     * {@link ActivityStatusEnum}
     */
    private Integer status;

    /**
     * 活动类型
     * {@link ActivityTypeEnum}
     */
    private Integer activityType;

    /**
     * 福袋/礼物/红包的数量
     */
    private Integer amount;

    /**
     * 红包总额度
     */
    private Integer quota;

    /**
     * 活动背景地址
     */
    private String imgLink;

    /**
     * 创建时间
     * 注意: timezone要与数据库的一致, 注意数据库的日期时间类型要设置为TIMESTAMP, 而不是DATE类型
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createDate;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date updateDate;

    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date startDate;

    /**
     * 活动终止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date endDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public String getImgLink() {
        return imgLink;
    }

    public void setImgLink(String imgLink) {
        this.imgLink = imgLink;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
