package net.ouwan.umipay.android.manager;

import android.content.Context;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_RedPoint;
import net.youmi.android.libs.common.cache.Interface_Serializable;
import net.youmi.android.libs.common.cache.Proxy_Common_CacheManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * FloatmenuCacheManager
 *
 * @author zacklpx
 *         date 15-3-25
 *         description
 */
public class FloatmenuCacheManager extends Observable implements Interface_Serializable {

	private static final String KEY_PUSH = "slkjfdsakjlfloatmenu";
	public static final String TYPE_NOTICE = "notice";
	public static final String TYPE_GIFT = "gift";
	public static final String TYPE_BOARD = "board";
	public static final String TYPE_TRUMPET="trumpet";

	private static FloatmenuCacheManager mInstance;
	private Context mContext;
	private List<String> mNoticeIdList = null;
	private List<String> mBoardIdList = null;
	private List<String> mGiftIdList = null;
	private List<String> mTrumpetIdList = null;

	//只保存已读id，id列表每10分钟或则更换账户更新一次
	private List<String> mConsumedBoardIds = null;
	private List<String> mConsumedTrumpetIds = null;

	//保存上次阅读过的礼包列表
	private List<String> mLastGiftIdsList = null;



	private int mNoticeBubbleNum = 0;
	private int mBoardBubbleNum = 0;
	private int mGiftBubbleNum = 0;
	private int mTrumpetBubbleNum = 0;

	private FloatmenuCacheManager(Context context) {
		this.mContext = context;
		mNoticeIdList = new ArrayList<String>();

		mBoardIdList = new ArrayList<String>();
		mConsumedBoardIds = new ArrayList<String>();

		mGiftIdList = new ArrayList<String>();
		mLastGiftIdsList = new ArrayList<String>();

		mTrumpetIdList = new ArrayList<String>();
		mConsumedTrumpetIds = new ArrayList<String>();
	}

	public synchronized static FloatmenuCacheManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = read(context);
		}
		return mInstance;
	}

	private static FloatmenuCacheManager read(Context context) {
		try {
			FloatmenuCacheManager floatmenuCacheManager = new FloatmenuCacheManager(context);
			Proxy_Common_CacheManager.getCache(context, floatmenuCacheManager);
			return floatmenuCacheManager;
		} catch (Throwable e) {
			return new FloatmenuCacheManager(context);
		}
	}

	public boolean save() {
		try {
			return Proxy_Common_CacheManager.saveCache(mContext, this);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	public void parseRedPointInfos(Gson_Cmd_RedPoint result) {
		try {
			int code = result.getCode();
			if (code == UmipaySDKStatusCode.SUCCESS) {
				List<String> notices = result.getData().getNotices();
				List<String> boards = result.getData().getBoards();
				List<String> gift = result.getData().getGift();
				List<String> trumpet = result.getData().getTrumpet();

				if (mConsumedBoardIds == null) {
					mConsumedBoardIds = new ArrayList<String>();
				}
				if (mLastGiftIdsList == null) {
					mLastGiftIdsList = new ArrayList<String>();
				}

				if(mConsumedTrumpetIds == null){
					mConsumedTrumpetIds =  new ArrayList<String>();
				}

				if (notices == null) {
					notices = new ArrayList<String>();
				}
				mNoticeIdList.clear();
				mNoticeIdList.addAll(notices);
				if (boards == null) {
					boards = new ArrayList<String>();
				}
				mBoardIdList.clear();
				mBoardIdList.addAll(boards);
				if (gift == null) {
					gift = new ArrayList<String>();
				}
				mGiftIdList.clear();
				mGiftIdList.addAll(gift);

				if (trumpet == null) {
					trumpet = new ArrayList<String>();
				}
				mTrumpetIdList.clear();
				mTrumpetIdList.addAll(trumpet);
				//更新列表正常就更新并保存已经读信息
				updateConsumedIds();
				save();
				//更新数据后通知观察者更新
				setChanged();
				Debug_Log.dd("notifyObservers");
				notifyObservers();

			} else {
				Debug_Log.d("Parser redPoint info fail : " + result.getMessage() + "(" + code + ")");
			}
		} catch (Exception e) {
			Debug_Log.e(e);
		}
	}

	private void updateConsumedIds() {
		if (mConsumedBoardIds != null && mBoardIdList != null) {
			List<String> newConsumedBoradIdList = new ArrayList<String>();
			for (String id : mBoardIdList) {//同时在新列表和已读列表中的id才是有效的已读id
				if (mConsumedBoardIds.contains(id)) {
					newConsumedBoradIdList.add(id);
				}
			}
			mConsumedBoardIds = newConsumedBoradIdList;
		}

		if (mConsumedTrumpetIds != null && mTrumpetIdList != null) {
			List<String> newConsumedTrumpetIdList = new ArrayList<String>();
			for (String id : mTrumpetIdList) {
				if (mConsumedTrumpetIds.contains(id)) {
					newConsumedTrumpetIdList.add(id);
				}
			}
			mConsumedTrumpetIds = newConsumedTrumpetIdList;
		}
	}

	public boolean consume(String type, String id) {
		if (type == null || id == null) {
			return false;
		}
		Debug_Log.dd("consume : type = " + type + ",id = " + id);

		try {

			if (type.equals(TYPE_NOTICE)) {
				if (mNoticeIdList != null && mNoticeIdList.contains(id)) {
					mNoticeIdList.remove(id);
					//更新数据后通知观察者更新
					setChanged();
					notifyObservers();
					return true;
				}
			}

			if (type.equals(TYPE_BOARD)) {
				if (mConsumedBoardIds == null) {
					mConsumedBoardIds = new ArrayList<String>();
				}
				if (mBoardIdList != null && mBoardIdList.contains(id) && !mConsumedBoardIds.contains(id)) {
					mBoardBubbleNum--;
					mConsumedBoardIds.add(id);
					save();
					//更新数据后通知观察者更新
					setChanged();
					notifyObservers();
					return true;
				}
			}

			if (type.equals(TYPE_GIFT)) {
				mLastGiftIdsList = mGiftIdList;
				mGiftBubbleNum = 0;
				save();
				//更新数据后通知观察者更新
				setChanged();
				notifyObservers();
				return true;
			}


			if (type.equals(TYPE_TRUMPET)) {
					mConsumedTrumpetIds = mTrumpetIdList;
					mTrumpetBubbleNum = 0;
					save();
					//更新数据后通知观察者更新
					setChanged();
					notifyObservers();
					return true;
			}

		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	public String getConsumedList(String type) {
		List list = null;
		String res = "";
		int len;
		try {

			/*
			if(type.equals(TYPE_NOTICE)){
				if(mNoticeIdList == null){
					return null;
				}
				return String.valueOf(mNoticeIdList.size());
			}
			*/

			if (type.equals(TYPE_BOARD)) {//
				list = mConsumedBoardIds;
			}

			if(type.equals(TYPE_TRUMPET)){
				list = mConsumedTrumpetIds;
			}

			if (list != null) {
				len = list.size();
				for (int i = 0; i < len; i++) {
					res += list.get(i);
					if (i < len - 1) {
						res += ",";
					}
				}
				return res;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}

		return null;
	}

	@Override
	public String serialize() {
		try {
			JSONObject jObject = new JSONObject();

			//BOARD
			JSONArray array = new JSONArray();
			if (mConsumedBoardIds != null) {
				for (String mConsumedBoardId : mConsumedBoardIds) {
					array.put(mConsumedBoardId);
				}
			}
			jObject.put(TYPE_BOARD, array);

			//GIFT
			JSONArray array2 = new JSONArray();
			//存放上次已读礼包列表
			if (mLastGiftIdsList != null) {
				for (String mLastGiftId : mLastGiftIdsList) {
					array2.put(mLastGiftId);
				}
			}
			jObject.put(TYPE_GIFT, array2);

			//TRUMPPET
			JSONArray array3 = new JSONArray();
			if (mConsumedTrumpetIds != null) {
				for (String mConsumedTrumpetId : mConsumedTrumpetIds) {
					array3.put(mConsumedTrumpetId);
				}
			}
			jObject.put(TYPE_TRUMPET, array3);

			return jObject.toString();
		} catch (Throwable e) {
			Debug_Log.e(e);
			return null;
		}
	}

	@Override
	public boolean deserialize(String arg0) {
		try {
			JSONObject jO = new JSONObject(arg0);
			String item = null;


			//BOARD
			JSONArray array1 = jO.getJSONArray(TYPE_BOARD);
			if (array1 != null) {
				mConsumedBoardIds = new ArrayList<String>();
				for (int i = 0; i < array1.length(); i++) {
					try {
						item = array1.getString(i);
					} catch (Throwable e) {
						e.printStackTrace();
					}
					if (item != null) {
						mConsumedBoardIds.add(item);
					}
				}
			}

			//GIFT
			JSONArray array2 = jO.getJSONArray(TYPE_GIFT);
			if (array2 != null) {
				mLastGiftIdsList = new ArrayList<String>();
				for (int i = 0; i < array2.length(); i++) {
					try {
						item = array2.getString(i);
					} catch (Throwable e) {
						e.printStackTrace();
					}
					if (item != null) {
						mLastGiftIdsList.add(item);
					}
				}
			}

			//TRUMPET
			JSONArray array3 = jO.getJSONArray(TYPE_TRUMPET);
			if (array3 != null) {
				mConsumedTrumpetIds = new ArrayList<String>();
				for (int i = 0; i < array3.length(); i++) {
					try {
						item = array3.getString(i);
					} catch (Throwable e) {
						e.printStackTrace();
					}
					if (item != null) {
						mConsumedTrumpetIds.add(item);
					}
				}
			}

			return true;
		} catch (Throwable e) {
			Debug_Log.e(e);
			return false;
		}
	}

	public int getNoticeBubbleNum() {
		if (mNoticeIdList == null) {
			return 0;
		}
		mNoticeBubbleNum = mNoticeIdList.size();
		return (mNoticeBubbleNum > 0 ? mNoticeBubbleNum : 0);

	}

	public int getBoardBubbleNum() {
		if (mBoardIdList == null) {
			return 0;
		}
		if (mConsumedBoardIds == null) {
			mConsumedBoardIds = new ArrayList<String>();
		}
		mBoardBubbleNum = mBoardIdList.size() - mConsumedBoardIds.size();
		return (mBoardBubbleNum > 0 ? mBoardBubbleNum : 0);
	}

	public int getGiftBubbleNum() {
		if (mGiftIdList == null) {
			return 0;
		}
		if (mLastGiftIdsList == null) {
			mLastGiftIdsList = new ArrayList<String>();
		}
		for(String giftId:mGiftIdList){
			if(mLastGiftIdsList.contains(giftId) != true){
				mGiftBubbleNum = mGiftIdList.size();
				return mGiftBubbleNum;
			}
		}
		return 0;
	}

	public int getTrumpetBubbleNum() {
		if (mTrumpetIdList == null) {
			return 0;
		}
		if (mConsumedTrumpetIds == null) {
			mConsumedTrumpetIds = new ArrayList<String>();
		}
		mTrumpetBubbleNum = mTrumpetIdList.size() - mConsumedTrumpetIds.size();
		return (mTrumpetBubbleNum > 0 ? mTrumpetBubbleNum : 0);
	}

	@Override
	public long getValidCacheTime_ms() {
		return -1;
	}

	@Override
	public String getCacheKey() {
		return KEY_PUSH;
	}
}
