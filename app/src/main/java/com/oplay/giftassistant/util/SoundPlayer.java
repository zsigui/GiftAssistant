package com.oplay.giftassistant.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public class SoundPlayer {

    private static SoundPlayer sInstance;
    private SoundPool mSoundPool;
    private int mDownloadSoundId;

    private SoundPlayer(Context context) {
        if (Build.VERSION.SDK_INT < 21) {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        } else {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSoundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(1)
                    .build();
        }
        mDownloadSoundId = mSoundPool.load(context, R.raw.download_complete, 0);
    }

    public synchronized static SoundPlayer getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SoundPlayer(context);
        }
        return sInstance;
    }

    public void playDownloadComplete() {
        if (mSoundPool != null) {
            final int id = mSoundPool.play(mDownloadSoundId, 0.5f, 0.5f, 0, 0, 1.0f);
            if (AppDebugConfig.IS_DEBUG) {
                AppDebugConfig.logMethodWithParams(this, id);
            }
        }
    }

    public void release() {
        sInstance = null;
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }
    }
}
