package com.oplay.giftcool.test;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oplay.giftcool.R;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.test.gson_ext.GsonConverterFactory;
import com.oplay.giftcool.util.CommonUtil;
import com.socks.library.KLog;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 测试接口是否正常
 * <p/>
 * Created by zsigui on 15-12-21.
 */
public class TestActivity extends Activity {


	private Button btnSend;
	private EditText etSendData;
	private TextView tvBackData;
	private TextView tvRealData;
	private TestEngine mEngine;
	private Gson mGson;
	public static String RESP_DATA = "";
	public static String REQ_DATA = "";
	public static final int TEST_CMD = 999;

	static {
		System.loadLibrary("ymfx");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_main);
		btnSend = getViewById(R.id.btn_send);
		etSendData = getViewById(R.id.tv_send_data);
		tvBackData = getViewById(R.id.tv_back_data);
		tvRealData = getViewById(R.id.tv_real_data);
		CommonUtil.initMobileInfoModel(this);

		btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				KLog.i("click");
				final JsonReqBase<String> dataModel = new JsonReqBase<>();
				dataModel.data = etSendData.getText().toString().isEmpty() ?
						"这是测试数据" :
						etSendData.getText().toString();
				CommonUtil.addCommonParams(dataModel, TEST_CMD);
				mEngine.testPack(dataModel).enqueue(new Callback<JsonRespBase<String>>() {
					@Override
					public void onResponse(final Response<JsonRespBase<String>> response, Retrofit retrofit) {
						if (response.code() == 200) {
							TestActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {

									tvBackData.setText("请求数据:\n" + TestActivity.REQ_DATA);
									tvRealData.setText("返回数据:\n" + TestActivity.RESP_DATA + "\n\n解析获取数据:\n" + response
											.body().getData());
								}
							});
						} else {
							TestActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									tvBackData.setText("请求数据:\n" + TestActivity.REQ_DATA);
									tvRealData.setText("访问失败!\n" + response.code() + " " + response.errorBody());
								}
							});
						}
					}

					@Override
					public void onFailure(final Throwable t) {
						KLog.e(t);
						TestActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								tvBackData.setText("请求数据:\n" + TestActivity.REQ_DATA);
								tvRealData.setText("出错了！\n" + t.getMessage());
							}
						});
					}
				});

			}
		});
		mGson = new GsonBuilder().create();
		mEngine = new Retrofit.Builder()
				.baseUrl("http://172.16.3.22:8888/")
				.addConverterFactory(GsonConverterFactory.create())
				.build().create(TestEngine.class);
	}

	public <V extends View> V getViewById(@IdRes int id) {
		return (V) findViewById(id);
	}
}
