package net.ouwan.umipay.android.manager;

import android.content.Context;
import android.text.TextUtils;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.io.UmipaySDKDirectoryStorer;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.cache.Interface_Serializable;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.platform.global.Global_Runtime_ClientId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;

/**
 * FloatmenuCacheManager
 *
 * @author zacklpx
 *         date 15-3-25
 *         description
 */
public class UmipayCommonAccountCacheManager {

	public static final int COMMON_ACCOUNT_TO_CHANGE = 0;
	public static final int COMMON_ACCOUNT = 1;
	public static final String ACTION_ACCOUNT_CHANGE ="net.ouwan.umipay.android.account.change";
	//一般登录态储存文件
	private static final String COMMON_ACCOUNT_FILE = "0205005e430a15";//account
	//待切换登录态存储文件
	private static final String COMMON_ACCOUNT_TOCHANGE_FILE = "0205005e430a153a405f3d530c510c5152";//account
	private UmipaySDKDirectoryStorer mPublicFileStorer;
	private static UmipayCommonAccountCacheManager mInstance;
	private Context mContext;

	private UmipayCommonAccountCacheManager(Context context) {
		this.mContext = context;
		mPublicFileStorer = UmipaySDKDirectoryStorer.getPublicFileStorer(context);
	}

	public synchronized static UmipayCommonAccountCacheManager getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new UmipayCommonAccountCacheManager(context);
		}
		return mInstance;
	}


	private UmipayCommonAccountData readUmipayCommonAccountData(String fileName) {
		UmipayCommonAccountData result = new UmipayCommonAccountData();
		StringBuffer data = new StringBuffer();
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		FileChannel fileChannel = null;
		FileLock fileLock = null;
		try {
			if (!mPublicFileStorer.isFileExistInDirectory(fileName)) {
				mPublicFileStorer.getFileByFileName(fileName).createNewFile();
			}
			File file = mPublicFileStorer.getFileByFileName(fileName);
			RandomAccessFile raf = new RandomAccessFile(file,"rw");
			fileChannel = raf.getChannel();
			fileLock = fileChannel.lock();
			Debug_Log.dd("readUmipayCommonAccountData get lock");
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			while (true) {
				String str = bufferedReader.readLine();
				//测试
				if (TextUtils.isEmpty(str)) {
					break;
				}
				data.append(str);
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
					Debug_Log.dd("readCommonAccounts release lock");
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
		if(!TextUtils.isEmpty(data.toString())){
			result.deserialize(data.toString());
		}
		return result;
	}

	private void saveUmipayCommonAccountData(UmipayCommonAccountData accountData,String fileName) {
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		FileChannel fileChannel = null;
		FileLock fileLock = null;
		try {
			if (!mPublicFileStorer.isFileExistInDirectory(fileName)) {
				mPublicFileStorer.getFileByFileName(fileName).createNewFile();
			}
			File file = mPublicFileStorer.getFileByFileName(fileName);
			RandomAccessFile raf = new RandomAccessFile(file,"rw");
			fileChannel = raf.getChannel();
			fileLock = fileChannel.lock();
			Debug_Log.dd("saveUmipayCommonAccountData get lock");
			fileWriter = new FileWriter(file, false);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(accountData.serialize());
			bufferedWriter.flush();
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
					Debug_Log.dd("saveUmipayCommonAccountData release lock");
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

	public boolean checkDate(UmipayCommonAccount account){
		if(account == null || account.getUid() == 0 || TextUtils.isEmpty(account.getSession()) || TextUtils.isEmpty(account.getDestPackageName()) || TextUtils.isEmpty(account.getOriginPackageName()) || TextUtils.isEmpty(account.getUserName())) {
			return false;
		}
		return true;
	}

	public synchronized ArrayList<UmipayCommonAccount> getCommonAccountList(int accountType) {
		ArrayList<UmipayCommonAccount> umipayCommonAccountArrayList = new ArrayList<UmipayCommonAccount>();
		String fileName = (accountType == COMMON_ACCOUNT)?COMMON_ACCOUNT_FILE:COMMON_ACCOUNT_TOCHANGE_FILE;
		UmipayCommonAccountData umipayCommonAccountData = readUmipayCommonAccountData(fileName);
		if(umipayCommonAccountData != null){
			JSONArray jsonArray = umipayCommonAccountData.getCommonAccountJSONArray();
			if(jsonArray != null){
				for (int i = 0; i < jsonArray.length(); i++) {
					try {
						String json = Basic_JSONUtil.getString(jsonArray,i,"");
						if(!TextUtils.isEmpty(json)) {
							UmipayCommonAccount account = new UmipayCommonAccount(json);
							if(checkDate(account)) {
								umipayCommonAccountArrayList.add(account);
							}
						}
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				}
			}
		}
		return umipayCommonAccountArrayList;
	}

	public synchronized UmipayCommonAccount getCommonAccountByPackageName(final String packageName,int accountType){

		String fileName = (accountType == COMMON_ACCOUNT)?COMMON_ACCOUNT_FILE:COMMON_ACCOUNT_TOCHANGE_FILE;
		UmipayCommonAccountData umipayCommonAccountData = readUmipayCommonAccountData(fileName);
		if(umipayCommonAccountData != null){
			JSONArray jsonArray = umipayCommonAccountData.getCommonAccountJSONArray();
			if(jsonArray != null){
				for (int i = 0; i < jsonArray.length(); i++) {
					try {
						String json = Basic_JSONUtil.getString(jsonArray,i,"");
						if(!TextUtils.isEmpty(json)) {
							UmipayCommonAccount account = new UmipayCommonAccount(json);
							if(checkDate(account) && packageName.equals(account.getDestPackageName())) {
								return  account;
							}
						}
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				}
			}
		}
		return null;
	}

	/**
	 * 一个包名对应的登录态永远只有一个
	 * 添加的是已验证的完整登录态，cache中读出的为未认证
	 * @param umipayCommonAccount
	 */
	public synchronized void addCommonAccount(UmipayCommonAccount umipayCommonAccount,int accountType){
		if(umipayCommonAccount == null || !checkDate(umipayCommonAccount)){
			return;
		}

		String fileName = (accountType == COMMON_ACCOUNT)?COMMON_ACCOUNT_FILE:COMMON_ACCOUNT_TOCHANGE_FILE;
		UmipayCommonAccountData umipayCommonAccountData = readUmipayCommonAccountData(fileName);
		if(umipayCommonAccountData != null){
			final String packageName = umipayCommonAccount.getDestPackageName();
			JSONArray jsonArray = umipayCommonAccountData.getCommonAccountJSONArray();
			if(jsonArray != null){
				for (int i = 0; i < jsonArray.length(); i++) {
					try {
						String json = Basic_JSONUtil.getString(jsonArray,i,"");
						if(!TextUtils.isEmpty(json)) {
							UmipayCommonAccount account = new UmipayCommonAccount(json);
							if(packageName.equals(account.getDestPackageName())) {
								jsonArray.put(i,null);
							}
						}
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				}
				try{
					jsonArray.put(new JSONObject(umipayCommonAccount.serialize()));
					umipayCommonAccountData.setCommonAccountJSONArray(jsonArray);
					saveUmipayCommonAccountData(umipayCommonAccountData,fileName);
				}catch (Throwable e){
					Debug_Log.e(e);
				}
			}
		}
		Debug_Log.dd("addCommonAccount");
	}

	//需要验证的是一般非切换区域的账号
	public synchronized  void valiDateCommonAccount(ArrayList validatedlist){
		if(validatedlist == null || validatedlist.isEmpty()){
			return;
		}
		UmipayCommonAccountData umipayCommonAccountData = readUmipayCommonAccountData(COMMON_ACCOUNT_FILE);
		if(umipayCommonAccountData != null){
			JSONArray jsonArray = umipayCommonAccountData.getCommonAccountJSONArray();
			if(jsonArray != null){
				for (int i = 0; i < jsonArray.length(); i++) {
					try {
						String json = Basic_JSONUtil.getString(jsonArray,i,"");
						if(!TextUtils.isEmpty(json)) {
							UmipayCommonAccount account = new UmipayCommonAccount(json);
							if(!validatedlist.contains(account.getUid())) {
								jsonArray.put(i,null);
							}
						}
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				}
				try{
					umipayCommonAccountData.setCommonAccountJSONArray(jsonArray);
					saveUmipayCommonAccountData(umipayCommonAccountData,COMMON_ACCOUNT_FILE);
				}catch (Throwable e){
					Debug_Log.e(e);
				}
			}
		}
		Debug_Log.dd("valiDateCommonAccount");
	}

	public synchronized  void removeCommonAccount(UmipayCommonAccount account,int accountType){
		if(account == null){
			return;
		}
		String fileName = (accountType == COMMON_ACCOUNT)?COMMON_ACCOUNT_FILE:COMMON_ACCOUNT_TOCHANGE_FILE;
		UmipayCommonAccountData umipayCommonAccountData = readUmipayCommonAccountData(fileName);
		if(umipayCommonAccountData != null){
			JSONArray jsonArray = umipayCommonAccountData.getCommonAccountJSONArray();
			if(jsonArray != null){
				for (int i = 0; i < jsonArray.length(); i++) {
					try {
						String json = Basic_JSONUtil.getString(jsonArray,i,"");
						if(!TextUtils.isEmpty(json)) {
							UmipayCommonAccount item = new UmipayCommonAccount(json);
							if(account.equals(item)) {
								jsonArray.put(i, null);
								break;
							}
						}
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				}
				try{
					umipayCommonAccountData.setCommonAccountJSONArray(jsonArray);
					saveUmipayCommonAccountData(umipayCommonAccountData,fileName);
				}catch (Throwable e){
					Debug_Log.e(e);
				}
			}
		}
		Debug_Log.dd("removeCommonAccount");
	}

	/**
	 * 获得其他app的登录态
	 * @return
	 */
	public UmipayCommonAccount popCommonAccountToChange(){
		UmipayCommonAccount account;
		try {
			String packName = mContext.getPackageName();
			 account = getCommonAccountByPackageName(packName,COMMON_ACCOUNT_TO_CHANGE);
			if(account!= null && !account.getDestPackageName().equals(account.getOriginPackageName())){
				removeCommonAccount(account,COMMON_ACCOUNT_TO_CHANGE);
				return account;
			}
		}catch (Throwable e){
			Debug_Log.e(e);
		}
		return null;
	}

	public class UmipayCommonAccountData implements Interface_Serializable {
		private static final String KEY_COMMON_ACCOUNT_CACHE = "slkjfdsakjlcommonaccount";
		private static final String TYPE_COMMON_ACCOUNT = "aa";
		private static final String TYPE_SIGN = "ab";
		private JSONArray mCommonAccountJSONArray;

		public JSONArray getCommonAccountJSONArray(){
			if(mCommonAccountJSONArray == null){
				mCommonAccountJSONArray = new JSONArray();
			}
			return mCommonAccountJSONArray;
		}
		public void setCommonAccountJSONArray(JSONArray commonAccountJSONArray){
			if(commonAccountJSONArray != null) {
				mCommonAccountJSONArray = commonAccountJSONArray;
			}
		}
		@Override
		public String serialize() {
			try {
				JSONObject jObject = new JSONObject();
				String cid = new Global_Runtime_ClientId(mContext).getCid();
				//本机信息签名，防止cache复制到其他机器上使用
				String sign = Coder_Md5.md5(cid);
				JSONArray jsonArray = new JSONArray();
				if(mCommonAccountJSONArray != null) {
					for (int i = 0; i < mCommonAccountJSONArray.length(); i++) {
						JSONObject item = Basic_JSONUtil.getJsonObject(mCommonAccountJSONArray,i,null);
						if (item != null) {
							jsonArray.put(item);
						}
					}
				}
				Basic_JSONUtil.put(jObject, TYPE_COMMON_ACCOUNT, jsonArray);
				Basic_JSONUtil.put(jObject, TYPE_SIGN, sign);
				return jObject.toString();
			}catch (Throwable e){
				Debug_Log.e(e);
			}

			return null;
		}

		@Override
		public boolean deserialize(String s) {
			try {
				JSONObject jO = Basic_JSONUtil.toJsonObject(s);
				String cid = new Global_Runtime_ClientId(mContext).getCid();
				String sign = Coder_Md5.md5(cid);
				String cache_sign =Basic_JSONUtil.getString(jO,TYPE_COMMON_ACCOUNT,"");
				if(TextUtils.isEmpty(sign) || sign.equals(cache_sign)) {
					//本机信息签名与cache签名不一致时不允许使用cache
					return false;
				}
				JSONArray jsonArray = Basic_JSONUtil.getJsonArray(jO,TYPE_COMMON_ACCOUNT,new JSONArray());
				mCommonAccountJSONArray = new JSONArray();
				for(int i=0;i<jsonArray.length();i++){
					Object item = jsonArray.get(i);
					if(item != null){
						mCommonAccountJSONArray.put(item);
					}
				}
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
			return KEY_COMMON_ACCOUNT_CACHE;
		}
	}
}