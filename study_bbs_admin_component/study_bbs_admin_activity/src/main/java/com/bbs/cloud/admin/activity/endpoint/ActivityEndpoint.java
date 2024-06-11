package com.bbs.cloud.admin.activity.endpoint;

import com.bbs.cloud.admin.activity.service.ActivityService;
import com.bbs.cloud.admin.common.result.HttpResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProjectName: com.bbs.cloud.admin.activity.endpoint
 *
 * @author: 10270
 * description: 远程调用活动组件接口, 通过调用活动组件feign客户端ActivityFeighClient实现
 */
@RestController
@RequestMapping("activity/endpoint")
public class ActivityEndpoint {
    @Autowired
    private ActivityService activityService;
    /**
     * 根据礼物类别去福袋活动表lucky_bag中查询已使用的礼物总数（礼物状态: normal \ geted 的礼物总数-涉及的总行数count(*)）
     *
     * @return
     */
    @GetMapping("/gift/used/amount/query")
    public HttpResult<Integer> queryUsedGiftAmountByType(@RequestParam("giftType") Integer giftType){
        return activityService.queryUsedGiftAmountByType(giftType);
    }
    /**
     * 查询红包表red_packet\积分兑换金币表activity_gold中已使用的金币情况
     * red_packet表中状态为normal \ geted的quota总额; activity_gold表中状态为del的used_quota已使用额度 \ 状态为normal的quota总额
     * @return
     */
    @GetMapping("/gold/used/query")
    public HttpResult<Integer> queryUsedGold(){
        return activityService.queryUsedGold();
    }
}
