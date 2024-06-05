package com.bbs.cloud.admin.service.mapper;

import com.bbs.cloud.admin.service.dto.ServiceGoldDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
/**
 * 操作的table是service_gold
 * 主要的功能：根据名字查询金币、插入金币、更新金币信息
 */
@Mapper
public interface ServiceGoldMapper {

    ServiceGoldDTO queryServiceGoldDTO(@Param("name") String name);

    /**
     * 添加金币服务
     * @param serviceGoldDTO
     */
    void insertServiceGold(ServiceGoldDTO serviceGoldDTO);

    /**
     * 更新金币服务
     * @param serviceGoldDTO
     */
    void updateServiceGold(ServiceGoldDTO serviceGoldDTO);

}
