package com.oplay.giftcool.util;

import com.oplay.giftcool.config.AppDebugConfig;

/**
 * Created by zsigui on 16-10-19.
 */

public class RecordTimeUtil {

    private long mLastTime = 0;
    private long mFirstTime = 0;

    public void start() {
        mLastTime = System.currentTimeMillis();
        mFirstTime = mLastTime;
    }

    public void record(String prefix) {
        long curTime = System.currentTimeMillis();
        long spendTime = curTime - mLastTime;
        AppDebugConfig.d(AppDebugConfig.TAG_WARN, String.format("\"%s\" spend %d ms", prefix, spendTime));
        mLastTime = curTime;
    }

    public void stop() {
        long finalTime = System.currentTimeMillis();
        long spendTime = finalTime - mFirstTime;
        AppDebugConfig.d(AppDebugConfig.TAG_WARN, String.format("total execution spend %d ms", spendTime));
    }
}
