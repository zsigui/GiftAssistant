package net.ouwan.umipay.android.manager;

import android.content.Context;
import android.text.TextUtils;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.io.UmipaySDKDirectoryStorer;
import net.youmi.android.libs.platform.coder.Coder_SDKPswCoder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by liangpeixing on 14-2-25.
 */
public class ChannelManager {
	private static final String CHANNELS_FILE_NAME = ".4d44090845054c055a51570d500844";
	private static final String CHANNELS_PSW = "lqUzC16F";
	private static ChannelManager mInstance = null;
	private String mChannel = "0";
	private String mSubChannel = "0";
	private Context mContext = null;

	public ChannelManager(Context context) {
		this.mContext = context;
		getSDCardChannels();
	}

	public static ChannelManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ChannelManager(context);
		} else {
			mInstance.getSDCardChannels();
		}
		return mInstance;
	}

	public String getChannel() {
		return mChannel;
	}

	public void setChannel(String channel) {
		this.mChannel = channel;
	}

	public String getSubChannel() {
		return mSubChannel;
	}

	public void setSubChannel(String subChannel) {
		this.mSubChannel = subChannel;
	}

	public void getSDCardChannels() {
		UmipaySDKDirectoryStorer storer = UmipaySDKDirectoryStorer.getCacheFileStorer(mContext);
		if (storer.isFileExistInDirectory(CHANNELS_FILE_NAME)) {
			String[] channels = readChannels(storer.getFileByFileName(CHANNELS_FILE_NAME));
			if (!TextUtils.isEmpty(channels[0])) {
				this.mChannel = Coder_SDKPswCoder.decode(channels[0], CHANNELS_PSW);
			}
			if (!TextUtils.isEmpty(channels[1])) {
				this.mSubChannel = Coder_SDKPswCoder.decode(channels[1], CHANNELS_PSW);
			}
		}
	}

	public void saveChannels(final String channel, final String subChannel) {
		try {
			if (!channel.equals(mChannel) || !subChannel.equals(mSubChannel)) {
				UmipaySDKDirectoryStorer storer = UmipaySDKDirectoryStorer.getCacheFileStorer(mContext);
				storer.getFileByFileName(CHANNELS_FILE_NAME).createNewFile();
				File channelsFile = storer.getFileByFileName(CHANNELS_FILE_NAME);
				writeChannels(channelsFile, channel, subChannel);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	private void writeChannels(File file, String channel, String subChannel) {
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			fileWriter = new FileWriter(file, false);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(Coder_SDKPswCoder.encode(channel, CHANNELS_PSW));
			bufferedWriter.newLine();
			bufferedWriter.write(Coder_SDKPswCoder.encode(subChannel, CHANNELS_PSW));
			bufferedWriter.newLine();
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
		}
	}

	public void writeFileToSdcard(String fileName, String message) {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(fileName);
			byte[] bytes = message.getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Throwable e) {
			Debug_Log.e(e);
		} finally {
			try {
				if (null != fout) {
					fout.close();
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
	}

	public String[] readChannels(File file) {
		String res[] = new String[2];
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			for (int i = 0; i < 2; i++) {
				res[i] = bufferedReader.readLine();
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
		}
		return res;
	}
}
