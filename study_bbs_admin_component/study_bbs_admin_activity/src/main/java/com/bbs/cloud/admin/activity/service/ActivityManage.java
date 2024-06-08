package com.bbs.cloud.admin.activity.service;

import com.bbs.cloud.admin.activity.param.CreateActivityParam;
import com.bbs.cloud.admin.activity.param.OperatorActivityParam;
import com.bbs.cloud.admin.common.result.HttpResult;

/**
 * ProjectName: com.bbs.cloud.admin.activity.service
 *
 * @author: 10270
 * description:
 */
public interface ActivityManage {

    HttpResult createActivity(CreateActivityParam param);
    HttpResult startActivity(OperatorActivityParam param);
    HttpResult endActivity(OperatorActivityParam param);

    Integer getActivityType();//处理哪种类型的接口


}
