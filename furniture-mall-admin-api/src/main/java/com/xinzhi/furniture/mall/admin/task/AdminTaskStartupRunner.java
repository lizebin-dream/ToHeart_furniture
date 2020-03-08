package com.xinzhi.furniture.mall.admin.task;

import com.xinzhi.furniture.mall.db.util.GrouponConstant;
import com.xinzhi.furniture.mall.core.task.TaskService;
import com.xinzhi.furniture.mall.db.domain.LitemallGrouponRules;
import com.xinzhi.furniture.mall.db.service.LitemallGrouponRulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class AdminTaskStartupRunner implements ApplicationRunner {

    @Autowired
    private LitemallGrouponRulesService rulesService;
    @Autowired
    private TaskService taskService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<LitemallGrouponRules> grouponRulesList = rulesService.queryByStatus(GrouponConstant.RULE_STATUS_ON);
        for(LitemallGrouponRules grouponRules : grouponRulesList){
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expire =  grouponRules.getExpireTime();
            if(expire.isBefore(now)) {
                // 已经过期，则加入延迟队列
                taskService.addTask(new GrouponRuleExpiredTask(grouponRules.getId(), 0));
            }
            else{
                // 还没过期，则加入延迟队列
                long delay = ChronoUnit.MILLIS.between(now, expire);
                taskService.addTask(new GrouponRuleExpiredTask(grouponRules.getId(), delay));
            }
        }
    }
}