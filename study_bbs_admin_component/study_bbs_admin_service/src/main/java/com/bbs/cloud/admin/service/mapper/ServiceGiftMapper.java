package com.bbs.cloud.admin.service.mapper;

import com.bbs.cloud.admin.service.dto.ServiceGiftDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作的table是service_gift
 * 主要的功能：根据礼物类型查询礼物、插入礼物、更新礼物、查询多个礼物、查询礼物数量、批量更新礼物列表
 */
@Mapper
public interface ServiceGiftMapper {

    ServiceGiftDTO queryGiftDTO(@Param("giftType") Integer giftType);

    void insertGiftDTO(ServiceGiftDTO giftDTO);

    void updateGiftDTO(ServiceGiftDTO giftDTO);

    List<ServiceGiftDTO> queryGiftDTOList();

    Integer queryGiftAmount();


    void updateGiftDTOList(@Param("list") List<ServiceGiftDTO> data);
}
