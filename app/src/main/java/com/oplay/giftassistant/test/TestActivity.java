package com.oplay.giftassistant.test;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ext.retrofit2.GsonConverterFactory;
import com.oplay.giftassistant.model.DataModel;
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
	private TestEngine mEngine;
	private Gson mGson;

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


		btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				KLog.i("click");
				final DataModel<String> dataModel = new DataModel<>();
				dataModel.data = etSendData.getText().toString().isEmpty() ?
						"这是测试数据" :
						etSendData.getText().toString();
				dataModel.status = 0;

				mEngine.testPack(dataModel).enqueue(new Callback<DataModel<String>>() {
					@Override
					public void onResponse(final Response<DataModel<String>> response, Retrofit retrofit) {
						KLog.i("response = " + response);
						KLog.i("response.status = " + response.code());
						KLog.i("response.errBody = " + response.errorBody());
						KLog.i("response.body = " + response.body());
						KLog.i("response.body.status = " + response.body()==null? null : response.body().status);
						if (response.code() == 200) {
							TestActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									tvBackData.setText(response.body().data);
								}
							});
						}
					}

					@Override
					public void onFailure(Throwable t) {
						KLog.e(t);
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
