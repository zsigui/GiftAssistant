package net.ouwan.umipay.android.Utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.coder.Coder_Md5;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Util_Loadlib
 * Created by liangpeixing on 5/26/15.
 */
public class Util_Loadlib {
    public static boolean loadlib(Context context, String libName) {

        Queue<String> existQueue = new PriorityQueue<String>(4, new MyComparator());
        Queue<String> copytQueue = new PriorityQueue<String>(4, new MyComparator());
        //先尝试加载已经存在的so库,校验并尝试加载
        existQueue.add("armeabi");
        existQueue.add("armeabi-v7a");
	    existQueue.add("arm64-v8a");
        existQueue.add("x86");
        existQueue.add("mips");

        while (existQueue.size() != 0) {
            String cpuType = existQueue.poll();
            String libPath = "/data/data/" + context.getPackageName() + "/files/" + cpuType + "_" + libName;
            File localFile = new File(libPath);
            if (localFile.exists()) {
                try {
                    if(isLocalLibValid(context,localFile,libName,cpuType)) {
                        System.load(libPath);
                        return true;
                    }
                } catch (Throwable e) {
                    Debug_Log.e(e);
                }
            }
        }

        //不存在时，从assets中复制、改名并尝试加载
        copytQueue.add("armeabi");
        copytQueue.add("armeabi-v7a");
	    copytQueue.add("arm64-v8a");
        copytQueue.add("x86");
        copytQueue.add("mips");

        while (copytQueue.size() != 0) {
            String cpuType = copytQueue.poll();
            String libPath = "/data/data/" + context.getPackageName() + "/files/" + cpuType + "_" + libName;
            if (copyAndLoadLib(context, libName, cpuType, libPath)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLocalLibValid(Context context,File file,String libName, String cpuType){
        boolean isValid = false;
        String libAssetsPath = "so" + File.separator + cpuType + File.separator + libName;
            InputStream localInputStream = null;
            try {
                localInputStream = context.getAssets().open(libAssetsPath);
            } catch (Throwable e) {
                Debug_Log.v(e);
            }
            if (localInputStream == null) {
                return isValid;
            }

            byte[] arrayOfByte = null;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                int i;
                while ((i = localInputStream.read()) != -1) {
                    byteArrayOutputStream.write(i);
                }
                arrayOfByte = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
            } catch (Throwable e) {
                Debug_Log.e(e);
            } finally {
                try {
                    localInputStream.close();
                    byteArrayOutputStream.close();
                } catch (Throwable ignore) {
                }
            }
            if (arrayOfByte == null || arrayOfByte.length <= 0) {
                return isValid;
            }

        if(file != null && file.exists()){
            String cacheFileMd5 = Coder_Md5.getMD5SUM(file);
            String assetsFileMd5 = Coder_Md5.md5(arrayOfByte);
            Debug_Log.dd("cacheLib MD5 = " + cacheFileMd5);
            Debug_Log.dd("assetsLib MD5 = " + assetsFileMd5);
            if (!TextUtils.isEmpty(cacheFileMd5) && !TextUtils.isEmpty(assetsFileMd5) && cacheFileMd5.equals
                    (assetsFileMd5)) {
                isValid = true;
            }
        }
        return isValid;
    }

    private static boolean copyAndLoadLib(Context context, String libName, String cpuType, String libPath) {
        String libAssetsPath = "so" + File.separator + cpuType + File.separator + libName;
        //复制动态库
        FileOutputStream localFileOutputStream = null;
        try {
            //必须改名否则失败后无法再次加载
            localFileOutputStream = context.openFileOutput(cpuType + "_" + libName, 0);
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
        if (localFileOutputStream == null) {
            return false;
        }

        InputStream localInputStream = null;
        try {
            localInputStream = context.getAssets().open(libAssetsPath);
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
        if (localInputStream == null) {
            return false;
        }

        byte[] arrayOfByte = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            int i;
            while ((i = localInputStream.read()) != -1) {
                byteArrayOutputStream.write(i);
            }
            arrayOfByte = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
        } catch (Throwable e) {
            Debug_Log.e(e);
        } finally {
            try {
                localInputStream.close();
                byteArrayOutputStream.close();
            } catch (Throwable ignore) {
            }
        }
        if (arrayOfByte == null || arrayOfByte.length <= 0) {
            return false;
        }

        try {
            localFileOutputStream.write(arrayOfByte);
        } catch (Throwable e) {
            Debug_Log.e(e);
            return false;
        } finally {
            try {
                localFileOutputStream.close();
            } catch (Throwable ignore) {
            }
        }

        //动态库复制完毕，尝试加载
        try {
            System.load(libPath);
            return true;
        } catch (Throwable e) {
            Debug_Log.e(e);
            //加载失败则删除该库
            try {
                File fp = new File(libPath);
                if (fp.exists()) {
                    fp.delete();
                }
            } catch (Throwable e2) {
                Debug_Log.e(e2);
            }
        }
        return false;
    }

    static class MyComparator implements Comparator<String> {
        @Override
        public int compare(String s, String s2) {
            if (s2.equals(Build.CPU_ABI)) {
                return 1;
            } else if (s.equals(Build.CPU_ABI)) {
                return -1;
            }else if (s2.equals("armeabi") && Build.CPU_ABI.contains("armeabi")) {
                //修复部分android2.3手机加载armeabi-v7a,mips直接闪退，不抛出异常的问题（如 CPU_ABI == armeabi-v6l时优先加载armeabi）
                return 1;
            } else if (s.equals("armeabi") && Build.CPU_ABI.contains("armeabi")) {
                return -1;
            }else if (s2.contains("x86")) {
                return 1;
            } else if (s.contains("x86")) {
                return -1;
            }
            return 0;
        }
    }

}
