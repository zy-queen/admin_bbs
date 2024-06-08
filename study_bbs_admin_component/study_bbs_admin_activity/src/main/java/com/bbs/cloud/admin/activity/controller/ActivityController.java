package com.bbs.cloud.admin.activity.controller;

import com.bbs.cloud.admin.activity.param.CreateActivityParam;
import com.bbs.cloud.admin.activity.param.OperatorActivityParam;
import com.bbs.cloud.admin.activity.service.ActivityService;
import com.bbs.cloud.admin.common.result.HttpResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProjectName: com.bbs.cloud.admin.activity.controller
 *
 * @author: 10270
 * description:
 */
@RestController
@RequestMapping("activity")
public class ActivityController {
    @Autowired
    private ActivityService activityService;
    /**
     * 发布活动：创建活动
     * 活动类型共有四种：福袋活动、红包活动、积分兑换福袋活动、积分兑换金币活动
     * 涉及到用户对福袋/红包进行操作时，使用MQ解耦，即发送消息给服务组件，让服务组件监听消息，操作DB
     */
    //发布活动：创建活动
    @PostMapping("/create")
    public HttpResult createActivity(@RequestBody CreateActivityParam param){//接收请求参数
        return activityService.createActivity(param);
    }
    //开启活动：
    @PostMapping("/start")
    public HttpResult startActivity(@RequestBody OperatorActivityParam param){
        return activityService.startActivity(param);
    }
    //结束活动
    @PostMapping("/end")
    public HttpResult endActivity(@RequestBody OperatorActivityParam param){
        return activityService.endActivity(param);
    }
}
