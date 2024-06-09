package com.bbs.cloud.admin.activity.service;

import com.bbs.cloud.admin.activity.contant.ActivityContant;
import com.bbs.cloud.admin.activity.dto.ActivityDTO;
import com.bbs.cloud.admin.activity.exception.ActivityException;
import com.bbs.cloud.admin.activity.mapper.ActivityMapper;
import com.bbs.cloud.admin.activity.mapper.LuckyBagMapper;
import com.bbs.cloud.admin.activity.param.CreateActivityParam;
import com.bbs.cloud.admin.activity.param.OperatorActivityParam;
import com.bbs.cloud.admin.common.enums.activity.ActivityStatusEnum;
import com.bbs.cloud.admin.common.enums.activity.ActivityTypeEnum;
import com.bbs.cloud.admin.common.enums.activity.LuckyBagStatusEnum;
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
    private ActivityMapper activityMapper;//操作activity活动表的mapper

    @Autowired
    private LuckyBagMapper luckyBagMapper;//操作lucky_bag福袋活动表的mapper

    /**
     * 创建活动
     * @param param
     * @return
     */
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
        //判断活动类型是否符合要求
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
    /**
     * 启动活动
     * @param param
     * @return
     */
    public HttpResult startActivity(OperatorActivityParam param) {
        /**
         * 处理通用字段，handler过滤器到具体类型的活动再做具体处理
         */
        logger.info("启动活动,请求参数:{}",JsonUtils.objectToJson(param));
        //得到活动id：1、是否为空; 2、长度是否符合; 3、该活动是否创建（即活动表activity中是否存在）; 4、判断activity表中状态;
        String id = param.getId();
        if(StringUtils.isEmpty(id)){//判断活动id是否为空
            logger.info("启动活动---活动id为空, 请求参数:{}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_ID_IS_NOT_NULL);

        }
        if(ActivityContant.ACTIVITY_ID_LENGTH != id.length()){//uuid生成的id长度为32位
            logger.info("启动活动---活动id格式不正确, 请求参数:{}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_ID_FORMAT_NOT_TRUE);
        }
        ActivityDTO activityDTO = activityMapper.queryActivityById(id);//去活动表查询该活动是否存在
        if(activityDTO == null){//activity表中不存在该id——即该活动不存在
            logger.info("启动活动---活动不存在, 请求参数:{}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_IS_NOT_EXIST);
        }
        //活动启动前提：初始状态
        if(!activityDTO.getStatus().equals(ActivityStatusEnum.INITIAL.getStatus())){//活动非初始化状态1
            logger.info("启动活动---活动状态不正确, 请求参数:{}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_STATUS_NOT_TRUE);

        }
        Integer activityType = activityDTO.getActivityType();//activity表中关于该id的活动已经拿到——》得到活动类型
        //符合上面几种要求后，handler过滤到具体的活动类型后做"开启活动"操作
        return activityManages.stream()
                .filter(item -> item.getActivityType().equals(activityType))
                .findFirst().get()
                .startActivity(activityDTO);
    }

    /**
     * 终止活动
     * @param param
     * @return
     */
    public HttpResult endActivity(OperatorActivityParam param) {
        logger.info("终止活动,请求参数:{}",JsonUtils.objectToJson(param));
        //得到活动id：1、是否为空; 2、长度是否符合; 3、该活动是否创建（即活动表activity中是否存在）; 4、判断activity表中状态;
        String id = param.getId();
        if(StringUtils.isEmpty(id)){//判断活动id是否为空
            logger.info("终止活动---活动id为空, 请求参数:{}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_ID_IS_NOT_NULL);

        }
        if(ActivityContant.ACTIVITY_ID_LENGTH != id.length()){//uuid生成的id长度为32位
            logger.info("终止活动---活动id格式不正确, 请求参数:{}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_ID_FORMAT_NOT_TRUE);
        }
        ActivityDTO activityDTO = activityMapper.queryActivityById(id);//去活动表查询该活动是否存在
        if(activityDTO == null){//activity表中不存在该id——即该活动不存在
            logger.info("终止活动---活动不存在, 请求参数:{}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_IS_NOT_EXIST);
        }
        //活动启动前提：活动正在进行中
        if(!activityDTO.getStatus().equals(ActivityStatusEnum.RUNNING.getStatus())){//活动非进行中状态2
            logger.info("终止活动---活动状态不正确, 请求参数:{}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_STATUS_NOT_TRUE);

        }
        Integer activityType = activityDTO.getActivityType();//activity表中关于该id的活动已经拿到——》得到活动类型
        //符合上面几种要求后，handler过滤到具体的活动类型后做"终止活动"操作
        return activityManages.stream()
                .filter(item -> item.getActivityType().equals(activityType))
                .findFirst().get()
                .endActivity(activityDTO);
    }
    /**
     * 根据礼物类别去福袋活动表lucky_bag中查询已使用的礼物总数（礼物状态: 正常待领取、正在使用中的）
     * @return
     */
    public HttpResult<Integer> queryUsedGiftAmountByType(Integer giftType) {
        logger.info("远程调用------start-----获取待领取和已经被领取的礼物数量, giftType: {}", giftType);

        Integer amount = luckyBagMapper.queryGiftAmount(giftType,
                Arrays.asList(
                        LuckyBagStatusEnum.NORMAL.getStatus(),
                        LuckyBagStatusEnum.GETED.getStatus()
                )
        );
        if(amount == null){
            amount = 0;//原始表没有数据会发生这种情况
        }
        logger.info("远程调用------end-----获取待领取和已经被领取的礼物数量, giftType: {}, amount: {}", giftType, amount);
        return new HttpResult(amount);
    }
}
