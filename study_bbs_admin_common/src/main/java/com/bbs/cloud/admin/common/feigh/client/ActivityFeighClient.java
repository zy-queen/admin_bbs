package com.bbs.cloud.admin.common.feigh.client;

import com.bbs.cloud.admin.common.feigh.factory.ActivityFeighFactory;
import com.bbs.cloud.admin.common.result.HttpResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * 这里的接口是由远程调用时实现的--->将对应服务组件的接口对外开放
 */
@FeignClient(name = "activity", fallbackFactory = ActivityFeighFactory.class)
public interface ActivityFeighClient {
    /**
     * 根据礼物类别去福袋活动表lucky_bag中查询已使用的礼物总数（礼物状态: normal \ geted 的礼物总数-涉及的总行数count(*)）
     * @return
     */
    @GetMapping("activity/endpoint/gift/used/amount/query")
    public HttpResult<Integer> queryServiceGiftTotal(@RequestParam("giftType") Integer giftType);
    /**
     * 查询红包表red_packet\积分兑换金币表activity_gold中已使用的金币情况
     * red_packet表中状态为normal \ geted的quota总额; activity_gold表中状态为del的used_quota已使用额度 \ 状态为normal的quota总额
     * @return
     */
    @GetMapping("activity/endpoint/gold/used/query")
    public HttpResult<Integer> queryUsedGold();
}
