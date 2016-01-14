package net.ouwan.umipay.android.entry.gson;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;

/**
 * Gson_Base
 *
 * @author zacklpx
 *         date 15-4-10
 *         description
 */
public class Gson_Base<Data> {

	public Gson_Base(Context context, int code, String message, Data data) {
		mContext = context;
		mCode = code;
		mMessage = message;
		mData = data;
	}

	protected transient Context mContext;

	@SerializedName("c")
	protected int mCode;

	@SerializedName("m")
	protected String mMessage;

	@SerializedName("d")
	protected Data mData;

	public int getCode() {
		return mCode;
	}

	public void setCode(int code) {
		mCode = code;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String message) {
		mMessage = message;
	}

	public Data getData() {
		return mData;
	}

	public void setData(Data data) {
		mData = data;
	}

	public Context getContext() {
		return mContext;
	}

	public void setContext(Context context) {
		mContext = context;
	}

	/**
	 * 检查code = 0但data = null的情况
	 *
	 * @return
	 */
	public boolean checkData() {
		return !(mCode == UmipaySDKStatusCode.SUCCESS && mData == null);
	}
}
