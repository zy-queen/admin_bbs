package com.bbs.cloud.admin.activity.service.manage;

import com.bbs.cloud.admin.activity.contant.ActivityContant;
import com.bbs.cloud.admin.activity.dto.ActivityDTO;
import com.bbs.cloud.admin.activity.dto.GiftDTO;
import com.bbs.cloud.admin.activity.dto.LuckyBagDTO;
import com.bbs.cloud.admin.activity.exception.ActivityException;
import com.bbs.cloud.admin.activity.mapper.ActivityMapper;
import com.bbs.cloud.admin.activity.mapper.LuckyBagMapper;
import com.bbs.cloud.admin.activity.param.CreateActivityParam;
import com.bbs.cloud.admin.activity.service.ActivityManage;
import com.bbs.cloud.admin.common.contant.RedisContant;
import com.bbs.cloud.admin.common.enums.activity.ActivityStatusEnum;
import com.bbs.cloud.admin.common.enums.activity.ActivityTypeEnum;
import com.bbs.cloud.admin.common.enums.activity.LuckyBagStatusEnum;
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

import java.util.*;

/**
 * ProjectName: com.bbs.cloud.admin.activity.service.manage
 *
 * @author: 10270
 * description: 活动——福袋活动: 创建、启动、终止
 * 福袋活动特有的验证,quote总额字段不需要
 * 积分兑换金币、红包活动需要quote，福袋、金币兑换福袋不需要quote
 */
@Service
public class LuckyBagActivityManage implements ActivityManage {
    final static Logger logger = LoggerFactory.getLogger(LuckyBagActivityManage.class);
    @Autowired
    private ActivityMapper activityMapper;
    //远程调用接口的客户端————需要远程调用服务组件暴露的接口
    @Autowired
    private ServiceFeighClient serviceFeighClient;

    @Autowired
    private LuckyBagMapper luckyBagMapper;
    //分布式锁
    @Autowired
    private RedisLockHelper redisLockHelper;//组件
    @Autowired
    private JedisUtil jedisUtil;//redis缓存操作的工具: 创建-开启-结束活动,暂存福袋中间状态开启,等活动结束后再更新到数据库中

    /**
     * 创建福袋活动
     * 创建福袋活动流程：1、福袋的数量（空？<1？）；2、远程调用服务组件接口：查看现存礼物总量；3、创建福袋活动
     * 4、包装福袋，批量插入包换好的福袋——》同时需要远程调用服务组件，更新包装福袋后（消耗的礼物）现存的礼物库存
     * 问题：
     * 1、插入福袋发生异常，回滚？添加了try-catch来解决————即创建活动、插入福袋、查询礼物列表并更新
     * 2、多个请求同时过来----redis分布式锁保持数据
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = {Exception.class,HttpException.class})//开启事务,两种异常回滚
    public HttpResult createActivity(CreateActivityParam param) {
        logger.info("开始创建福袋活动，请求参数:{}", JsonUtils.objectToJson(param));
        //获取福袋的数量
        Integer amount = param.getAmount();
        //福袋数量为空
        if(ObjectUtils.isEmpty(amount)){
            logger.info("开始创建福袋活动，福袋数量为空，请求参数:{}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.LUCKY_BAG_ACTIVITY_AMOUNT_IS_NOT_NULL);
        }
        //福袋数量<小于默认最小的(1)————这也是创建不了活动的
        if(amount < ActivityContant.DEFAULT_LUCKY_BAG_ACTIVITY_MIN_AMOUNT){
            logger.info("开始创建福袋活动，福袋数量小于最小值1，请求参数:{}", JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(ActivityException.LUCKY_BAG_ACTIVITY_AMOUNT_LESS_THAN_ONE);
        }
        /**
         * 开始添加redis分布式锁---多个请求同时过来的时候保证只有一个能拿到这个key
         */
        //创建这个key的前提是如果key不存在会设置key值，如果已存在就啥也不做——也就说明key不为
        String key = RedisContant.BBS_CLOUD_LOCK_GIFT_KEY;//本系统默认只有一个租户，如果是多个租户还需要加上租户id
        try {
            //锁只有一把
            //被加上时间戳、不允许重试，因此如果已经有了其他请求到这是不能执行的，只有等待——理解为锁住了。测试时改成60000L
            if(redisLockHelper.lock(key, CommonUtil.createUUID(), 3000L)){//给key加锁，从获取礼物列表开始锁
                /**
                 * 远程调用服务组件（ServiceFeighClient提供的接口）查询礼物数量，判断礼物数量是否足够创建福袋活动的
                 * 需判断：1、未使用的另外iu数量是否为空；是否获取成功；2、比较未使用的礼物数量与创建当前活动所需数量的大小
                 */
                //这里如果发生异常，feign会有一些解决方法的
                HttpResult<Integer> result = serviceFeighClient.queryServiceGiftTotal();
                if(result == null || !CommonExceptionEnum.SUCCESS.getCode().equals(result.getCode()) || result.getData() == null){
                    logger.info("开始创建福袋活动，远程调用，获取服务组件礼物总数量失败，请求参数:{}, result:{}", JsonUtils.objectToJson(param), JsonUtils.objectToJson(result));
                    //获取礼物总数量失败
                    return HttpResult.generateHttpResult(ActivityException.LUCKY_BAG_ACTIVITY_SERVICE_GIFT_AMOUNT_FAIL);
                }
                Integer total = result.getData();//通过远程调用服务组件的接口，得到礼物未使用的总数量
                //判断未使用的礼物数量与创建福袋活动所需的礼物数量的大小关系
                if(total < amount){//未使用的小于所需的，还是不能创建活动成功
                    logger.info("开始创建福袋活动，远程调用，获取服务组件礼物总数量不足，请求参数:{}, result:{}", JsonUtils.objectToJson(param), JsonUtils.objectToJson(result));
                    return HttpResult.generateHttpResult(ActivityException.LUCKY_BAG_ACTIVITY_SERVICE_GIFT_AMOUNT_NOT_MEET);
                }
                //第一步：创建活动
                logger.info("开始创建福袋活动----开始创建活动，请求参数:{}", JsonUtils.objectToJson(param));
                ActivityDTO activityDTO = new ActivityDTO();
                activityDTO.setId(CommonUtil.createUUID());
                activityDTO.setName(param.getName());
                activityDTO.setContent(param.getContent());
                activityDTO.setStatus(ActivityStatusEnum.INITIAL.getStatus());//刚开始是一个初始化的状态
                activityDTO.setActivityType(param.getActivityType());
                activityDTO.setAmount(amount);//福袋数量
                activityDTO.setCreateDate(new Date());
                activityDTO.setUpdateDate(new Date());
                activityMapper.insertActivityDTO(activityDTO);//将创建的福袋活动保存到数据库表activity中

                /**
                 * 礼物数量都是够创建当前活动所用的，下面开始创建福袋（随机指定礼物类别成为福袋）
                 * 保存福袋之前，需要远程调用服务组件更新相应的礼物库存
                 */
                //第二步：包装福袋
                List<GiftDTO> giftDTOList = packLuckyBag(amount, activityDTO.getId());//把礼物包装成福袋：随机生成一个数指定礼物为福袋
                //第三步：远程调用更新服务组件的礼物列表service_gift使用情况
                logger.info("开始创建福袋活动----更新服务组件礼物列表，请求参数:{}", JsonUtils.objectToJson(param));
                HttpResult updateResult = serviceFeighClient.updateServiceGiftList(JsonUtils.objectToJson(giftDTOList));
                if(updateResult == null || !CommonExceptionEnum.SUCCESS.getCode().equals(updateResult.getCode())){
                    logger.info("开始创建福袋活动----更新服务组件礼物列表异常，请求参数:{}", JsonUtils.objectToJson(param));
                    throw new HttpException(ActivityException.LUCKY_BAG_ACTIVITY_SERVICE_GIFT_LIST_UPDATE_FAIL);
                }
            }else{//没获取到锁————返回一个请勿重复操作
                logger.info("开始创建福袋活动----请勿重复操作，请求参数:{}", JsonUtils.objectToJson(param));
                return HttpResult.generateHttpResult(ActivityException.ACTIVITY_NOT_REPEAT_MANAGE);
            }
        }catch (HttpException e){//已知的运行时异常
            logger.info("开始创建福袋活动，发生HttpException异常，请求参数:{}", JsonUtils.objectToJson(param));
            e.printStackTrace();
            throw e;
        }catch (Exception e){//未知的运行时异常
            logger.info("开始创建福袋活动，发生Exception异常，请求参数:{}", JsonUtils.objectToJson(param));
            e.printStackTrace();
            throw e;
            //return HttpResult.fail();
        }finally {
            redisLockHelper.releaseLock(key);//释放锁
        }

        return HttpResult.ok();
    }

    /**
     * //1、把礼物包装成福袋；2、同时远程调用服务组件，获取礼物列表，将对应的礼物库存减少
     * 福袋活动表、礼物对应的表都要变
     * @param amount
     */
    private List<GiftDTO> packLuckyBag(Integer amount, String activityId) {
        logger.info("开始创建福袋活动----开始包装福袋，activityId:{},amount:{}", activityId, amount);

        //远程调用服务组件的查询礼物列表接口
        HttpResult<String> result = serviceFeighClient.queryServiceGiftList();//查询礼物列表
        if(result == null || !CommonExceptionEnum.SUCCESS.getCode().equals(result.getCode()) || result.getData() == null){
            logger.info("开始创建福袋活动，包装福袋方法内，远程调用，获取服务组件列表失败，amount:{}, activityId:{}, result:{}", amount, activityId, JsonUtils.objectToJson(result));
            //获取礼物列表失败————抛出异常
            throw new HttpException(ActivityException.LUCKY_BAG_ACTIVITY_QUERY_SERVICE_GIFT_LIST_ERROR);
        }
        String giftListJson = result.getData();
        List<GiftDTO> giftDTOS = JsonUtils.jsonToList(giftListJson, GiftDTO.class);
        Map<Integer, GiftDTO> giftDTOMap = new HashMap<>();
        giftDTOS.forEach(item -> giftDTOMap.put(item.getGiftType(), item));//礼物逐个添加至礼物map中
        List<LuckyBagDTO> luckyBagDTOList = new ArrayList<>();
        for(int i = 0; i < amount; i++){//开始包装福袋，根据传入的数量来，礼物类型有10种，是随机生成的
            LuckyBagDTO luckyBagDTO = new LuckyBagDTO();
            luckyBagDTO.setId(CommonUtil.createUUID());
            luckyBagDTO.setActivityId(activityId);
            luckyBagDTO.setStatus(LuckyBagStatusEnum.NORMAL.getStatus());//福袋的状态
            luckyBagDTO.setGiftType(randomGiftType());//随机生成一个1-10之间
            luckyBagDTOList.add(luckyBagDTO);
            /**
             * 更新服务组件礼物的数量
             */
            GiftDTO giftDTO = giftDTOMap.get(luckyBagDTO.getGiftType());
            //默认每次消费一个:需要更新礼物表中的：已使用、未使用数量
            giftDTO.setUsedAmount(giftDTO.getUsedAmount() + ActivityContant.DEFAULT_LUCKY_BAG_CONSUME_AMOUNT);
            giftDTO.setUnusedAmount(giftDTO.getUnusedAmount() - ActivityContant.DEFAULT_LUCKY_BAG_CONSUME_AMOUNT);
            giftDTOMap.put(luckyBagDTO.getGiftType(), giftDTO);

        }
        luckyBagMapper.insertLuckyBag(luckyBagDTOList);//批量插入包装好的福袋列表
        List giftDTOList = Arrays.asList(giftDTOMap.values().toArray());
        return giftDTOList;//返回远程调用服务组件的礼物列表，后面更新服务组件那边的礼物列表

    }
    /**
     * 生成1-10之间的随机数：随机指定礼物为福袋
     * @return
     */
    private static Integer randomGiftType() {
        int min = 1;
        int max = 10;
        int randomNum = (int)(Math.random() * (max - min + 1)) + min;
        return Integer.valueOf(randomNum);
    }

    /**
     * 启动福袋活动
     * handler到这里直接对具体的福袋活动进行操作——启动活动:1、修改activity表中活动状态为进行中2；2、redis修改福袋活动表lucky_bag中状态
     * @param activityDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public HttpResult startActivity(ActivityDTO activityDTO) {
        logger.info("启动福袋活动, 请求参数param:{}", JsonUtils.objectToJson(activityDTO));
        //生成redis分布式锁的key：防止多个请求重复开启
        String key = RedisContant.BBS_CLOUD_LOCK_ACTIVITY + activityDTO.getId();//各个活动不一样，添加活动的id
        try {
            //添加分布式锁——获取分布式锁
            if(redisLockHelper.lock(key, CommonUtil.createUUID(), 3000L)){//3s过期时间
                activityDTO.setStatus(ActivityStatusEnum.RUNNING.getStatus());//更改活动状态为正在进行中
                activityDTO.setStartDate(new Date());
                activityDTO.setUpdateDate(new Date());
                activityMapper.updateActivity(activityDTO);//更新activity表中的活动（本质来说更新了状态为正在进行中2）
                //查询福袋表lucky_bag——更改对应的福袋状态——使用redis缓存中间状态: 已领取
                List<LuckyBagDTO> luckyBagDTOList = luckyBagMapper.queryLuckyBag(activityDTO.getId());
                //直接 lpush 到redis中, 可能value有点问题, 用这个优化一下
                luckyBagDTOList.forEach(item ->{
                    jedisUtil.lpush(RedisContant.BBS_CLOUD_ACTIVITY_LUCKY_BAG_LIST,JsonUtils.objectToJson(item));
                });
            }else {
                logger.info("启动福袋活动---请勿重复操作, 请求参数:{}", JsonUtils.objectToJson(activityDTO));
                return HttpResult.generateHttpResult(ActivityException.ACTIVITY_NOT_REPEAT_MANAGE);
            }
        }catch (Exception e){
            logger.info("启动福袋活动, 发生异常, 请求参数param:{}", JsonUtils.objectToJson(activityDTO));
            jedisUtil.del(RedisContant.BBS_CLOUD_ACTIVITY_LUCKY_BAG_LIST);//发生回滚，存在的话就会删除
            e.printStackTrace();
            throw e;
        }finally {
            redisLockHelper.releaseLock(key);//释放锁
        }
        return HttpResult.ok();
    }
    /**
     * 终止福袋活动
     * handler到这里直接对具体的福袋活动进行操作——终止活动:1、修改activity表中活动状态为结束3；2、redis修改福袋活动表lucky_bag中状态
     * ————》福袋活动结束后将福袋更新到福袋表中lucky_bag
     * @param activityDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public HttpResult endActivity(ActivityDTO activityDTO) {
        logger.info("终止福袋活动, 请求参数param:{}", JsonUtils.objectToJson(activityDTO));
        //生成redis分布式锁的key：防止多个请求重复开启
        String key = RedisContant.BBS_CLOUD_LOCK_ACTIVITY + activityDTO.getId();//各个活动不一样，添加活动的id

        try {
            //添加分布式锁——获取分布式锁
            if(redisLockHelper.lock(key, CommonUtil.createUUID(), 3000L)){//3s过期时间
                activityDTO.setStatus(ActivityStatusEnum.END.getStatus());//更改活动状态为结束状态
                activityDTO.setEndDate(new Date());
                activityDTO.setUpdateDate(new Date());
                activityMapper.updateActivity(activityDTO);//更新activity表中的活动（本质来说更新了状态为结束3）

                jedisUtil.del(RedisContant.BBS_CLOUD_ACTIVITY_LUCKY_BAG_LIST);//删除redis缓存中的关于福袋表状态的key
                //活动结束, 更行福袋表lucky_bag: NORMAL正常待领取——》INVALID失效
                luckyBagMapper.updateLuckyBag(activityDTO.getId(), LuckyBagStatusEnum.INVALID.getStatus(), LuckyBagStatusEnum.NORMAL.getStatus());

            }else {
                logger.info("终止福袋活动---请勿重复操作, 请求参数:{}", JsonUtils.objectToJson(activityDTO));
                return HttpResult.generateHttpResult(ActivityException.ACTIVITY_NOT_REPEAT_MANAGE);
            }
        }catch (Exception e){
            logger.info("终止福袋活动, 发生异常, 请求参数param:{}", JsonUtils.objectToJson(activityDTO));
            e.printStackTrace();
            throw e;
        }finally {
            redisLockHelper.releaseLock(key);//释放锁
        }
        return HttpResult.ok();
    }

    @Override
    public Integer getActivityType() {
        return ActivityTypeEnum.LUCKY_BAG.getType();
    }
}
