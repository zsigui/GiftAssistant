package com.oplay.giftassistant.util.encrypt;

import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.model.DecryptDataModel;
import com.socks.library.KLog;

import net.ymfx.android.d.aa;
import net.ymfx.android.d.ab;
import net.ymfx.android.d.bb;

import java.util.Arrays;

/**
 * Created by zsigui on 15-12-21.
 */
public class DataEncrypt {

	static {
		System.loadLibrary("ymfx");
	}

	private static DataEncrypt sInstance;
	private DecryptDataModel mData = new DecryptDataModel();

	public static DataEncrypt getInstance() {
		synchronized (Object.class) {
			if (sInstance == null) {
				sInstance = new DataEncrypt();
			}
		}
		return sInstance;
	}

	/**
	 *
	 * 执行网络操作前对数据进行加密
	 *
	 * @param data 待加密内容
	 * @return 加密结果
	 */
	public byte[] encrypt(String data) {
		byte[] result = null;
		bb encryptData = null;
		try {
			encryptData = ab.p(mData.getUid(), mData.getSdkVer(), mData.getPlatform(), mData.getAppkey(),
					mData.getAppSecret(), mData.getSession(), data, mData.getCmd());
		} catch (Exception e) {
			KLog.e(e);
		}
		if (encryptData == null) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.i(AppDebugConfig.TAG_ENCRYPT, "packData = " + data);
				KLog.i(AppDebugConfig.TAG_ENCRYPT, "packData failed, get null");
			}
		} else if (encryptData.getA() != 0) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.i(AppDebugConfig.TAG_ENCRYPT, "packData = " + data);
				KLog.i(AppDebugConfig.TAG_ENCRYPT, "packData failed");
			}
		} else {
			result = encryptData.getB();
		}
		return result;
	}


	/**
	 *
	 * 对网络请求返回的数据进行解密
	 *
	 * @param data 待解密字节数组
	 * @return 解密结果
	 */
	public String decrypt(byte[] data) {
		String result = null;
		aa decryptData = null;
		try {
			decryptData = ab.up(mData.getUid(), mData.getSdkVer(), mData.getPlatform(), mData.getAppkey(),
					mData.getAppSecret(), mData.getSession(), data, mData.getCmd());
		} catch (Exception e) {
			KLog.e(e);
		}
		if (decryptData == null) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.i(AppDebugConfig.TAG_ENCRYPT, "unpackData = " + Arrays.toString(data));
				KLog.i(AppDebugConfig.TAG_ENCRYPT, "unpackData failed, get null");
			}
		} else if (decryptData.getA() != 0) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.i(AppDebugConfig.TAG_ENCRYPT, "packData = " + Arrays.toString(data));
				KLog.i(AppDebugConfig.TAG_ENCRYPT, "packData failed");
			}
		} else {
			result = decryptData.getB();
		}
		return result;
	}

	public void setData(DecryptDataModel data) {
		mData = data;
	}
}
