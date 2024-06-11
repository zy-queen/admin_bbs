package com.bbs.cloud.admin.activity.service.manage;

import com.bbs.cloud.admin.activity.contant.ActivityContant;
import com.bbs.cloud.admin.activity.dto.ActivityDTO;
import com.bbs.cloud.admin.activity.dto.ActivityGoldDTO;
import com.bbs.cloud.admin.activity.exception.ActivityException;
import com.bbs.cloud.admin.activity.mapper.ActivityGoldMapper;
import com.bbs.cloud.admin.activity.mapper.ActivityMapper;
import com.bbs.cloud.admin.activity.param.CreateActivityParam;
import com.bbs.cloud.admin.activity.service.ActivityManage;
import com.bbs.cloud.admin.common.contant.RedisContant;
import com.bbs.cloud.admin.common.enums.activity.ActivityGoldStatusEnum;
import com.bbs.cloud.admin.common.enums.activity.ActivityStatusEnum;
import com.bbs.cloud.admin.common.enums.activity.ActivityTypeEnum;
import com.bbs.cloud.admin.common.error.CommonExceptionEnum;
import com.bbs.cloud.admin.common.error.HttpException;
import com.bbs.cloud.admin.common.feigh.client.ServiceFeighClient;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.CommonUtil;
import com.bbs.cloud.admin.common.util.JedisUtil;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.common.util.RedisLockHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ProjectName: com.bbs.cloud.admin.activity.service.manage
 *
 * @author: 10270
 * description: 活动--积分兑换金币活动
 */
@Service
public class ScoreExchangeGoldActivityManage implements ActivityManage {
    final static Logger logger = LoggerFactory.getLogger(RedPacketActivityManage.class);
    @Autowired
    private ActivityMapper activityMapper;//操作的是表activity
    @Autowired
    private ServiceFeighClient serviceFeighClient;//远程调用接口的客户端————需要远程调用服务组件暴露的接口

    @Autowired
    private ActivityGoldMapper activityGoldMapper;

    @Autowired
    private RedisLockHelper redisLockHelper;//redis分布式锁的的添加\释放

    @Autowired
    private JedisUtil jedisUtil;//redis缓存操作的工具: 创建-开启-结束活动,暂存红包中间状态开启,等活动结束后再更新到数据库中

    /**
     * 创建积分兑换金币活动
     * 已验证：name\content\activityTpe, 需验证: amount\quota
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, HttpException.class})//开启事务,两种异常回滚
    public HttpResult createActivity(CreateActivityParam param) {
        logger.info("开始创建积分兑换金币活动, 请求参数: {}", JsonUtils.objectToJson(param));

        //教未加,由于数据库设置为not null,这里加一下
        Integer amount = param.getAmount();
        if(ObjectUtils.isEmpty(amount)) {
            logger.info("开始创建红包活动, 红包数量不能为空, 请求参数:{}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.RED_PACKET_ACTIVITY_AMOUNT_IS_NOT_NULL);
        }
        if(amount < ActivityContant.DEFAULT_RED_PACKET_ACTIVITY_MIN_AMOUNT) {
            logger.info("开始创建红包活动, 红包数量不能小于1, 请求参数:{}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.RED_PACKET_ACTIVITY_AMOUNT_LESS_THAN_ONE);
        }

        Integer quota = param.getQuota();
        if(ObjectUtils.isEmpty(quota)){
            logger.info("开始创建积分兑换金币活动, 金币额度不能为空, 请求参数: {}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.SCORE_GOLD_ACTIVITY_QUOTA_IS_NOT_NULL);
        }
        if(quota < ActivityContant.DEFAULT_SCORE_EXCHANGE_GOLD_ACTIVITY_MIN_QUOTA){//教未改-不影响-都为1
            logger.info("开始创建积分兑换金币活动, 金币额度不能小于1, 请求参数: {}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.SCORE_GOLD_ACTIVITY_QUOTA_LESS_THAN_ONE);
        }
        /**
         * 开始添加redis分布式锁---多个请求同时过来的时候保证只有一个能拿到这个key
         */
        //创建这个key的前提是如果key不存在会设置key值，如果已存在就啥也不做——也就说明key不为
        String key = RedisContant.BBS_CLOUD_LOCK_GOLD_KEY;//锁金币
        try{
            if(redisLockHelper.lock(key, CommonUtil.createUUID(), 3000L)){//redis分布式锁设置3s失效
                logger.info("远程调用----start----获取服务组件未使用的金币额度");
                //远程调用服务组件的获取金币未使用额度的接口(操作的是表service_gold)
                HttpResult<Integer> result = serviceFeighClient.queryServiceGold();//获取金币未使用额度
                logger.info("远程调用----获取服务组件未使用的金币额度, result: {}", JsonUtils.objectToJson(result));
                if(result == null || !CommonExceptionEnum.SUCCESS.getCode().equals(result.getCode()) || result.getData() == null){
                    logger.info("远程调用----获取服务组件未使用的金币额度发生异常, result: {}", JsonUtils.objectToJson(result));
                    return HttpResult.generateHttpResult(ActivityException.SCORE_GOLD_ACTIVITY_SERVICE_GOLD_AMOUNT_QUERY_FAIL);
                }
                Integer serviceGold = result.getData();//目前金币未使用额度
                //创建积分兑换金币活动所需的金币总额度是否大目前于未使用的金币
                if(serviceGold < quota){//目前未使用的金币数量不足以创建积分兑换金币活动
                    logger.info("开始创建积分兑换金币活动, 服务组件金币额度不足, 请求参数: {}, serviceGold: {}",JsonUtils.objectToJson(param), serviceGold);
                    return HttpResult.generateHttpResult(ActivityException.SCORE_GOLD_ACTIVITY_SERVICE_GOLD_AMOUNT_NOT_MEET);
                }
                //第一步：创建活动
                logger.info("开始创建积分兑换金币活动----开始创建活动，请求参数:{}", JsonUtils.objectToJson(param));
                ActivityDTO activityDTO = new ActivityDTO();
                activityDTO.setId(CommonUtil.createUUID());
                activityDTO.setName(param.getName());
                activityDTO.setContent(param.getContent());
                activityDTO.setStatus(ActivityStatusEnum.INITIAL.getStatus());//刚开始是一个初始化的状态
                activityDTO.setActivityType(param.getActivityType());
                activityDTO.setAmount(amount);//教未加,由于数据库设置为not null,这里加一下
                activityDTO.setQuota(quota);//积分兑换金币活动需要使用的金币额度
                activityDTO.setCreateDate(new Date());
                activityDTO.setUpdateDate(new Date());
                activityMapper.insertActivityDTO(activityDTO);//将创建的积分兑换金币活动保存到数据库表activity中

                //第二步: 封装金币使用记录, 并插入
                logger.info("开始创建积分兑换金币活动---开始封装金币使用记录, 请求参数:{}", JsonUtils.objectToJson(param));
                ActivityGoldDTO activityGoldDTO = new ActivityGoldDTO();
                activityGoldDTO.setActivityId(activityDTO.getId());
                activityGoldDTO.setId(CommonUtil.createUUID());
                activityGoldDTO.setStatus(ActivityGoldStatusEnum.NORMAL.getStatus());
                activityGoldDTO.setQuota(quota);
                activityGoldDTO.setUsedQuota(ActivityContant.DEFAULT_ACTIVITY_GOLD_USED_AMOUNT);//已使用, 默认为0
                activityGoldDTO.setUnusedQuota(quota);
                activityGoldMapper.insertActivityGoldDTO(activityGoldDTO);//插入金币活动activity_gold表

                //第三步：远程调用更新服务组件的金币service_gold表的已使用/未使用额度
                logger.info("开始创建积分兑换金币活动---更新服务组件被使用金币数量, 请求参数:{}", JsonUtils.objectToJson(param));
                HttpResult updateResult = serviceFeighClient.updateServiceGold(quota);
                if(updateResult == null || !CommonExceptionEnum.SUCCESS.getCode().equals(updateResult.getCode())){
                    logger.info("开始创建积分兑换金币活动----更新服务组件金币数量异常，请求参数:{}", JsonUtils.objectToJson(param));
                    throw new HttpException(ActivityException.SCORE_GOLD_ACTIVITY_SERVICE_GOLD_AMOUNT_UPDATE_FAIL);
                }
            }else{//没获取到锁————返回一个请勿重复操作
                logger.info("开始创建积分兑换金币活动----请勿重复操作，请求参数:{}", JsonUtils.objectToJson(param));
                return HttpResult.generateHttpResult(ActivityException.ACTIVITY_NOT_REPEAT_MANAGE);
            }
        }catch (HttpException e){
            logger.info("开始创建积分兑换金币活动，发生HttpException异常，请求参数:{}", JsonUtils.objectToJson(param));
            e.printStackTrace();
            throw e;
        }catch (Exception e){
            logger.info("开始创建积分兑换金币活动，发生Exception异常，请求参数:{}", JsonUtils.objectToJson(param));
            e.printStackTrace();
            throw e;
            //return HttpResult.fail();
        }finally {
            redisLockHelper.releaseLock(key);//释放锁
        }
        return HttpResult.ok();
    }

    /**
     * 开启积分兑换金币活动
     * @param activityDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public HttpResult startActivity(ActivityDTO activityDTO) {
        logger.info("启动积分兑换金币活动, 请求参数param:{}", JsonUtils.objectToJson(activityDTO));
        //生成redis分布式锁的key：防止多个请求重复开启
        String key = RedisContant.BBS_CLOUD_LOCK_ACTIVITY + activityDTO.getId();//各个活动不一样，添加活动的id
        try {
            //添加分布式锁——获取分布式锁
            if(redisLockHelper.lock(key, CommonUtil.createUUID(), 3000L)){//3s过期时间
                activityDTO.setStatus(ActivityStatusEnum.RUNNING.getStatus());//更改活动状态为正在进行中
                activityDTO.setStartDate(new Date());
                activityDTO.setUpdateDate(new Date());
                activityMapper.updateActivity(activityDTO);//更新activity表中的活动（本质来说更新了状态为正在进行中2）
                //查询积分兑换金币表activity_gold——更改对应的活动状态——使用redis缓存未使用的数量
                ActivityGoldDTO activityGoldDTO = activityGoldMapper.queryActivityGoldDTOByActivityId(activityDTO.getId());
                //set到redis中, 可能value有点问题, 用这个优化一下
                jedisUtil.set(RedisContant.BBS_CLOUD_ACTIVITY_SCORE_GOLD, String.valueOf(activityGoldDTO.getUnusedQuota()));
            }else {
                logger.info("启动积分兑换金币活动---请勿重复操作, 请求参数:{}", JsonUtils.objectToJson(activityDTO));
                return HttpResult.generateHttpResult(ActivityException.ACTIVITY_NOT_REPEAT_MANAGE);
            }
        }catch (Exception e){
            logger.info("启动积分兑换金币活动, 发生异常, 请求参数param:{}", JsonUtils.objectToJson(activityDTO));
            jedisUtil.del(RedisContant.BBS_CLOUD_ACTIVITY_SCORE_GOLD);//发生回滚，存在的话就会删除
            e.printStackTrace();
            throw e;
        }finally {
            redisLockHelper.releaseLock(key);//释放锁
        }
        return HttpResult.ok();
    }

    @Override
    public HttpResult endActivity(ActivityDTO activityDTO) {
        logger.info("终止积分兑换活动, 请求参数param:{}", JsonUtils.objectToJson(activityDTO));
        //生成redis分布式锁的key：防止多个请求重复开启
        String key = RedisContant.BBS_CLOUD_LOCK_ACTIVITY + activityDTO.getId();//各个活动不一样，添加活动的id

        try {
            //添加分布式锁——获取分布式锁
            if(redisLockHelper.lock(key, CommonUtil.createUUID(), 3000L)){//3s过期时间
                //活动表activity的更新
                activityDTO.setStatus(ActivityStatusEnum.END.getStatus());//更改活动状态为结束状态
                activityDTO.setEndDate(new Date());
                activityDTO.setUpdateDate(new Date());
                activityMapper.updateActivity(activityDTO);//更新activity表中的活动（本质来说更新了状态为结束3）
                //活动表的外键id: 是关联activity_gold积分兑换金币表
                ActivityGoldDTO activityGoldDTO = activityGoldMapper.queryActivityGoldDTOByActivityId(activityDTO.getId());

                Integer unusedGold = jedisUtil.get(RedisContant.BBS_CLOUD_ACTIVITY_SCORE_GOLD, Integer.class);
                jedisUtil.del(RedisContant.BBS_CLOUD_ACTIVITY_SCORE_GOLD);//删除redis缓存中的关于积分兑换金币表activity_gold的key
                //积分兑换金币活动表activity_gold的处理: 金币情况\关联的活动状态
                activityGoldDTO.setUnusedQuota(unusedGold);
                activityGoldDTO.setStatus(ActivityGoldStatusEnum.DEL.getStatus());
                //活动结束, 更新积分兑换金币表activity_gold: 金币使用情况 \ 金币状态
                activityGoldMapper.updateActivityGoldDTO(activityGoldDTO);

            }else {
                logger.info("终止积分兑换活动---请勿重复操作, 请求参数:{}", JsonUtils.objectToJson(activityDTO));
                return HttpResult.generateHttpResult(ActivityException.ACTIVITY_NOT_REPEAT_MANAGE);
            }
        }catch (Exception e){
            logger.info("终止积分兑换活动, 发生异常, 请求参数param:{}", JsonUtils.objectToJson(activityDTO));
            e.printStackTrace();
            throw e;
        }finally {
            redisLockHelper.releaseLock(key);//释放锁
        }
        return HttpResult.ok();
    }

    @Override
    public Integer getActivityType() {
        return ActivityTypeEnum.SCORE_EXCHANGE_GOLD.getType();
    }
}
