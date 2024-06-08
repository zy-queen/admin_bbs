package com.bbs.cloud.admin.activity.service;

import com.bbs.cloud.admin.activity.dto.ActivityDTO;
import com.bbs.cloud.admin.activity.exception.ActivityException;
import com.bbs.cloud.admin.activity.mapper.ActivityMapper;
import com.bbs.cloud.admin.activity.param.CreateActivityParam;
import com.bbs.cloud.admin.activity.param.OperatorActivityParam;
import com.bbs.cloud.admin.common.enums.activity.ActivityStatusEnum;
import com.bbs.cloud.admin.common.enums.activity.ActivityTypeEnum;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ProjectName: com.bbs.cloud.admin.activity.service
 *
 * @author: 10270
 * description:
 */
@Service
public class ActivityService {
    //控制台输出日志
    final static Logger logger = LoggerFactory.getLogger(ActivityService.class);
    @Autowired
    private List<ActivityManage> activityManages;//加载该接口activityManages的所有实现类

    @Autowired
    private ActivityMapper activityMapper;

    public HttpResult createActivity(CreateActivityParam param) {
        logger.info("开始创建活动，请求参数{}", JsonUtils.objectToJson(param));
        /**
         * 验证几种共有的：定义了一些常见的Activity异常枚举，出现相关异常调用即可
         * 一些通用的字段（每种活动都共有的）：name、type、id、activity
         * 各个活动不同：amount、quota总额（红包、积分兑换金币有总额，而福袋、积分兑换福袋没有总额）———在具体的ActivityManage实现类中去做(福袋、金币)
         * 例如LuckyBagActivityManage中定义福袋活动所独有的一些验证
         */
        String name = param.getName();
        if(StringUtils.isEmpty(name)){
            logger.info("开始创建活动，活动名称为空，请求参数{}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_NAME_IS_NOT_EMTRY);//活动名称不能为空
        }
        String content = param.getContent();
        if(StringUtils.isEmpty(content)){
            logger.info("开始创建活动，活动内容为空，请求参数{}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_CONTENT_IS_NOT_EMTRY);//活动内容不能为空
        }

        Integer activityType = param.getActivityType();
        if(ActivityTypeEnum.getActivityTypeEnumMap().getOrDefault(activityType,null) == null){
            logger.info("开始创建活动，活动类型不存在，请求参数{}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_TYPE_IS_NOT_EXIST);//活动类型不存在
        }

        //根据活动类型查询活动，还需要加上活动状态(1初始化、2正在进行中)【还有0所有状态、3活动终止】
        //status在所列的这两种活动状态中：status in
        /**
         * 公用的字段验证完了，调用各个活动特有的manage设置验证
         */
        //将一个数组转换成一个固定大小的列表（List)
        ActivityDTO activityDTO = activityMapper.queryActivityByType(activityType, Arrays.asList(
                ActivityStatusEnum.INITIAL.getStatus(),
                ActivityStatusEnum.RUNNING.getStatus())
        );

        //查询到的活动结果是否为空
        if(activityDTO != null){
            logger.info("开始创建活动，该类型活动已存在，请求参数{}", JsonUtils.objectToJson(param));
            //该活动类型已存在
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_TYPE_ENTITY_IS_EXIST);

        }
//        ActivityManage activityManage = activityManages.stream()
//                .filter(item -> item.getActivityType().equals(activityType))
//                .findFirst().get();
//        System.out.println(activityManage);
        return activityManages.stream()
                .filter(item -> item.getActivityType().equals(activityType))
                .findFirst().get()
                .createActivity(param);

    }

    public HttpResult startActivity(OperatorActivityParam param) {
        return HttpResult.ok();
    }

    public HttpResult endActivity(OperatorActivityParam param) {
        return HttpResult.ok();
    }
}
