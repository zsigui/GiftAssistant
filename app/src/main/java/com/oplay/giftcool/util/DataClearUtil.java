package com.oplay.giftcool.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import net.youmi.android.libs.common.util.Util_System_File;

import java.io.File;
import java.math.BigDecimal;

/**
 * Created by zsigui on 16-1-6.
 */
public class DataClearUtil {

    /**
     * 清除本应用SharedPreference(/data/data/com.xxx.xxx/shared_prefs)
     */
    public static void cleanSharedPreference(Context context) {
        Util_System_File.delete(new File(String.format("%s%s%s", context.getFilesDir().getPath(),
                context.getPackageName(), "/shared_prefs")));
    }

    /**
     * 清除本应用所有数据库(/data/data/com.xxx.xxx/databases)
     */
    public static void cleanDatabase(Context context) {
        Util_System_File.delete(new File(String.format("%s%s%s", context.getFilesDir().getPath(),
                context.getPackageName(), "/databases")));
    }

    /**
     * 清除内部缓存文件夹
     */
    public static void cleanInternalCache(Context context) {
        Util_System_File.delete(context.getCacheDir());
    }

    /**
     * 清除外部缓存文件夹
     */
    public static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Util_System_File.delete(context.getExternalCacheDir());
        }
    }

    /**
     * 清除/data/data/com.xxx.xxx/files下的内容
     */
    public static void cleanFiles(Context context) {
        Util_System_File.delete(context.getFilesDir());
    }

    /**
     * 清除自定义路径下的文件，使用需小心，请不要误删。而且只支持目录下的文件删除
     */
    public static void cleanCustomCache(String filePath) {
        cleanCustomCache(new File(filePath), null);
    }

    /**
     * 清除自定义路径下的文件或者文件夹
     *
     * @param file 指定路径文件
     * @param suffix 当文件后缀匹配该值或者该值为空时，表示删除该文件
     */
    public static void cleanCustomCache(File file, String suffix) {
        if (file != null) {
            if (file.isDirectory()) {
                for (File subFile : file.listFiles()) {
                    cleanCustomCache(subFile, suffix);
                }
            } else {
                if (TextUtils.isEmpty(suffix) || file.getName().endsWith(suffix)) {
                    Util_System_File.delete(file);
                }
            }
        }
    }

    /**
     * 递归获取文件夹大小，单位:byte
     */
    public static long getFolderSize(File file) {
        return getFolderSize(file, null);
    }

    /**
     * 递归获取文件夹内特定文件名后缀的文件大小总和，当后缀为空，等同于{@link #getFolderSize(File)}，单位:byte
     */
    public static long getFolderSize(File file, String suffix) {
        long size = 0;
        try {
            if (file != null) {
                if (file.isDirectory()) {
                    for (File aFileList : file.listFiles()) {
                        // 如果下面还有文件
                        size = size + getFolderSize(aFileList);
                    }
                } else {
                    if (TextUtils.isEmpty(suffix) || file.getName().endsWith(suffix)) {
                        size = size + file.length();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 获取格式化大小
     *
     * @param size 长度，单位:byte
     * @return
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }
}
