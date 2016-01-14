package net.ouwan.umipay.android.entry.gson;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;

import java.util.List;

/**
 * Gson_Cmd_GetPush
 *
 * @author zacklpx
 *         date 15-5-4
 *         description
 */
public class Gson_Cmd_GetPush extends Gson_Base<Gson_Cmd_GetPush.Cmd_GetPush_Data> {

	public Gson_Cmd_GetPush(Context context, int code, String message, Cmd_GetPush_Data cmd_getPush_data) {
		super(context, code, message, cmd_getPush_data);
	}

	@Override
	public String toString() {
		return "Gson_Cmd_GetPush{" +
				" code = " + mCode +
				" msg = " + mMessage +
				" data =" + mData +
				"}";
	}

	@Override
	public boolean checkData() {
		if (mCode == UmipaySDKStatusCode.SUCCESS && super.checkData()) {
			return mData.getPushList() != null;
		} else {
			return super.checkData();
		}
	}

	public class Cmd_GetPush_Data {
		@SerializedName("a")
		private List<Cmd_GetPush_Data_push> mPushList;

		public List<Cmd_GetPush_Data_push> getPushList() {
			return mPushList;
		}

		public void setPushList(List<Cmd_GetPush_Data_push> pushList) {
			mPushList = pushList;
		}

		@Override
		public String toString() {
			return "Cmd_GetPush_Data{" +
					"mPushList=" + mPushList +
					'}';
		}
	}

	public class Cmd_GetPush_Data_push {
		@SerializedName("pid")
		private int pid;
		@SerializedName("piu")
		private String piu;
		@SerializedName("title")
		private String title;
		@SerializedName("ct")
		private String ct;
		@SerializedName("t")
		private int t;
		@SerializedName("url")
		private String url;
		@SerializedName("dt")
		private int dt;

		public int getPid() {
			return pid;
		}

		public void setPid(int pid) {
			this.pid = pid;
		}

		public String getPiu() {
			return piu;
		}

		public void setPiu(String piu) {
			this.piu = piu;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getCt() {
			return ct;
		}

		public void setCt(String ct) {
			this.ct = ct;
		}

		public int getT() {
			return t;
		}

		public void setT(int t) {
			this.t = t;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public int getDt() {
			return dt;
		}

		public void setDt(int dt) {
			this.dt = dt;
		}

		@Override
		public String toString() {
			return "Cmd_GetPush_Data_push{" +
					"pid=" + pid +
					", piu='" + piu + '\'' +
					", title='" + title + '\'' +
					", ct='" + ct + '\'' +
					", t=" + t +
					", url='" + url + '\'' +
					", dt=" + dt +
					'}';
		}
	}
}
