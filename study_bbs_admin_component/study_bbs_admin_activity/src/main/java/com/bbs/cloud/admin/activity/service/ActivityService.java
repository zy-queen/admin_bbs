package com.bbs.cloud.admin.activity.service;

import com.bbs.cloud.admin.activity.contant.ActivityContant;
import com.bbs.cloud.admin.activity.dto.ActivityConditionDTO;
import com.bbs.cloud.admin.activity.dto.ActivityDTO;
import com.bbs.cloud.admin.activity.dto.ActivityGoldDTO;
import com.bbs.cloud.admin.activity.exception.ActivityException;
import com.bbs.cloud.admin.activity.mapper.ActivityGoldMapper;
import com.bbs.cloud.admin.activity.mapper.ActivityMapper;
import com.bbs.cloud.admin.activity.mapper.LuckyBagMapper;
import com.bbs.cloud.admin.activity.mapper.RedPacketMapper;
import com.bbs.cloud.admin.activity.param.CreateActivityParam;
import com.bbs.cloud.admin.activity.param.OperatorActivityParam;
import com.bbs.cloud.admin.activity.param.QueryActivityPageByConditionParam;
import com.bbs.cloud.admin.common.enums.activity.*;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.result.ActivityPageResult;
import com.bbs.cloud.admin.result.vo.ActivityVO;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

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
    @Autowired
    private RedPacketMapper redPacketMapper;//操作表red_packet, 红包活动会用到的
    @Autowired
    private ActivityGoldMapper activityGoldMapper;//操作表activity_gold, 积分兑换金币会用到的

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
     * 按照条件活动分页查询--主要是activity表
     * @param param
     * @return
     */
    public HttpResult queryActivityPageByCondition(QueryActivityPageByConditionParam param) {
        logger.info("按照条件分页查询活动列表, param: {}", JsonUtils.objectToJson(param));

        Integer pageNow = param.getPageNow();//当前页
        if(ObjectUtils.isEmpty(pageNow)){
            logger.info("按照条件分页查询活动列表, 当前页为空, param: {}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.PAGE_NOW_IS_NOT_NULL);
        }
        if(pageNow < ActivityContant.DEFAULT_MIN_PAGENOW){
            logger.info("按照条件分页查询活动列表, 当前页小于1, param: {}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.PAGE_NOW_LESS_THAN_ZERO);
        }

        Integer pageSize = param.getPageSize();//页大小
        if(ObjectUtils.isEmpty(pageSize)){
            logger.info("按照条件分页查询活动列表, 当前页数据量为空, param: {}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.PAGE_SIZE_IS_NOT_NULL);
        }
        if(pageSize < ActivityContant.DEFAULT_MIN_PAGESIZE){
            logger.info("按照条件分页查询活动列表, 当前数据量小于1, param: {}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.PAGE_SIZE_LESS_THAN_ZERO);
        }

        Integer type = param.getType();
        if(ObjectUtils.isEmpty(type)){
            logger.info("按照条件分页查询活动列表, 活动类型不能为空, param: {}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_TYPE_IS_NOT_NULL);
        }
        if(ActivityTypeEnum.getActivityQueryUsedMap().getOrDefault(type, null) == null){
            logger.info("按照条件分页查询活动列表, 活动类型不正确, param: {}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_TYPE_IS_NOT_EXIST);
        }

        Integer status = param.getStatus();
        if(ObjectUtils.isEmpty(status)){
            logger.info("按照条件分页查询活动列表, 活动状态不能为空, param: {}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_STATUS_IS_NOT_NULL);
        }
        if(ActivityStatusEnum.getActivityStatusQueryMap().getOrDefault(status, null) == null){
            logger.info("按照条件分页查询活动列表, 活动状态不正确, param: {}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_STATUS_NOT_TRUE);
        }

        //活动类型: 可能是所有类型
        List<Integer> typeList = new ArrayList<>();
        if(type.equals(ActivityTypeEnum.ALL.getType())){
            typeList.add(ActivityTypeEnum.LUCKY_BAG.getType());
            typeList.add(ActivityTypeEnum.RED_PACKET.getType());
            typeList.add(ActivityTypeEnum.SCORE_EXCHANGE_LUCKY_BAG.getType());
            typeList.add(ActivityTypeEnum.SCORE_EXCHANGE_GOLD.getType());
        }else{
            typeList.add(type);
        }

        //活动状态: 可能是所有状态
        List<Integer> statusList = new ArrayList<>();
        if(status.equals(ActivityStatusEnum.ALL.getStatus())){
            statusList.add(ActivityStatusEnum.INITIAL.getStatus());
            statusList.add(ActivityStatusEnum.RUNNING.getStatus());
            statusList.add(ActivityStatusEnum.END.getStatus());
        }else{
            statusList.add(status);
        }

        Integer start = (pageNow - 1) * pageSize;
        Integer limit = pageSize;

        ActivityConditionDTO activityConditionDTO = new ActivityConditionDTO();
        activityConditionDTO.setTypeList(typeList);
        activityConditionDTO.setStatusList(statusList);
        activityConditionDTO.setStart(start);
        activityConditionDTO.setLimit(limit);
        Integer total = activityMapper.queryActivityCountByCondition(activityConditionDTO);
        if(ObjectUtils.isEmpty(total)){
            logger.info("按照条件分页查询活动列表, 当前条件没有查询到数据, param: {}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.ACTIVITY_DATA_IS_NULL);//当前没有数据
        }
        Integer pageTotal = total / pageSize + 1;
        if(pageNow > pageTotal){
            logger.info("按照条件分页查询活动列表, 当前页大于总页数, param: {}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.PAGE_NOW_GREATER_TOTAL_PAGE);
        }

        //开始查询数据
        List<ActivityDTO> activityDTOS = activityMapper.queryActivityByCondition(activityConditionDTO);

        List<ActivityVO> activityVOS = new ArrayList<>();
        activityDTOS.forEach(item -> {
            ActivityVO activityVO = new ActivityVO();
            BeanUtils.copyProperties(item, activityVO);
            activityVOS.add(activityVO);
        });
        //开始封装返回结果
        ActivityPageResult result = new ActivityPageResult();
        result.setStatueMap(ActivityStatusEnum.getActivityStatusMap());
        result.setTypeMap(ActivityTypeEnum.getActivityTypeMap());
        result.setData(activityVOS);

        return new HttpResult(result);

    }

    /**
     * 根据礼物类别去福袋活动表lucky_bag中查询已使用的礼物总数（礼物状态: normal \ geted 的礼物总数-涉及的总行数count(*)）
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

    /**
     * 查询红包表red_packet\积分兑换金币表activity_gold中已使用的金币情况
     * red_packet表中状态为normal \ geted的quota总额; activity_gold表中状态为del的used_quota已使用额度 \ 状态为normal的quota总额
     * @return
     */
    public HttpResult<Integer> queryUsedGold() {

        logger.info("远程调用------start-----获取待领取和已经被领取的红包金币的总额");
        Integer total = 0;
        //第一步: 查询red_packet表已使用的金币情况: 状态为normal \ geted的quota总额
        Integer normalRedPacketGoldTotal = redPacketMapper.queryActivityRedPacket(RedPacketStatusEnum.NORMAL.getStatus());
        if(normalRedPacketGoldTotal == null){
            normalRedPacketGoldTotal = 0;
        }
        logger.info("远程调用-----获取待领取的红包金币的总额, normalRedPacketGoldTotal: {}", normalRedPacketGoldTotal);
        total = total + normalRedPacketGoldTotal;

        Integer getedRedPacketGoldTotal = redPacketMapper.queryActivityRedPacket(RedPacketStatusEnum.GETED.getStatus());
        if(getedRedPacketGoldTotal == null){
            getedRedPacketGoldTotal = 0;
        }
        logger.info("远程调用-----获取被领取的红包金币的总额, getedRedPacketGoldTotal: {}", getedRedPacketGoldTotal);
        total = total + getedRedPacketGoldTotal;

        //第二步: 查询activity_gold表已使用的金币情况: 状态为del的used_quota已使用额度 \ 状态为normal的quota总额
        Integer endActivityUsedGold = activityGoldMapper.queryUsedAmountTotalByStatus(ActivityGoldStatusEnum.DEL.getStatus());//根据状态获取已使用金币额度
        if(endActivityUsedGold == null){
            endActivityUsedGold = 0;
        }
        logger.info("远程调用-----获取被终止的活动使用了多少金币, endActivityUsedGold: {}", endActivityUsedGold);
        total = total + endActivityUsedGold;


        ActivityGoldDTO activityGoldDTO = activityGoldMapper.queryActivityGoldDTOByStatus(ActivityGoldStatusEnum.NORMAL.getStatus());
        logger.info("远程调用-----获取初始化或者运行中的活动使用了多少金币, activityGoldDTO: {}", activityGoldDTO);
        if(activityGoldDTO != null){
            total = total + activityGoldDTO.getQuota();
        }
        return new HttpResult<>(total);
    }

    /**
     * 查询activity表的所有信息
     * @return
     */
    public List<ActivityDTO> queryActivityList() {
        return activityMapper.queryActivityList();//查询activity表的所有信息
    }
}

