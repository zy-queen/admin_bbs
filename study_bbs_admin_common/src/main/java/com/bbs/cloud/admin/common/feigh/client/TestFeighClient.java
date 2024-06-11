package com.bbs.cloud.admin.common.feigh.client;

import com.bbs.cloud.admin.common.feigh.factory.TestFeighFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 这里的接口是由远程调用时实现的--->将对应服务组件的接口对外开放
 */
@FeignClient(name = "test", fallbackFactory = TestFeighFactory.class)
public interface TestFeighClient {

    @GetMapping("/endpoint/feigh1")
    public String testFeigh();

}
