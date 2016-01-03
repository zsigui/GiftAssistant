package com.oplay.giftassistant.util;

import android.content.Context;
import android.content.Intent;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/1/3
 */
public class IntentUtil {
    public static void judgeWifiSetting(Context context) {
        Intent intent = new Intent();
        intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
        intent.putExtra("extra_prefs_show_button_bar", true);
        intent.putExtra("extra_prefs_set_next_text", "完成");
        intent.putExtra("extra_prefs_set_back_text", "返回");
        intent.putExtra("wifi_enable_next_on_connect", true);
        context.startActivity(intent);
    }
}
