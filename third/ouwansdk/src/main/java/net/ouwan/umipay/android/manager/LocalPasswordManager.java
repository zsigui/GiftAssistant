package net.ouwan.umipay.android.manager;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import net.ouwan.umipay.android.Utils.Util_FileHelper;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.coder.Coder_Base64;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.global.Global_Runtime_SystemInfo;
import net.youmi.android.libs.platform.coder.Coder_SDKPswCoder;

import org.json.JSONObject;

import java.io.File;

/**
 * LocalPasswordmanager
 *
 * @author zacklpx
 *         date 15-3-5
 *         description
 */
public class LocalPasswordManager {
	public static final String LOCAL_PSW_FILE = File.separator + ".umipay" + File.separator + "local.um";
	private static final String KEY_SEED = "jca25d1e1asd85";

	private static LocalPasswordManager mInstance;
	private Context mContext;

	private LocalPasswordManager(Context context) {
		this.mContext = context;
	}

	public synchronized static LocalPasswordManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new LocalPasswordManager(context);
		}
		return mInstance;
	}

	private static String readLocalFile(Context context) {
		try {
			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + LOCAL_PSW_FILE);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
				return null;
			}
			return Util_FileHelper.readFileSdcard(file.getAbsolutePath());
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	private static void saveLocalFile(Context context, String value) {
		try {
			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + LOCAL_PSW_FILE);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			Util_FileHelper.writeFileSdcard(file.getAbsolutePath(), value);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	private String generateKey(String value) {
		try {
			String imei = Global_Runtime_SystemInfo.getImei(mContext);
			String androidid = Global_Runtime_SystemInfo.getAndroidId(mContext);
			String source = imei + androidid + value;
			String key = Coder_Md5.md5(source).substring(0, 10);
			return key;
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	private String encodePassword(String password) {
		try {
			String key = generateKey(KEY_SEED);
			String enc_psw = Coder_SDKPswCoder.encode(password, key);
			return enc_psw;
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	private String decodePassword(String enc_psw) {
		try {
			String Key = generateKey(KEY_SEED);
			String psw_ori = Coder_SDKPswCoder.decode(enc_psw, Key);
			return psw_ori;
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	public String getPassword(String username) {
		try {
			String base64_file = readLocalFile(mContext);
			String json_src_str = Coder_Base64.decode(base64_file);
			JSONObject jsonObject = Basic_JSONUtil.toJsonObject(json_src_str);
			String key = generateKey(username);
			if (TextUtils.isEmpty(key)) {
				return null;
			}
			String enc_psw = Basic_JSONUtil.getString(jsonObject, key, "");
			return decodePassword(enc_psw);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	public void putPassword(String username, String password) {
		try {
			String base64_file = readLocalFile(mContext);
			JSONObject jsonObject = new JSONObject();
			if (!TextUtils.isEmpty(base64_file)) {
				try {
					String json_src_str = Coder_Base64.decode(base64_file);
					jsonObject = new JSONObject(json_src_str);
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
			}
			//对用户名md5
			String key = generateKey(username);

			//移除旧的用户密码，更新新的密码
			jsonObject.remove(key);

			String enc_psw = encodePassword(password);
			Basic_JSONUtil.put(jsonObject, key, enc_psw);

			String json_str = jsonObject.toString();
			String base64 = Coder_Base64.encode(json_str);
			saveLocalFile(mContext, base64);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	public void removePassword(String username) {
		try {
			String base64_file = readLocalFile(mContext);
			JSONObject jsonObject = new JSONObject();
			if (!TextUtils.isEmpty(base64_file)) {
				try {
					String json_src_str = Coder_Base64.decode(base64_file);
					jsonObject = new JSONObject(json_src_str);
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
			}
			//对用户名md5
			String key = generateKey(username);

			//移除旧的用户密码，更新新的密码
			jsonObject.remove(key);
			String json_str = jsonObject.toString();
			String base64 = Coder_Base64.encode(json_str);
			saveLocalFile(mContext, base64);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}


}
