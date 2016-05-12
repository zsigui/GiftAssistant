package com.oplay.giftcool.util;

import android.content.Context;
import android.text.TextUtils;

import com.nostra13.universalimageloader.utils.StorageUtils;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.listener.CallbackListener;
import com.socks.library.KLog;

import net.youmi.android.libs.common.coder.Coder_Md5;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/4/3
 */
public class FileUtil {

    // 定义512MB为大小文件分隔
    private static final int BIG_FILE_COUNT = 1024;

    public static final char EXTENSION_SEPARATOR = '.';

    private static final char UNIX_SEPARATOR = '/';

    private static final char WINDOWS_SEPARATOR = '\\';
    public static final String DEFAULT_CHASET = "UTF-8";


    /* ---- 自定义网络请求数据缓存读写 Start ---- */

    /**
     * 根据Key的MD5值获取对应页面的缓存数据
     */
    public static <T> void readCacheByKey(final Context context, final String key,
                                          final CallbackListener<T> callback, final Type type) {
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                T data = null;
                try {
                    String realKey = Coder_Md5.md5(key);
                    File storeFile = new File(StorageUtils.getOwnCacheDirectory(context, Global.NET_CACHE_PATH, true),
                            realKey + ".cache");
                    if (storeFile.exists() && storeFile.isFile()) {
                        String d = readString(storeFile, DEFAULT_CHASET);
                        if (AppDebugConfig.IS_DEBUG) {
                            KLog.d(AppDebugConfig.TAG_UTIL, "FileUtil.readCacheByKey : read d = " + d);
                        }
                        if (!TextUtils.isEmpty(d)) {
                            data = AssistantApp.getInstance().getGson().fromJson(d, type);
                        }
                    }
                } catch (Throwable t) {
                    if (AppDebugConfig.IS_DEBUG) {
                        KLog.d(AppDebugConfig.TAG_UTIL, "FileUtil.error = " + t.getMessage());
                    }
                } finally {
                    if (callback != null) {
                        final T result = data;
                        ThreadUtil.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.doCallBack(result);
                            }
                        });
                    }
                }

            }
        });
    }

    /**
     * 写入缓存数据到指定Key的MD5为文件名的文件中
     */
    public static <T> void writeCacheByKey(final Context context, final String key, final T data) {
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String realKey = Coder_Md5.md5(key);
                    File storeFile = new File(StorageUtils.getOwnCacheDirectory(context, Global.NET_CACHE_PATH, true),
                            realKey + ".cache");
                    String content = AssistantApp.getInstance().getGson().toJson(data);
                    writeString(storeFile, content, DEFAULT_CHASET, false);
                    if (AppDebugConfig.IS_DEBUG) {
                        KLog.d(AppDebugConfig.TAG_UTIL, "FileUtil.writeCacheByKey : write content success!");
                    }
                } catch (Throwable t) {
                    if (AppDebugConfig.IS_DEBUG) {
                        t.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 读取文件后返回接口
     */
    public interface ReadFileCallback<T> {
        void response(T data);
    }

    /* ---- 自定义网络请求数据缓存读写 End ---- */

    public static void forceMkdir(File directory) throws IOException {
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                String message =
                        "File "
                                + directory
                                + " exists and is "
                                + "not a directory. Unable to create directory.";
                throw new IOException(message);
            }
        } else {
            if (!directory.mkdirs()) {
                // Double-check that some other thread or process hasn't made
                // the directory in the background
                if (!directory.isDirectory()) {
                    String message =
                            "Unable to create directory " + directory;
                    throw new IOException(message);
                }
            }
        }
    }

    public static boolean mkdirs(File directory) {
        try {
            forceMkdir(directory);
            return true;
        } catch (IOException e){
        }
        return false;
    }

    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = indexOfLastSeparator(filename);
        return lastSeparator > extensionPos ? -1 : extensionPos;
    }

    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    /**
     * 写入字节数组数据到指定输出流中(该方法会创建新文件)
     *
     * @param out
     * @param data
     */
    public static void writeBytes(OutputStream out, byte[] data) {
        if (out == null) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.d(AppDebugConfig.TAG_UTIL, "FileUtil.writeBytes : params out(OutputStream) is null");
            }
        }
        if (data == null) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.d(AppDebugConfig.TAG_UTIL, "FileUtil.writeBytes : params data(byte[]) is null");
            }
        }

        if (out instanceof FileOutputStream) {
            // 通道方式写入
            FileChannel outChannel = null;
            try {
                outChannel = ((FileOutputStream) out).getChannel();
                outChannel.write(ByteBuffer.wrap(data));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeIO(outChannel);
            }
        } else {
            // 普通IO写入
            IOWriteBytes(out, data);
        }
    }

    /**
     * 从文件中读取指定编码格式的文本字符串
     *
     * @param file
     * @param charset
     * @return
     */
    public static String readString(File file, String charset) {
        String result = null;
        try {
            result = new String(readBytes(file), charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写入系统默认编码的文本字符串到文件中(该方法会创建新文件)
     *
     * @param file
     * @param data
     */
    public static void writeString(File file, String data, String charset, boolean append) {
        try {
            writeBytes(file, data.getBytes(charset), append);
        } catch (UnsupportedEncodingException e) {
            if (AppDebugConfig.IS_DEBUG) {
                AppDebugConfig.warn(AppDebugConfig.TAG_UTIL, e);
            }
        }
    }

    /**
     * 写入字节数组数据到输出流中,该操作完成后会关闭流
     */
    public static void IOWriteBytes(OutputStream out, byte[] data) {
        BufferedOutputStream bout = (out instanceof BufferedOutputStream) ? (BufferedOutputStream) out : new
                BufferedOutputStream(out);
        try {
            bout.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(bout, out);
        }
    }

    /**
     * 写入字节数组数据到指定文件中(该方法会创建新文件)
     *
     * @param file
     * @param data
     */
    public static void writeBytes(File file, byte[] data, boolean append) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(file, append);
            writeBytes(fout, data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeIO(fout);
        }
    }

    /**
     * 从文件中读取数据存放到字节数组中
     *
     * @param file
     * @return
     */
    public static byte[] readBytes(File file) {
        byte[] result = null;
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            result = IOReadBytes(fin);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(fin);
        }
        return result;
    }


    /**
     * 从输入流中读取字节数组,该操作完成后会关闭流
     */
    public static byte[] IOReadBytes(InputStream in) {
        byte[] result = null;
        BufferedInputStream bin = (in instanceof BufferedInputStream) ? (BufferedInputStream) in : new
                BufferedInputStream(in);
        byte[] tempBuf = null;
        try {
            tempBuf = new byte[1024];
            int length;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(bin.available());
            while ((length = bin.read(tempBuf)) != -1) {
                baos.write(tempBuf, 0, length);
            }
            baos.flush();
            result = baos.toByteArray();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(bin, in);
        }
        return result;
    }

    /**
     * 关闭IO流
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null || closeables.length <= 0) {
            return;
        }
        for (Closeable c : closeables) {
            if (c == null) {
                continue;
            }
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
