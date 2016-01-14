package com.oplay.giftassistant.util;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.oplay.giftassistant.config.AppDebugConfig;
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
	public static final String KEY_CHANNEL = "l3yn45";

	/**
	 * 获取通过Base64加密的channel id
	 *
	 * @param context
	 * @param fileNameSuffix channel file name suffix
	 * @return
	 */
	public static int getChannelId(Context context, String fileNameSuffix) {

		final int chn = PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_CHANNEL, -1);
		if (chn != -1) {
			return chn;
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
				if (entryName.endsWith(fileNameSuffix)) {
					if (AppDebugConfig.IS_DEBUG) {
						KLog.e("CHANNEL", "CHANNEL_FILE_ENTRY:" + entryName);
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
				int end = channelFileName.indexOf(fileNameSuffix);
				String channelMisc = channelFileName.substring(start, end);
				String channel = new String(Base64.decode(channelMisc, Base64.NO_OPTIONS), "UTF-8");

				if (AppDebugConfig.IS_DEBUG) {
					KLog.e("CHANNEL", "NAME:" + channelFileName + " MISC:" + channelMisc + " DECODED:" + channel +
							" time:" + (System.currentTimeMillis() - time));
				}

				// replaceall non-printable character
				channel = channel.replaceAll("[\\p{C}\\p{Z}]", "");

				final int chnInt = Integer.valueOf(channel);
				PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_CHANNEL, chnInt).commit();
				return chnInt;
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
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