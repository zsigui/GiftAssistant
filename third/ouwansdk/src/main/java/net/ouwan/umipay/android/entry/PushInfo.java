package net.ouwan.umipay.android.entry;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_GetPush;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.basic.Basic_StringUtil;

import org.json.JSONObject;

import java.io.Serializable;
import java.sql.Date;

/**
 * PushInfo
 *
 * @author zacklpx
 *         date 15-4-7
 *         description
 */
public class PushInfo implements Serializable {
	private int mId;
	private String mIconUrl;
	private String mTitle;
	private String mContent;
	private String mUri;

	private int mType;
	private long mShowTime_ms;

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public String getIconUrl() {
		return mIconUrl;
	}

	public void setIconUrl(String iconUrl) {
		mIconUrl = iconUrl;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getContent() {
		return mContent;
	}

	public void setContent(String content) {
		mContent = content;
	}

	public String getUri() {
		return mUri;
	}

	public void setUri(String uri) {
		mUri = uri;
	}

	public int getType() {
		return mType;
	}

	public void setType(int type) {
		mType = type;
	}

	public long getShowTime_ms() {
		return mShowTime_ms;
	}

	public void setShowTime_ms(long showTime_ms) {
		mShowTime_ms = showTime_ms;
	}

	public boolean parser(Gson_Cmd_GetPush.Cmd_GetPush_Data_push cmdGetPushDataPush) {
		if (cmdGetPushDataPush == null) {
			return false;
		}
		try {
			mId = cmdGetPushDataPush.getPid();
			mIconUrl = cmdGetPushDataPush.getPiu();
			mTitle = cmdGetPushDataPush.getTitle();
			mContent = cmdGetPushDataPush.getCt();
			mUri = cmdGetPushDataPush.getUrl();
			mType = cmdGetPushDataPush.getT();
			int delaytime = cmdGetPushDataPush.getDt();
			if (delaytime == 0) {
				delaytime = 10;
			}
//			//简单的避免同时推送多条
//			Random random = new Random();
//			delaytime += random.nextInt(30);

			mShowTime_ms = System.currentTimeMillis() + delaytime * 1000;
			return true;
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	public boolean parser(JSONObject jsonObject) {
		try {
			if (jsonObject == null) {
				return false;
			}
			int pid = Basic_JSONUtil.getInt(jsonObject, "pid", -1);
			if (pid == -1) {
				return false;
			}
			mId = pid;

			//如果icon为空，使用app默认icon
			mIconUrl = Basic_JSONUtil.getString(jsonObject, "piu", null);

			String title = Basic_JSONUtil.getString(jsonObject, "title", null);
			if (Basic_StringUtil.isNullOrEmpty(title)) {
				return false;
			}
			mTitle = title;

			String ct = Basic_JSONUtil.getString(jsonObject, "ct", null);
			if (Basic_StringUtil.isNullOrEmpty(ct)) {
				return false;
			}
			mContent = ct;

			String html = Basic_JSONUtil.getString(jsonObject, "url", null);
			if (Basic_StringUtil.isNullOrEmpty(html)) {
				return false;
			}

			mUri = html;

			mType = Basic_JSONUtil.getInt(jsonObject, "t", 1);

			mShowTime_ms = Basic_JSONUtil.getLong(jsonObject, "st", mShowTime_ms);

			return true;
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	public JSONObject toJsonObject() {
		try {
			JSONObject jsonObject = new JSONObject();
			Basic_JSONUtil.putInt(jsonObject, "pid", mId);
			Basic_JSONUtil.putString(jsonObject, "piu", mIconUrl);
			Basic_JSONUtil.putString(jsonObject, "title", mTitle);
			Basic_JSONUtil.putString(jsonObject, "ct", mContent);
			Basic_JSONUtil.putString(jsonObject, "url", mUri);
			Basic_JSONUtil.putLong(jsonObject, "st", mShowTime_ms);
			Basic_JSONUtil.putInt(jsonObject, "t", mType);
			return jsonObject;
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	@Override
	public String toString() {
		return "showtime at " + new Date(mShowTime_ms).toLocaleString() + "\n" +
				" id = " + mId + "\n" +
				" iconurl = " + mIconUrl + "\n" +
				" mtitle = " + mTitle + "\n" +
				" mContent = " + mContent + "\n" +
				" mHtml = " + mUri + "\n";
	}
}
