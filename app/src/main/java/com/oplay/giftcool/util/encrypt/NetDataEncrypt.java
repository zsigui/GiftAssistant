package com.oplay.giftcool.util.encrypt;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.model.DecryptDataModel;

import net.ymfx.android.d.aa;
import net.ymfx.android.d.ab;
import net.ymfx.android.d.bb;

import java.util.Arrays;

/**
 * Created by zsigui on 15-12-21.
 */
public class NetDataEncrypt {

    static {
        System.loadLibrary("ymfx");
    }

    private static NetDataEncrypt sInstance;
    private DecryptDataModel mData = new DecryptDataModel();

    public static NetDataEncrypt getInstance() {
        synchronized (Object.class) {
            if (sInstance == null) {
                sInstance = new NetDataEncrypt();
            }
        }
        return sInstance;
    }

    public void initDecryptDataModel(int uid, String session) {
        if (mData == null) {
            mData = new DecryptDataModel();
        }
        mData.setSession(session);
        mData.setUid(uid);
    }

    /**
     * 执行网络操作前对数据进行加密
     *
     * @param data 待加密内容
     * @param data 命令字
     * @return 加密结果
     */
    public byte[] encrypt(String data, int cmd) {
        byte[] result = null;
        bb encryptData = null;
        try {
            encryptData = ab.p(mData.getUid(), mData.getSdkVer(), mData.getPlatform(), mData.getAppKey(),
                    mData.getAppSecret(), mData.getSession(), data, cmd);
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, e);
        }
        if (encryptData == null) {
            AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, "packData = " + data + "\npackData failed, get null");
        } else if (encryptData.getA() != 0) {
            AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, "packData = " + data + "\npackData failed");
        } else {
            result = encryptData.getB();
        }
        return result;
    }


    /**
     * 对网络请求返回的数据进行解密
     *
     * @param data 待解密字节数组
     * @param cmd  命令字
     * @return 解密结果
     */
    public String decrypt(byte[] data, int cmd) {
        String result = null;
        aa decryptData = null;
        try {
            decryptData = ab.up(mData.getUid(), mData.getSdkVer(), mData.getPlatform(), mData.getAppKey(),
                    mData.getAppSecret(), mData.getSession(), data, cmd);
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_UTIL, e);
        }
        if (decryptData == null) {
            AppDebugConfig.d(AppDebugConfig.TAG_ENCRYPT, "unpackData = " + Arrays.toString(data));
            AppDebugConfig.d(AppDebugConfig.TAG_ENCRYPT, "unpackData failed, get null");
        } else if (decryptData.getA() != 0) {
            AppDebugConfig.d(AppDebugConfig.TAG_ENCRYPT, "packData = " + Arrays.toString(data));
            AppDebugConfig.d(AppDebugConfig.TAG_ENCRYPT, "packData failed");
        } else {
            result = decryptData.getB();
        }
        return result;
    }
}
