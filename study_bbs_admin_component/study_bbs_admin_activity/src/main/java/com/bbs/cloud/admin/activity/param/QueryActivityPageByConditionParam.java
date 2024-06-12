package com.bbs.cloud.admin.activity.param;

/**
 * ProjectName: com.bbs.cloud.admin.activity.param
 *
 * @author: 10270
 * description: 活动分页查询的请求体结构
 */
public class QueryActivityPageByConditionParam {
    private Integer type;
    private Integer status;
    private Integer pageNow;
    private Integer pageSize;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPageNow() {
        return pageNow;
    }

    public void setPageNow(Integer pageNow) {
        this.pageNow = pageNow;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
