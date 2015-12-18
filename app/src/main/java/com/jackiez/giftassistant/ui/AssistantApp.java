package com.jackiez.giftassistant.ui;

import android.app.Application;

import com.jackiez.giftassistant.constant.Global;
import com.jackiez.giftassistant.engine.NetEngine;
import com.socks.library.KLog;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class AssistantApp extends Application {

	private static AssistantApp sInstance;
	private NetEngine mEngine;

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		KLog.init(true);
		mEngine = new Retrofit.Builder()
				.baseUrl(Global.BASE_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.build()
				.create(NetEngine.class);

	}

	public static AssistantApp getInstance() {
		return sInstance;
	}

	public NetEngine getEngine() {
		return mEngine;
	}
}
