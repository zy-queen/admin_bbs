package com.bbs.cloud.admin.common.feigh.client;

import com.bbs.cloud.admin.common.feigh.factory.ServiceFeighFactory;
import com.bbs.cloud.admin.common.result.HttpResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 这里的接口是由远程调用时实现的--->将对应服务组件的接口对外开放
 * 当远程端需要调用该实现类时，直接使用该接口调用相关方法接口实现远程调用
 * 对应服务组件的endpoint包下的ServiceEndPoint提供的接口，由活动组件通过本客户端即可远程调用
 */
@FeignClient(name = "service", fallbackFactory = ServiceFeighFactory.class)
public interface ServiceFeighClient {

    @GetMapping("service/endpoint/gift/total/query")
    public HttpResult<Integer> queryServiceGiftTotal();

    @GetMapping("service/endpoint/gift/list/query")
    public HttpResult<String> queryServiceGiftList();//查询礼物列表


    @PostMapping("service/endpoint/gift/list/update")
    public HttpResult updateServiceGiftList(@RequestParam("data") String data);


    @GetMapping("service/endpoint/gold/query")
    public HttpResult<Integer> queryServiceGold();


    @PostMapping("service/endpoint/gold/update")
    public HttpResult updateServiceGold(@RequestParam("usedGold") Integer usedGold);
}
