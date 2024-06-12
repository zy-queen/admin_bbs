package com.bbs.cloud.admin.result;

import com.bbs.cloud.admin.result.vo.ActivityVO;

import java.util.List;
import java.util.Map;

/**
 * ProjectName: com.bbs.cloud.admin.result.vo
 *
 * @author: 10270
 * description: 定义活动分页查询的返回结果
 */
public class ActivityPageResult {
    private List<ActivityVO> data;
    private Map<Integer, String> statueMap;
    private Map<Integer, String> typeMap;

    public List<ActivityVO> getData() {
        return data;
    }

    public void setData(List<ActivityVO> data) {
        this.data = data;
    }

    public Map<Integer, String> getStatueMap() {
        return statueMap;
    }

    public void setStatueMap(Map<Integer, String> statueMap) {
        this.statueMap = statueMap;
    }

    public Map<Integer, String> getTypeMap() {
        return typeMap;
    }

    public void setTypeMap(Map<Integer, String> typeMap) {
        this.typeMap = typeMap;
    }
}
