package net.ouwan.umipay.android.manager;

import android.content.Context;
import android.text.TextUtils;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.PushInfo;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_GetPush;
import net.ouwan.umipay.android.io.UmipaySDKDirectoryStorer;
import net.youmi.android.libs.common.cache.Interface_Serializable;
import net.youmi.android.libs.common.cache.Proxy_Common_CacheManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * ProxyPushCacheManager
 *
 * @author zacklpx
 *         date 15-4-7
 *         description
 */
public class ProxyPushCacheManager implements Interface_Serializable {

	private static final String KEY_PUSH = "slkjfdsakjl";
	private static final String CONSUMED_ID_FILE = ".8jjeizj9";
	private static ProxyPushCacheManager mInstance;
	private UmipaySDKDirectoryStorer mPublicFileStorer;
	private List<PushInfo> mPushInfoList = null;
	private List<Integer> mConsumedIdList = null;
	private int mWaitingPushId = -1;
	private Context mContext;

	private ProxyPushCacheManager(Context context) {
		mContext = context;
		mPushInfoList = new ArrayList<PushInfo>();
		mConsumedIdList = new ArrayList<Integer>();
		mPublicFileStorer = UmipaySDKDirectoryStorer.getPublicFileStorer(context);
	}

	public static ProxyPushCacheManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = read(context);
		}
		return mInstance;
	}

	private static ProxyPushCacheManager read(Context context) {
		try {
			ProxyPushCacheManager selfInfo = new ProxyPushCacheManager(context);
			Proxy_Common_CacheManager.getCache(context, selfInfo);
			return selfInfo;
		} catch (Throwable e) {
			return new ProxyPushCacheManager(context);
		}
	}

	public int getWaitingPushId() {
		return mWaitingPushId;
	}

	public void setWaitingPushId(int waitingPushId) {
		mWaitingPushId = waitingPushId;
	}

	public PushInfo getPushInfo(int id) {
		if (mPushInfoList == null) {
			return null;
		}
		try {
			for (PushInfo item : mPushInfoList) {
				if (item != null && item.getId() == id) {
					return item;
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	public void removePushInfo(int id) {
		if (mPushInfoList == null) {
			return;
		}
		try {
			for (PushInfo item : mPushInfoList) {
				if (item != null && item.getId() == id) {
					mPushInfoList.remove(item);
					save();
					return;
				}
			}
		} catch (Exception e) {
			Debug_Log.e(e);
		}
	}

	public PushInfo getLastPushInfo() {
		if (mPushInfoList == null || mPushInfoList.size() <= 0) {
			return null;
		}
		sort();
//		long cur = System.currentTimeMillis();
		try {
			//找出最近将要显示的推送信息
			for (PushInfo item : mPushInfoList) {
				//时间还没过去,且还没被展示过，表明还有效
				if (item != null && !isConsumed(item)) {
					return item;
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	public boolean consumePushInfo(PushInfo pushInfo) {
		if (pushInfo == null) {
			return false;
		}
		if (isConsumed(pushInfo)) {
			return true;
		}
		try {
			if (mConsumedIdList == null) {
				mConsumedIdList = new ArrayList<Integer>();
			}
			if (mConsumedIdList.size() > 300) {
				mConsumedIdList.remove(0);
			}
			mConsumedIdList.add(pushInfo.getId());
			removePushInfo(pushInfo.getId());
			save();
			return true;
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	public boolean markPushInfo(PushInfo pushInfo) {
		if (pushInfo == null) {
			return false;
		}
		if (isConsumed(pushInfo)) {
			return true;
		}
		try {
			ArrayList<Map.Entry<Integer, String>> idList = getConsumedPushIds();
			if (idList == null) {
				idList = new ArrayList<Map.Entry<Integer, String>>();
			}
			if (idList.size() > 300) {
				idList.remove(0);
			}
			idList.add(new PushIdwithPackageNameEntry(pushInfo.getId(), mContext.getPackageName()));
			saveConsumedPushIds(idList);
			return true;
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	public boolean isConsumed(PushInfo pushInfo) {
		if (pushInfo == null) {
			return false;
		}
		try {
			for (Integer integer : mConsumedIdList) {
				if (integer.intValue() == pushInfo.getId()) {
					return true;
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	public boolean isMark(PushInfo pushInfo) {
		if (pushInfo == null) {
			return false;
		}
		try {
			ArrayList<Map.Entry<Integer, String>> idList = getConsumedPushIds();
			if (idList != null && idList.size() > 0) {
				for (int i = 0; i < idList.size(); i++) {
					if (idList.get(i).getKey() == pushInfo.getId() && !idList.get(i).getValue().equals(mContext
							.getPackageName())) {
						return true;
					}
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	public void parserPushInfos(Gson_Cmd_GetPush gsonCmdGetPush) {
		int code = gsonCmdGetPush.getCode();
		if (code == UmipaySDKStatusCode.SUCCESS) {
			try {
				List<Gson_Cmd_GetPush.Cmd_GetPush_Data_push> pushList = gsonCmdGetPush.getData().getPushList();
				if (pushList != null && !pushList.isEmpty()) {
					List<PushInfo> list = new ArrayList<PushInfo>();
					for (Gson_Cmd_GetPush.Cmd_GetPush_Data_push push : pushList) {
						PushInfo pushInfo = new PushInfo();
						if (pushInfo.parser(push)) {
							list.add(pushInfo);
						}
					}
					mPushInfoList = list;
					sort();
					if (mPushInfoList != null && !mPushInfoList.isEmpty()) {
						save();
					}
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		} else {
			Debug_Log.d("Parser push info fail : " + gsonCmdGetPush.getMessage() + "(" + code + ")");
		}
	}

	private void sort() {
		if (mPushInfoList == null) {
			return;
		}
		Comparator<PushInfo> comparator = new Comparator<PushInfo>() {
			@Override
			public int compare(PushInfo lhs, PushInfo rhs) {
				try {
					if (lhs.getShowTime_ms() > rhs.getShowTime_ms()) {
						return 1;
					} else if (lhs.getShowTime_ms() < rhs.getShowTime_ms()) {
						return -1;
					} else {
						return 0;
					}
				} catch (Exception e) {
					return 0;
				}

			}
		};
		Collections.sort(mPushInfoList, comparator);
	}

	public boolean save() {
		try {
			return Proxy_Common_CacheManager.saveCache(mContext, this);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	public boolean checkPushInfo (PushInfo pushInfo) {

		if (pushInfo == null) {
			return false;
		}

		FileOutputStream fos = null;
		FileChannel fileChannel = null;
		FileLock fileLock = null;
		File file = null;
		try {
			if (!mPublicFileStorer.isFileExistInDirectory(CONSUMED_ID_FILE)) {
				mPublicFileStorer.getFileByFileName(CONSUMED_ID_FILE).createNewFile();
			}

			file = mPublicFileStorer.getFileByFileName(CONSUMED_ID_FILE);
		}catch (Throwable e) {
			Debug_Log.e(e);
		}
		if (file == null || !file.exists()) {
			return false;
		}

		try {
			fos = new FileOutputStream(file,true);
			fileChannel= fos.getChannel();
			while (true) {
				try {
					fileLock = fileChannel.tryLock();
					break;
				}catch (Throwable e) {
					Debug_Log.dd(e);
					Thread.sleep(1000);
				}
			}
			Debug_Log.dd("get lock");
			ArrayList<Map.Entry<Integer, String>> result = new ArrayList<Map.Entry<Integer, String>>();
			FileReader fileReader = null;
			BufferedReader bufferedReader = null;
			try {
				fileReader = new FileReader(file);
				bufferedReader = new BufferedReader(fileReader);
				while (true) {
					String str = bufferedReader.readLine();
					if (TextUtils.isEmpty(str)) {
						break;
					}
					try {
						String[] item = str.split(",");
						Integer id = Integer.valueOf(item[0]);
						result.add(new PushIdwithPackageNameEntry(id, item[1]));
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				}
			}catch (Throwable e) {
				Debug_Log.e(e);
			}finally {
				try {
					if (null != bufferedReader) {
						bufferedReader.close();
					}
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
				try {
					if (null != fileReader) {
						fileReader.close();
					}
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
			}
			boolean isMark = false;
			boolean isvalid = true;
			if (result.size() > 0) {
				for (int i = 0; i < result.size(); i++) {
					if (result.get(i).getKey() == pushInfo.getId()) {
						isMark = true;
						if (!result.get(i).getValue().equals(mContext.getPackageName())) {
							isvalid = false;
						}
					}
				}
			}
			if (!isMark) {
				if (result.size() > 300) {
					result.remove(0);
				}
				result.add(new PushIdwithPackageNameEntry(pushInfo.getId(), mContext.getPackageName()));
				FileWriter fileWriter = null;
				BufferedWriter bufferedWriter = null;
				try {
					fileWriter = new FileWriter(file, false);
					bufferedWriter = new BufferedWriter(fileWriter);
					if (!result.isEmpty()) {
						for (int i = 0; i < result.size(); i++) {
							bufferedWriter.write(String.valueOf(result.get(i).getKey()) + "," + result.get(i).getValue());
							bufferedWriter.newLine();
						}
						bufferedWriter.flush();
					}
				} catch (Throwable e) {
					Debug_Log.e(e);
				} finally {
					try {
						if (null != bufferedWriter) {
							bufferedWriter.close();
						}
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
					try {
						if (null != fileWriter) {
							fileWriter.close();
						}
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				}
			}
			return isvalid;
		}catch (Throwable e) {
			Debug_Log.e(e);
		}finally {
			try {
				if (fileLock != null) {
					fileLock.release();
					Debug_Log.dd("release lock");
				}
			}catch (Throwable e) {
				Debug_Log.e(e);
			}
			try {
				if (fileChannel != null) {
					fileChannel.close();
				}
			}catch (Throwable e) {
				Debug_Log.e(e);
			}
			try {
				if (fos != null) {
					fos.close();
				}
			}catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
		return false;
	}

	private void saveConsumedPushIds(ArrayList<Map.Entry<Integer, String>> idList) {
		try {
			if (idList != null && !idList.isEmpty()) {
				if (!mPublicFileStorer.isFileExistInDirectory(CONSUMED_ID_FILE)) {
					mPublicFileStorer.getFileByFileName(CONSUMED_ID_FILE).createNewFile();
				}
				File file = mPublicFileStorer.getFileByFileName(CONSUMED_ID_FILE);
				writeComsumedPushIds(file, idList);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	private ArrayList<Map.Entry<Integer, String>> getConsumedPushIds() {
		try {
			if (!mPublicFileStorer.isFileExistInDirectory(CONSUMED_ID_FILE)) {
				mPublicFileStorer.getFileByFileName(CONSUMED_ID_FILE).createNewFile();
			}
			File file = mPublicFileStorer.getFileByFileName(CONSUMED_ID_FILE);
			return readComsumedPushIds(file);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	private ArrayList<Map.Entry<Integer, String>> readComsumedPushIds(File file) {
		ArrayList<Map.Entry<Integer, String>> result = new ArrayList<Map.Entry<Integer, String>>();
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		FileChannel fileChannel = null;
		FileLock fileLock = null;
		try {
			RandomAccessFile raf = new RandomAccessFile(file,"rw");
			fileChannel = raf.getChannel();
			fileLock = fileChannel.lock();
			Debug_Log.dd("readComsumedPushIds get lock");
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			while (true) {
				String str = bufferedReader.readLine();
				if (TextUtils.isEmpty(str)) {
					break;
				}
				try {
					String[] item = str.split(",");
					Integer id = Integer.valueOf(item[0]);
					result.add(new PushIdwithPackageNameEntry(id, item[1]));
				} catch (Throwable e) {
					Debug_Log.e(e);
					if (file.exists()) {
						file.delete();
					}
				}
			}
		} catch (Exception e) {
			Debug_Log.e(e);
		} finally {
			try {
				if (null != bufferedReader) {
					bufferedReader.close();
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
			try {
				if (null != fileReader) {
					fileReader.close();
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
			try {
				if (fileLock != null) {
					fileLock.release();
					Debug_Log.dd("readComsumedPushIds release lock");
				}
			}catch (Throwable e) {
				Debug_Log.e(e);
			}
			try {
				if (fileChannel != null) {
					fileChannel.close();
				}
			}catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
		return result;
	}

	private void writeComsumedPushIds(File file, ArrayList<Map.Entry<Integer, String>> idList) {
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		FileChannel fileChannel = null;
		FileLock fileLock = null;
		try {
			RandomAccessFile raf = new RandomAccessFile(file,"rw");
			fileChannel = raf.getChannel();
			fileLock = fileChannel.lock();
			Debug_Log.dd("writeComsumedPushIds get lock");
			fileWriter = new FileWriter(file, false);
			bufferedWriter = new BufferedWriter(fileWriter);
			if (idList != null && !idList.isEmpty()) {
				for (int i = 0; i < idList.size(); i++) {
					bufferedWriter.write(String.valueOf(idList.get(i).getKey()) + "," + idList.get(i).getValue());
					bufferedWriter.newLine();
				}
				bufferedWriter.flush();
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		} finally {
			try {
				if (null != bufferedWriter) {
					bufferedWriter.close();
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
			try {
				if (null != fileWriter) {
					fileWriter.close();
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
			try {
				if (fileLock != null) {
					fileLock.release();
					Debug_Log.dd("writeComsumedPushIds release lock");
				}
			}catch (Throwable e) {
				Debug_Log.e(e);
			}
			try {
				if (fileChannel != null) {
					fileChannel.close();
				}
			}catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
	}

	@Override
	public String serialize() {
		try {
			JSONObject jObject = new JSONObject();
			JSONArray array = new JSONArray();
			if (mPushInfoList != null) {
				for (PushInfo pushInfo : mPushInfoList) {
					array.put(pushInfo.toJsonObject());
				}
			}
			jObject.put("pushinfoList", array);
			JSONArray array2 = new JSONArray();
			if (mConsumedIdList != null) {
				for (Integer idInteger : mConsumedIdList) {
					array2.put(idInteger.intValue());
				}
			}
			jObject.put("consumedIds", array2);
			jObject.put("waitingId", mWaitingPushId);
			return jObject.toString();
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	@Override
	public boolean deserialize(String json) {
		try {
			JSONObject jO = new JSONObject(json);
			JSONArray array = jO.getJSONArray("pushinfoList");
			if (array != null) {
				mPushInfoList = new ArrayList<PushInfo>();
				for (int i = 0; i < array.length(); i++) {
					JSONObject item = null;
					try {
						item = array.getJSONObject(i);
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
					PushInfo pushInfo = new PushInfo();
					if (pushInfo.parser(item)) {
						mPushInfoList.add(pushInfo);
					}
				}
			}
			JSONArray array2 = jO.getJSONArray("consumedIds");
			if (array2 != null) {
				mConsumedIdList = new ArrayList<Integer>();
				for (int i = 0; i < array2.length(); i++) {
					mConsumedIdList.add(array2.getInt(i));
				}
			}
			mWaitingPushId = jO.getInt("waitingId");
			return true;
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	@Override
	public long getValidCacheTime_ms() {
		return -1;
	}

	@Override
	public String getCacheKey() {
		return KEY_PUSH;
	}

	public class PushIdwithPackageNameEntry implements Map.Entry<Integer, String> {
		private Integer mKey;
		private String mValue;

		public PushIdwithPackageNameEntry(Integer key, String value) {
			mKey = key;
			mValue = value;
		}

		@Override
		public Integer getKey() {
			return mKey;
		}

		@Override
		public String getValue() {
			return mValue;
		}

		@Override
		public String setValue(String value) {
			mValue = value;
			return mValue;
		}
	}
}
