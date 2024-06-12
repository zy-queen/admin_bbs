package com.bbs.cloud.admin.service.result;

import com.bbs.cloud.admin.service.result.vo.GiftVO;
import com.bbs.cloud.admin.service.result.vo.ServiceGiftVO;
import com.bbs.cloud.admin.service.result.vo.ServiceGoldVO;

import java.util.Map;

/**
 * ProjectName: com.bbs.cloud.admin.service.result
 *
 * @author: 10270
 * description: 返回的服务信息结果result
 */
public class ServiceInfoResult {
    /**
     * 升级的表示方式: 开闭原则,低耦合,高内聚
     * 开闭原则: 允许新增一个类,但是不允许在已有的类上修改
     */
//    private Map<String, String> serviceVO;//key: serviceXXXVO; value: 用json字符串表示

    private ServiceGiftVO serviceGiftVO;
    private ServiceGoldVO serviceGoldVO;
    private Map<Integer, String> serviceType;

    public ServiceGiftVO getServiceGiftVO() {
        return serviceGiftVO;
    }

    public void setServiceGiftVO(ServiceGiftVO serviceGiftVO) {
        this.serviceGiftVO = serviceGiftVO;
    }

    public ServiceGoldVO getServiceGoldVO() {
        return serviceGoldVO;
    }

    public void setServiceGoldVO(ServiceGoldVO serviceGoldVO) {
        this.serviceGoldVO = serviceGoldVO;
    }

    public Map<Integer, String> getServiceType() {
        return serviceType;
    }

    public void setServiceType(Map<Integer, String> serviceType) {
        this.serviceType = serviceType;
    }
}
