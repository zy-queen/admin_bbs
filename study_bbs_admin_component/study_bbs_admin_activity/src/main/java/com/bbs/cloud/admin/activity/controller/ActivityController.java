package com.bbs.cloud.admin.activity.controller;

import com.bbs.cloud.admin.activity.param.CreateActivityParam;
import com.bbs.cloud.admin.activity.param.OperatorActivityParam;
import com.bbs.cloud.admin.activity.param.QueryActivityPageByConditionParam;
import com.bbs.cloud.admin.activity.service.ActivityService;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.CommonUtil;
import com.bbs.cloud.admin.common.util.ExcelUtil;
import com.bbs.cloud.admin.result.vo.ActivityVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * ProjectName: com.bbs.cloud.admin.activity.controller
 *
 * @author: 10270
 * description:
 */
@RestController
@RequestMapping("activity")
public class ActivityController {
    @Autowired
    private ActivityService activityService;
    /**
     * 发布活动：创建活动
     * 活动类型共有四种：福袋活动、红包活动、积分兑换福袋活动、积分兑换金币活动
     * 涉及到用户对福袋/红包进行操作时，使用MQ解耦，即发送消息给服务组件，让服务组件监听消息，操作DB
     */
    //发布活动：创建活动——》初始化状态1
    @PostMapping("/create")
    public HttpResult createActivity(@RequestBody CreateActivityParam param){//接收请求参数
        return activityService.createActivity(param);
    }
    //开启活动——》正在进行中状态2
    @PostMapping("/start")
    public HttpResult startActivity(@RequestBody OperatorActivityParam param){
        return activityService.startActivity(param);
    }
    //结束活动——》结束状态3
    @PostMapping("/end")
    public HttpResult endActivity(@RequestBody OperatorActivityParam param){
        return activityService.endActivity(param);
    }

    /**
     * 按照条件活动分页查询--主要是activity表
     * @param param
     * @return
     */
    @PostMapping("/page/condition/query")
    public HttpResult queryActivityPageByCondition(@RequestBody QueryActivityPageByConditionParam param) {
        return activityService.queryActivityPageByCondition(param);
    }

    /**
     * 利用excel表工具类: 数据库中表 --> xlsx表
     * 例: 导出activity表
     * 从response流中-->将返回的二进制表的内容放到响应流中,在postman测试就将另存为xlsx表文件
     */
    @PostMapping("export")
    public void exportActivityList(HttpServletResponse response){
        String fileName = CommonUtil.createUUID();
        String sheetName = "活动列表";
        String[] headers = {"ID", "名称", "内容", "创建时间"};
        String[] propertys = {"id", "name", "content", "createDate"};
        List data = activityService.queryActivityList();
        List<ActivityVO> activityVOS = new ArrayList<>();
        data.forEach(item -> {
            ActivityVO activityVO = new ActivityVO();
            BeanUtils.copyProperties(item, activityVO);
            activityVOS.add(activityVO);
        });
        ExcelUtil.exportExcel(response, fileName, sheetName, headers, propertys, activityVOS);
    }
}
