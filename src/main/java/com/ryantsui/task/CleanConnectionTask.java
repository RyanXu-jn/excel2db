package com.ryantsui.task;

import com.ryantsui.cache.DBCache;
import com.ryantsui.entity.Db;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CleanConnectionTask {

    /**
     * 清理过期的connection,10分钟执行一次.
     */
    @Scheduled(cron = "0 10 * * * ? ")
    public void cleanConnectionTask() {
        DBCache.getInstance().closeConnection();
    }
}
