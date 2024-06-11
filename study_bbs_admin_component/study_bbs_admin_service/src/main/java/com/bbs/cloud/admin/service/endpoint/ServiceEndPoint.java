package com.bbs.cloud.admin.service.endpoint;

import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.service.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ProjectName: com.bbs.cloud.admin.service.endpoint
 *
 * @author: 10270
 * description: 远程调用服务组件接口, 通过调用活动组件feign客户端ServiceFeighClient实现
 * ：如福袋活动——福袋表lucky_bag、红包活动——红包表red_packet、积分兑换福袋——、积分兑换金币——金币
 */
@RestController
@RequestMapping("service/endpoint")
public class ServiceEndPoint {
    @Autowired
    private ServiceService serviceService;
    /**
     * 福袋活动: 福袋打开是礼物，因此需要获取礼物的unused数量
     */
    @GetMapping("/gift/total/query")
    public HttpResult<Integer> queryServiceGiftTotal(){
        return serviceService.queryServiceGiftTotal();
    }

    /**
     * 福袋活动: 拉取礼物的列表————活动组件那边创建活动生成福袋后，这边对应的礼物数量需要减少
     */
    @GetMapping("/gift/list/query")
    public HttpResult<String> queryServiceGiftList(){
        return serviceService.queryServiceGiftList();
    }

    /**
     * 福袋活动: 更新服务组件的礼物列表
     * @return
     */
    @PostMapping("/gift/list/update")
    public HttpResult updateServiceGiftList(@RequestParam("data") String data){
        return serviceService.updateServiceGiftList(data);
    }

    /**
     * 红包活动: 查询service_gold表中的金币unused数量
     * @return
     */
    @GetMapping("/gold/query")
    public HttpResult<Integer> queryServiceGold(){
        return serviceService.queryServiceGold();
    }
    /**
     * 红包活动: 由于创建活动后消耗了一些金币, 因此需更新service_gold表中的相关的金币数量(used已使用的数量)
     * @return
     */
    @PostMapping("/gold/update")
    public HttpResult updateServiceGold(@RequestParam("usedGold") Integer usedGold){
        return serviceService.updateServiceGold(usedGold);
    }
}
