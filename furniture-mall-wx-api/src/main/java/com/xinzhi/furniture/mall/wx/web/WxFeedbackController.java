package com.xinzhi.furniture.mall.wx.web;

import com.xinzhi.furniture.mall.core.util.RegexUtil;
import com.xinzhi.furniture.mall.core.util.ResponseUtil;
import com.xinzhi.furniture.mall.wx.annotation.LoginUser;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.xinzhi.furniture.mall.db.domain.LitemallFeedback;
import com.xinzhi.furniture.mall.db.domain.LitemallUser;
import com.xinzhi.furniture.mall.db.service.LitemallFeedbackService;
import com.xinzhi.furniture.mall.db.service.LitemallUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 意见反馈服务
 *
 * @author Yogeek
 * @date 2018/8/25 14:10
 */
@RestController
@RequestMapping("/wx/feedback")
@Validated
public class WxFeedbackController {
    private final Log logger = LogFactory.getLog(WxFeedbackController.class);

    @Autowired
    private LitemallFeedbackService feedbackService;
    @Autowired
    private LitemallUserService userService;

    private Object validate(LitemallFeedback feedback) {
        String content = feedback.getContent();
        if (StringUtils.isEmpty(content)) {
            return ResponseUtil.badArgument();
        }

        String type = feedback.getFeedType();
        if (StringUtils.isEmpty(type)) {
            return ResponseUtil.badArgument();
        }

        Boolean hasPicture = feedback.getHasPicture();
        if (hasPicture == null || !hasPicture) {
            feedback.setPicUrls(new String[0]);
        }

        // 测试手机号码是否正确
        String mobile = feedback.getMobile();
        if (StringUtils.isEmpty(mobile)) {
            return ResponseUtil.badArgument();
        }
        if (!RegexUtil.isMobileExact(mobile)) {
            return ResponseUtil.badArgument();
        }
        return null;
    }

    /**
     * 添加意见反馈
     *
     * @param userId   用户ID
     * @param feedback 意见反馈
     * @return 操作结果
     */
    @PostMapping("submit")
    public Object submit(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody LitemallFeedback feedback) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        Object error = validate(feedback);
        if (error != null) {
            return error;
        }

        LitemallUser user = userService.findById(userId);
        String username = user.getUsername();
        feedback.setId(null);
        feedback.setUserId(userId);
        feedback.setUsername(username);
        //状态默认是0，1表示状态已发生变化
        feedback.setStatus(1);
        feedbackService.add(feedback);

        return ResponseUtil.ok();
    }

}
