package com.bbs.cloud.admin.activity.dto;

import java.util.List;

/**
 * ProjectName: com.bbs.cloud.admin.activity.dto
 *
 * @author: 10270
 * description:
 */
public class ActivityConditionDTO {
    private List<Integer> typeList;
    private List<Integer> statusList;
    private Integer start;
    private Integer limit;

    public List<Integer> getTypeList() {
        return typeList;
    }

    public void setTypeList(List<Integer> typeList) {
        this.typeList = typeList;
    }

    public List<Integer> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<Integer> statusList) {
        this.statusList = statusList;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
