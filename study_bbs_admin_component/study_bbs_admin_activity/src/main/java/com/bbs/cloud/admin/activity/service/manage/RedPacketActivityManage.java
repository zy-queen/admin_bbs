package com.bbs.cloud.admin.activity.service.manage;

import com.bbs.cloud.admin.activity.contant.ActivityContant;
import com.bbs.cloud.admin.activity.dto.ActivityDTO;
import com.bbs.cloud.admin.activity.dto.RedPacketDTO;
import com.bbs.cloud.admin.activity.exception.ActivityException;
import com.bbs.cloud.admin.activity.mapper.ActivityMapper;
import com.bbs.cloud.admin.activity.mapper.RedPacketMapper;
import com.bbs.cloud.admin.activity.param.CreateActivityParam;
import com.bbs.cloud.admin.activity.service.ActivityManage;
import com.bbs.cloud.admin.common.contant.RedisContant;
import com.bbs.cloud.admin.common.enums.activity.ActivityStatusEnum;
import com.bbs.cloud.admin.common.enums.activity.ActivityTypeEnum;
import com.bbs.cloud.admin.common.enums.activity.RedPacketStatusEnum;
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
 * description: 活动——红包活动
 */
@Service
public class RedPacketActivityManage implements ActivityManage {
    final static Logger logger = LoggerFactory.getLogger(RedPacketActivityManage.class);
    @Autowired
    private ActivityMapper activityMapper;//操作的是表activity
    @Autowired
    private ServiceFeighClient serviceFeighClient;//远程调用接口的客户端————需要远程调用服务组件暴露的接口

    @Autowired
    private RedPacketMapper redPacketMapper;//操作的是表red_packet

    @Autowired
    private RedisLockHelper redisLockHelper;//redis分布式锁的的添加\释放

    @Autowired
    private JedisUtil jedisUtil;//redis缓存操作的工具: 创建-开启-结束活动,暂存红包中间状态开启,等活动结束后再更新到数据库中

    /**
     * 创建红包活动
     * 已验证：name\content\activityTpe, 需验证: amount\quota
     * 与创建福袋活动一样: 添加事务操作\添加redis分布式锁(在获取金币未使用额度之前)
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = {Exception.class,HttpException.class})//开启事务,两种异常回滚
    public HttpResult createActivity(CreateActivityParam param) {
        logger.info("开始创建红包活动, 请求参数: {}", JsonUtils.objectToJson(param));
        Integer amount = param.getAmount();
        if(ObjectUtils.isEmpty(amount)){
            logger.info("开始创建红包活动, 红包数量不能为空, 请求参数: {}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.RED_PACKET_ACTIVITY_AMOUNT_IS_NOT_NULL);
        }
        if(amount < ActivityContant.DEFAULT_RED_PACKET_ACTIVITY_MIN_AMOUNT){
            logger.info("开始创建红包活动, 红包数量不能小于1, 请求参数: {}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.RED_PACKET_ACTIVITY_AMOUNT_LESS_THAN_ONE);
        }
        Integer quota = param.getQuota();
        if(ObjectUtils.isEmpty(quota)){
            logger.info("开始创建红包活动, 红包额度不能为空, 请求参数: {}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.RED_PACKET_ACTIVITY_QUOTA_IS_NOT_NULL);
        }
        if(quota < ActivityContant.DEFAULT_RED_PACKET_ACTIVITY_MIN_QUOTA){
            logger.info("开始创建红包活动, 红包额度不能小于1, 请求参数: {}",JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.RED_PACKET_ACTIVITY_QUOTA_LESS_THAN_ONE);
        }
        /**
         * 开始添加redis分布式锁---多个请求同时过来的时候保证只有一个能拿到这个key
         */
        //创建这个key的前提是如果key不存在会设置key值，如果已存在就啥也不做——也就说明key不为
        String key = RedisContant.BBS_CLOUD_LOCK_GOLD_KEY;//本系统默认只有一个租户，如果是多个租户还需要加上租户id
        try{
            if(redisLockHelper.lock(key, CommonUtil.createUUID(), 3000L)){//redis分布式锁设置3s失效
                logger.info("远程调用----start----获取服务组件未使用的金币额度");
                //远程调用服务组件的获取金币未使用额度的接口(操作的是表service_gold)
                HttpResult<Integer> result = serviceFeighClient.queryServiceGold();//获取金币未使用额度
                logger.info("远程调用----获取服务组件未使用的金币额度, result: {}", JsonUtils.objectToJson(result));
                if(result == null || !CommonExceptionEnum.SUCCESS.getCode().equals(result.getCode()) || result.getData() == null){
                    logger.info("远程调用----获取服务组件未使用的金币额度发生异常, result: {}", JsonUtils.objectToJson(result));
                    return HttpResult.generateHttpResult(ActivityException.RED_PACKET_ACTIVITY_SERVICE_GOLD_AMOUNT_QUERY_FAIL);
                }
                Integer serviceGold = result.getData();//目前金币未使用额度
                //创建红包活动所需的金币总额度是否大目前于未使用的金币
                if(serviceGold < quota){//目前未使用的金币数量不足以创建红包活动
                    logger.info("开始创建红包活动, 服务组件金币额度不足, 请求参数: {}, serviceGold: {}",JsonUtils.objectToJson(param), serviceGold);
                    return HttpResult.generateHttpResult(ActivityException.RED_PACKET_ACTIVITY_SERVICE_GOLD_AMOUNT_NOT_MEET);
                }
                //第一步：创建活动
                logger.info("开始创建红包活动----开始创建活动，请求参数:{}", JsonUtils.objectToJson(param));
                ActivityDTO activityDTO = new ActivityDTO();
                activityDTO.setId(CommonUtil.createUUID());
                activityDTO.setName(param.getName());
                activityDTO.setContent(param.getContent());
                activityDTO.setStatus(ActivityStatusEnum.INITIAL.getStatus());//刚开始是一个初始化的状态
                activityDTO.setActivityType(param.getActivityType());
                activityDTO.setAmount(amount);//红包数量
                activityDTO.setQuota(quota);//红包活动需要使用的金币额度
                activityDTO.setCreateDate(new Date());
                activityDTO.setUpdateDate(new Date());
                activityMapper.insertActivityDTO(activityDTO);//将创建的福袋活动保存到数据库表activity中

                logger.info("开始创建红包活动----开始封装红包，请求参数:{}", JsonUtils.objectToJson(param));
                //第二步: 包装红包
                List<RedPacketDTO> redPacketDTOS = packRedPacket(amount, quota,activityDTO.getId());
                redPacketMapper.insertRedPacketList(redPacketDTOS);//批量插入包装好的红包
                logger.info("开始创建红包活动----更新服务组件被使用金币数量，请求参数:{}", JsonUtils.objectToJson(param));
                //第三步：远程调用更新服务组件的金币service_gold表的已使用/未使用额度
                HttpResult updateResult = serviceFeighClient.updateServiceGold(quota);
                if(updateResult == null || !CommonExceptionEnum.SUCCESS.getCode().equals(updateResult.getCode())){
                    logger.info("开始创建红包活动----更新服务组件金币数量异常，请求参数:{}", JsonUtils.objectToJson(param));
                    throw new HttpException(ActivityException.RED_PACKET_ACTIVITY_SERVICE_GOLD_UPDATE_FAIL);
                }
            }else{//没获取到锁————返回一个请勿重复操作
                logger.info("开始创建红包活动----请勿重复操作，请求参数:{}", JsonUtils.objectToJson(param));
                return HttpResult.generateHttpResult(ActivityException.ACTIVITY_NOT_REPEAT_MANAGE);
            }
        }catch (HttpException e){
            logger.info("开始创建红包活动，发生HttpException异常，请求参数:{}", JsonUtils.objectToJson(param));
            e.printStackTrace();
            throw e;
        }catch (Exception e){
            logger.info("开始创建红包活动，发生Exception异常，请求参数:{}", JsonUtils.objectToJson(param));
            e.printStackTrace();
            throw e;
            //return HttpResult.fail();
        }finally {
            redisLockHelper.releaseLock(key);//释放锁
        }
        return HttpResult.ok();
    }
    /**
     * //1、把金币包装成红包
     * 红包活动表、金币对应的表都要变
     * @param amount
     */
    private List<RedPacketDTO> packRedPacket(Integer amount, Integer quota, String activityId) {

        int gold = quota / amount;//就是把金币的总额平分一下, 例如: 50个金币包装成20个红包,那么每个红包有2.5个金币
        List<RedPacketDTO> redPacketDTOS = new ArrayList<>();
        while(amount > 0) {//一个一个包装红包
            RedPacketDTO redPacketDTO = new RedPacketDTO();
            redPacketDTO.setId(CommonUtil.createUUID());
            redPacketDTO.setGold(gold);
            redPacketDTO.setActivityId(activityId);
            redPacketDTO.setStatus(RedPacketStatusEnum.NORMAL.getStatus());//正常待领取
            redPacketDTOS.add(redPacketDTO);
            amount --;
        }
        return redPacketDTOS;
    }

    /**
     * 开启红包活动
     * @param activityDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public HttpResult startActivity(ActivityDTO activityDTO) {
        logger.info("启动红包活动, 请求参数param:{}", JsonUtils.objectToJson(activityDTO));
        //生成redis分布式锁的key：防止多个请求重复开启
        String key = RedisContant.BBS_CLOUD_LOCK_ACTIVITY + activityDTO.getId();//各个活动不一样，添加活动的id
        try {
            //添加分布式锁——获取分布式锁
            if(redisLockHelper.lock(key, CommonUtil.createUUID(), 3000L)){//3s过期时间
                activityDTO.setStatus(ActivityStatusEnum.RUNNING.getStatus());//更改活动状态为正在进行中
                activityDTO.setStartDate(new Date());
                activityDTO.setUpdateDate(new Date());
                activityMapper.updateActivity(activityDTO);//更新activity表中的活动（本质来说更新了状态为正在进行中2）
                //查询红包表red_packet——更改对应的红包状态——使用redis缓存中间状态: 已领取
                List<RedPacketDTO> redPacketDTOS = redPacketMapper.queryRedPacketList(activityDTO.getId());
                //直接 lpush 到redis中, 可能value有点问题, 用这个优化一下
                redPacketDTOS.forEach(item ->{
                    jedisUtil.lpush(RedisContant.BBS_CLOUD_ACTIVITY_RED_PACKET_LIST,JsonUtils.objectToJson(item));
                });
            }else {
                logger.info("启动红包活动---请勿重复操作, 请求参数:{}", JsonUtils.objectToJson(activityDTO));
                return HttpResult.generateHttpResult(ActivityException.ACTIVITY_NOT_REPEAT_MANAGE);
            }
        }catch (Exception e){
            logger.info("启动红包活动, 发生异常, 请求参数param:{}", JsonUtils.objectToJson(activityDTO));
            jedisUtil.del(RedisContant.BBS_CLOUD_ACTIVITY_RED_PACKET_LIST);//发生回滚，存在的话就会删除
            e.printStackTrace();
            throw e;
        }finally {
            redisLockHelper.releaseLock(key);//释放锁
        }
        return HttpResult.ok();
    }

    @Override
    public HttpResult endActivity(ActivityDTO activityDTO) {
        logger.info("终止红包活动, 请求参数param:{}", JsonUtils.objectToJson(activityDTO));
        //生成redis分布式锁的key：防止多个请求重复开启
        String key = RedisContant.BBS_CLOUD_LOCK_ACTIVITY + activityDTO.getId();//各个活动不一样，添加活动的id

        try {
            //添加分布式锁——获取分布式锁
            if(redisLockHelper.lock(key, CommonUtil.createUUID(), 3000L)){//3s过期时间
                activityDTO.setStatus(ActivityStatusEnum.END.getStatus());//更改活动状态为结束状态
                activityDTO.setEndDate(new Date());
                activityDTO.setUpdateDate(new Date());
                activityMapper.updateActivity(activityDTO);//更新activity表中的活动（本质来说更新了状态为结束3）
                jedisUtil.del(RedisContant.BBS_CLOUD_ACTIVITY_RED_PACKET_LIST);//删除redis缓存中的关于红包表状态的key
                //活动结束, 更行红包表red_packet: NORMAL正常待领取——》INVALID失效, 方法参数顺序与福袋活动哪里有些不同
                redPacketMapper.updateRedPacket(activityDTO.getId(), RedPacketStatusEnum.NORMAL.getStatus(), RedPacketStatusEnum.INVALID.getStatus());
            }else {
                logger.info("终止红包活动---请勿重复操作, 请求参数:{}", JsonUtils.objectToJson(activityDTO));
                return HttpResult.generateHttpResult(ActivityException.ACTIVITY_NOT_REPEAT_MANAGE);
            }
        }catch (Exception e){
            logger.info("终止红包活动, 发生异常, 请求参数param:{}", JsonUtils.objectToJson(activityDTO));
            e.printStackTrace();
            throw e;
        }finally {
            redisLockHelper.releaseLock(key);//释放锁
        }
        return HttpResult.ok();
    }

    @Override
    public Integer getActivityType() {
        return ActivityTypeEnum.RED_PACKET.getType();//得到红包活动对应的类型2
    }
}
