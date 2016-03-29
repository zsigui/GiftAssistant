package com.oplay.giftcool.util;

import android.content.Context;
import android.text.TextUtils;

import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.SPConfig;
import com.socks.library.KLog;

import net.youmi.android.libs.common.coder.Base64;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 读取META-INF得到的空白文件名来获得渠道信息
 *
 * Created by zsigui on 15-12-24.
 */
public class ChannelUtil {

	/**
	 * 获取通过Base64加密的channel id
	 *
	 * @param context
	 * @return
	 */
	public static int getChannelId(Context context) {

		if (AppConfig.TEST_MODE) {
			return AppDebugConfig.TEST_CHANNEL_ID;
		}

		// 从SP中获取
		int chnInt = SPUtil.getInt(context, SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_CHANNEL, 0);
		if (chnInt != 0) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_CHANNEL, "GET CHANNEL FROM SP : " + chnInt);
			}
			return chnInt;
		}

		ZipFile zipFile = null;
		try {
			long time = System.currentTimeMillis();
			final String path = context.getApplicationContext().getPackageCodePath();
			zipFile = new ZipFile(path);
			String channelFileName = "";
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while (zipEntries.hasMoreElements()) {
				ZipEntry entry = zipEntries.nextElement();
				String entryName = entry.getName();
				if (entryName.endsWith(Global.CHANNEL_FILE_NAME_SUFFIX)) {
					if (AppDebugConfig.IS_DEBUG) {
						KLog.e(AppDebugConfig.TAG_CHANNEL, "CHANNEL_FILE_ENTRY:" + entryName);
					}
					channelFileName = entryName;
					break;
				}
			}
			if (!TextUtils.isEmpty(channelFileName)) {
				int index = channelFileName.indexOf("/");
				int start = 0;
				if (index != -1) {
					start = index + 1;
				}
				int end = channelFileName.indexOf(Global.CHANNEL_FILE_NAME_SUFFIX);
				String channelMisc = channelFileName.substring(start, end);
				String channel = new String(Base64.decode(channelMisc, Base64.NO_OPTIONS), "UTF-8");

				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_CHANNEL, "NAME:" + channelFileName + " MISC:"
							+ channelMisc + " DECODED:" + channel +
							" time:" + (System.currentTimeMillis() - time));
				}

				// replaceall non-printable character
				channel = channel.replaceAll("[\\p{C}\\p{Z}]", "");

				chnInt = Integer.valueOf(channel);
				SPUtil.putInt(context, SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_CHANNEL, chnInt);
				return chnInt;
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_UTIL, e);
			}
		} finally {
			try {
				if (zipFile != null) {
					zipFile.close();
				}
			} catch (IOException e) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(e);
				}
			}
		}
		return 0;
	}
}
