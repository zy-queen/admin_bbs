package com.bbs.cloud.admin.activity.param;

/**
 * ProjectName: com.bbs.cloud.admin.activity.param
 *
 * @author: 10270
 * description: 操作活动的请求体(开启/结束活动)
 */
public class OperatorActivityParam {
    private String id;//操作活动只需要根据id即可

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
