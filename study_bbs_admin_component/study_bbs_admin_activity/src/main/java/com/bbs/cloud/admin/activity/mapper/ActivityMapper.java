package com.bbs.cloud.admin.activity.mapper;

import com.bbs.cloud.admin.activity.dto.ActivityConditionDTO;
import com.bbs.cloud.admin.activity.dto.ActivityDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作对应的数据库表：activity
 * 实现的功能：根据活动类型查询活动、插入活动、根据id查询活动、更新活动、查询活动列表（多个）、分页查询
 */
@Mapper
public interface ActivityMapper{

    ActivityDTO queryActivityByType(@Param("activityType") Integer activityType, @Param("statusList") List<Integer> asList);

    void insertActivityDTO(ActivityDTO activityDTO);

    ActivityDTO queryActivityById(@Param("id") String id);

    void updateActivity(ActivityDTO activityDTO);

    List<ActivityDTO> queryActivityList();

    Integer queryActivityCountByCondition(ActivityConditionDTO conditionDTO);

    List<ActivityDTO> queryActivityByCondition(ActivityConditionDTO conditionDTO);
}
