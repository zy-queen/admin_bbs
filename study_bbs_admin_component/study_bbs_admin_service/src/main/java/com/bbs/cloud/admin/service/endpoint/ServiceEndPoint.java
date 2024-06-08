package com.bbs.cloud.admin.service.endpoint;

import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.service.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ProjectName: com.bbs.cloud.admin.service.endpoint
 *
 * @author: 10270
 * description:给活动组件查询活动对应的数量：例如福袋活动——福袋数量、红包活动——红包数量、积分兑换福袋——、积分兑换金币——金币
 * 对外的feign接口，远程过程调用————给活动组件调用的接口————活动组件调用对应的feignClient
 */
@RestController
@RequestMapping("service/endpoint")
public class ServiceEndPoint {
    @Autowired
    private ServiceService serviceService;
    /**
     * 福袋打开是礼物，因此需要获取礼物的数量
     */
    @GetMapping("/gift/total/query")
    public HttpResult<Integer> queryServiceGiftTotal(){
        return serviceService.queryServiceGiftTotal();
    }

    /**
     * 拉取礼物的列表————活动组件那边创建活动生成福袋后，这边对应的礼物数量需要减少
     */
    @GetMapping("/gift/list/query")
    public HttpResult<String> queryServiceGiftList(){
        return serviceService.queryServiceGiftList();
    }

    /**
     * 更新服务组件的礼物列表
     * @return
     */
    @PostMapping("/gift/list/update")
    public HttpResult updateServiceGiftList(@RequestParam("data") String data){
        return serviceService.updateServiceGiftList(data);
    }
}
