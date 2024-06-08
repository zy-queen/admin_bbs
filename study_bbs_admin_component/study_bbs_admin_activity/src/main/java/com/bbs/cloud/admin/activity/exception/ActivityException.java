package com.bbs.cloud.admin.activity.exception;
import com.bbs.cloud.admin.common.error.ExceptionCode;

public enum ActivityException implements ExceptionCode {
    /**
     * code含义：1-2：组；3-4：具体的组件/业务（活动异常、福袋活动异常、红包活动异常、积分兑换福袋异常、积分兑换金币异常）；5-6：code的排序
     * 异常代码：前面几位通常是问主管
     * 一些通用的字段（每种活动都共有的）：name、type、id、activity
     * 不同的活动类型所独有的：amount、quota总额（红包、积分兑换金币有总额，而福袋、积分兑换福袋没有总额）
     */

    ACTIVITY_NAME_IS_NOT_EMTRY(600001, "活动名称不能为空", "ACTIVITY_NAME_IS_NOT_EMTRY"),

    ACTIVITY_CONTENT_IS_NOT_EMTRY(600002, "活动内容不能为空", "ACTIVITY_CONTENT_IS_NOT_EMTRY"),

    ACTIVITY_TYPE_IS_NOT_EXIST(600003, "活动类型不存在", "ACTIVITY_TYPE_IS_NOT_EXIST"),

    ACTIVITY_TYPE_ENTITY_IS_EXIST(600004, "同类型活动已存在", "ACTIVITY_TYPE_ENTITY_IS_EXIST"),

    ACTIVITY_ID_IS_NOT_NULL(600005, "该活动ID不能为空", "ACTIVITY_ID_IS_NOT_NULL"),

    ACTIVITY_ID_FORMAT_NOT_TRUE(600006, "该活动ID格式不正确", "ACTIVITY_ID_FORMAT_NOT_TRUE"),

    ACTIVITY_IS_NOT_EXIST(600007, "该活动不存在", "ACTIVITY_IS_NOT_EXIST"),

    ACTIVITY_STATUS_NOT_TRUE(600008, "活动状态不正确", "ACTIVITY_STATUS_NOT_TRUE"),

    PAGE_NOW_LESS_THAN_ZERO(600009, "当前页小于1", "PAGE_NOW_LESS_THAN_ZERO"),

    PAGE_NOW_IS_NOT_NULL(600010, "当前页不能为空", "PAGE_NOW_IS_NOT_NULL"),

    PAGE_SIZE_IS_NOT_NULL(600011, "pageSize不能为空", "PAGE_SIZE_IS_NOT_NULL"),

    PAGE_NOW_GREATER_TOTAL_PAGE(600012, "当前页大于总页数", "PAGE_NOW_GREATER_TOTAL_PAGE"),

    ACTIVITY_DATA_IS_NULL(600013, "当前没有数据", "ACTIVITY_DATA_IS_NULL"),

    ACTIVITY_NOT_REPEAT_MANAGE(600014, "请勿重复操作", "ACTIVITY_NOT_REPEAT_MANAGE"),

    PAGE_SIZE_LESS_THAN_ZERO(600015, "pageSize不能小于1", "PAGE_SIZE_LESS_THAN_ZERO"),

    ACTIVITY_TYPE_IS_NOT_NULL(600016, "活动类型不能为空", "ACTIVITY_TYPE_IS_NOT_NULL"),

    ACTIVITY_STATUS_IS_NOT_NULL(600017, "活动状态不能为空", "ACTIVITY_STATUS_IS_NOT_NULL"),


    /**
     * 福袋活动-------------------------------------------start
     *
     */
    LUCKY_BAG_ACTIVITY_AMOUNT_IS_NOT_NULL(601001, "福袋活动-福袋数量不能为空", "LUCKY_BAG_ACTIVITY_AMOUNT_IS_NOT_NULL"),

    LUCKY_BAG_ACTIVITY_AMOUNT_LESS_THAN_ONE(601002, "福袋活动-福袋数量不能小于1", "LUCKY_BAG_ACTIVITY_AMOUNT_LESS_THAN_ONE"),

    LUCKY_BAG_ACTIVITY_QUERY_SERVICE_GIFT_LIST_ERROR(601003, "获取系统服务礼物列表异常", "LUCKY_BAG_ACTIVITY_QUERY_SERVICE_GIFT_LIST_ERROR"),

    LUCKY_BAG_ACTIVITY_LUCKY_BAG_BATCH_INSERT_ERROR(601004, "福袋活动-福袋批量添加异常", "LUCKY_BAG_ACTIVITY_LUCKY_BAG_BATCH_INSERT_ERROR"),

    LUCKY_BAG_ACTIVITY_SERVICE_GIFT_AMOUNT_FAIL(601005, "获取系统服务礼物总数量异常", "LUCKY_BAG_ACTIVITY_SERVICE_GIFT_AMOUNT_FAIL"),

    LUCKY_BAG_ACTIVITY_SERVICE_GIFT_AMOUNT_NOT_MEET(601006, "系统服务礼物数量不足", "LUCKY_BAG_ACTIVITY_SERVICE_GIFT_AMOUNT_NOT_MEET"),

    LUCKY_BAG_ACTIVITY_SERVICE_GIFT_LIST_UPDATE_FAIL(601007, "服务组件礼物列表更新失败", "LUCKY_BAG_ACTIVITY_SERVICE_GIFT_LIST_UPDATE_FAIL"),

    /**
     * 福袋活动--------------------------------------------end
     */

    /**
     * 红包活动--------------------------------------------------start
     */
    RED_PACKET_ACTIVITY_AMOUNT_IS_NOT_NULL(602001, "红包活动-红包数量不能为空", "RED_PACKET_ACTIVITY_AMOUNT_IS_NOT_NULL"),

    RED_PACKET_ACTIVITY_AMOUNT_LESS_THAN_ONE(602002, "红包活动-红包数量不能小于1", "RED_PACKET_ACTIVITY_AMOUNT_LESS_THAN_ONE"),

    RED_PACKET_ACTIVITY_QUOTA_IS_NOT_NULL(602003, "红包活动-金币总额不能为空", "RED_PACKET_ACTIVITY_QUOTA_IS_NOT_NULL"),

    RED_PACKET_ACTIVITY_QUOTA_LESS_THAN_ONE(602004, "红包活动-金币总额不能小于1", "RED_PACKET_ACTIVITY_QUOTA_LESS_THAN_ONE"),

    RED_PACKET_ACTIVITY_SERVICE_GOLD_AMOUNT_QUERY_FAIL(602005, "获取系统服务未使用金币总数量异常", "RED_PACKET_ACTIVITY_SERVICE_GOLD_AMOUNT_QUERY_FAIL"),

    RED_PACKET_ACTIVITY_SERVICE_GOLD_AMOUNT_NOT_MEET(602006, "系统服务未使用金币总数量不足", "RED_PACKET_ACTIVITY_SERVICE_GOLD_AMOUNT_NOT_MEET"),

    RED_PACKET_ACTIVITY_SERVICE_GOLD_UPDATE_FAIL(602007, "系统服务未使用金币数量更新失败", "RED_PACKET_ACTIVITY_SERVICE_GOLD_UPDATE_FAIL"),

    /**
     * 红包活动--------------------------------------------------end
     */

    /**
     * 积分兑换福袋活动-------------------------------------------start
     */
    SCORE_LUCKY_BAG_ACTIVITY_AMOUNT_IS_NOT_NULL(603001, "积分兑换福袋活动-福袋数量不能为空", "SCORE_LUCKY_BAG_ACTIVITY_AMOUNT_IS_NOT_NULL"),

    SCORE_LUCKY_BAG_ACTIVITY_AMOUNT_LESS_THAN_ONE(603002, "积分兑换福袋活动-福袋数量不能小于1", "SCORE_LUCKY_BAG_ACTIVITY_AMOUNT_LESS_THAN_ONE"),

    SCORE_LUCKY_BAG_ACTIVITY_QUERY_SERVICE_GIFT_LIST_ERROR(603003, "获取系统服务礼物列表异常", "SCORE_LUCKY_BAG_ACTIVITY_QUERY_SERVICE_GIFT_LIST_ERROR"),

    SCORE_LUCKY_BAG_ACTIVITY_LUCKY_BAG_BATCH_INSERT_ERROR(603004, "积分兑换福袋活动-福袋批量添加异常", "SCORE_LUCKY_BAG_ACTIVITY_LUCKY_BAG_BATCH_INSERT_ERROR"),

    SCORE_LUCKY_BAG_ACTIVITY_SERVICE_GIFT_AMOUNT_FAIL(603005, "获取系统服务礼物总数量异常", "SCORE_LUCKY_BAG_ACTIVITY_SERVICE_GIFT_AMOUNT_FAIL"),

    SCORE_LUCKY_BAG_ACTIVITY_SERVICE_GIFT_AMOUNT_NOT_MEET(603006, "系统服务礼物数量不足", "SCORE_LUCKY_BAG_ACTIVITY_SERVICE_GIFT_AMOUNT_NOT_MEET"),

    SCORE_LUCKY_BAG_ACTIVITY_SERVICE_GIFT_LIST_UPDATE_FAIL(603007, "更新服务组件礼物列表异常", "SCORE_LUCKY_BAG_ACTIVITY_SERVICE_GIFT_LIST_UPDATE_FAIL"),
    /**
     * 积分兑换福袋活动--------------------------------------------end
     */

    /**
     * 积分兑换金币活动--------------------------------------------------start
     */
    SCORE_GOLD_ACTIVITY_QUOTA_IS_NOT_NULL(604001, "积分兑换金币活动-金币总额不能为空", "SCORE_GOLD_ACTIVITY_QUOTA_IS_NOT_NULL"),

    SCORE_GOLD_ACTIVITY_QUOTA_LESS_THAN_ONE(604002, "积分兑换金币活动-金币总额不能小于1", "SCORE_GOLD_ACTIVITY_QUOTA_LESS_THAN_ONE"),

    SCORE_GOLD_ACTIVITY_SERVICE_GOLD_AMOUNT_QUERY_FAIL(604003, "获取系统服务未使用金币总数量异常", "SCORE_GOLD_ACTIVITY_SERVICE_GOLD_AMOUNT_QUERY_FAIL"),

    SCORE_GOLD_ACTIVITY_SERVICE_GOLD_AMOUNT_NOT_MEET(604004, "系统服务未使用金币总数量不足", "SCORE_GOLD_ACTIVITY_SERVICE_GOLD_AMOUNT_NOT_MEET"),

    SCORE_GOLD_ACTIVITY_SERVICE_GOLD_AMOUNT_UPDATE_FAIL(604005, "系统服务使用金币数量更新失败", "SCORE_GOLD_ACTIVITY_SERVICE_GOLD_AMOUNT_UPDATE_FAIL"),

    /**
     * 积分兑换金币活动--------------------------------------------------end
     */
    ;

    private Integer code;

    private String message;

    private String name;

    private ActivityException(Integer code, String message, String name) {
        this.code = code;
        this.message = message;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
