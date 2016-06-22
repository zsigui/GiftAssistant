/*
 * Copyright (C) 2014 pengjianbo(pengjianbosoft@gmail.com), Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cn.finalteam.galleryfinal;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;

import net.youmi.android.libs.common.util.Util_System_File;
import net.youmi.android.libs.common.util.Util_System_SDCard_Util;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.finalteam.galleryfinal.model.PhotoInfo;
import cn.finalteam.galleryfinal.utils.Utils;

/**
 * Desction:
 * Author:pengjianbo
 * Date:15/12/2 上午11:05
 */
public class GalleryFinal {
    static final String PHOTO_LIST = "photo_list";
    static final String PHOTO_INDEX = "photo_index";
    static final String PHOTO_SHOW_TITLE = "photo_show_title";
    static final String PHOTO_SHOW_TAB = "photo_show_tab";

    static final int TAKE_REQUEST_CODE = 1001;

    static final int PERMISSIONS_CODE_GALLERY = 2001;

    private static FunctionConfig mCurrentFunctionConfig;
    private static FunctionConfig mGlobalFunctionConfig;
    private static ThemeConfig mThemeConfig;
    private static CoreConfig mCoreConfig;

    private static OnHandlerResultCallback mCallback;
    private static int mRequestCode;

    public static void init(CoreConfig coreConfig) {
        mThemeConfig = coreConfig.getThemeConfig();
        mCoreConfig = coreConfig;
        mGlobalFunctionConfig = coreConfig.getFunctionConfig();
    }

    public static FunctionConfig copyGlobalFuncationConfig() {
        if (mGlobalFunctionConfig != null) {
            return mGlobalFunctionConfig.clone();
        }
        return null;
    }

    public static CoreConfig getCoreConfig() {
        return mCoreConfig;
    }

    public static FunctionConfig getFunctionConfig() {
        return mCurrentFunctionConfig;
    }

    public static ThemeConfig getGalleryTheme() {
        if (mThemeConfig == null) {
            //使用默认配置
            mThemeConfig = ThemeConfig.DEFAULT;
        }
        return mThemeConfig;
    }

    public static boolean isInit() {
        return mThemeConfig != null && mCoreConfig != null && mGlobalFunctionConfig != null;
    }

    /**
     * 打开Gallery-单选
     *
     * @param requestCode
     * @param callback
     */
    public static void openGallerySingle(int requestCode, OnHandlerResultCallback callback) {
        FunctionConfig config = copyGlobalFuncationConfig();
        if (config != null) {
            openGallerySingle(requestCode, config, callback);
        } else {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            AppDebugConfig.e(AppDebugConfig.TAG_GALLERY, "FunctionConfig null");
        }
    }

    /**
     * 打开Gallery-单选
     *
     * @param requestCode
     * @param config
     * @param callback
     */
    public static void openGallerySingle(int requestCode, FunctionConfig config, OnHandlerResultCallback callback) {
        if (mCoreConfig.getImageLoader() == null) {
            AppDebugConfig.e(AppDebugConfig.TAG_GALLERY, "Please init GalleryFinal.");
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            return;
        }

        if (config == null && mGlobalFunctionConfig == null) {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            return;
        }

        if (!Util_System_SDCard_Util.IsSdCardCanRead()) {
            Toast.makeText(mCoreConfig.getContext(), R.string.empty_sdcard, Toast.LENGTH_SHORT).show();
            return;
        }
        config.mutiSelect = false;
        mRequestCode = requestCode;
        mCallback = callback;
        mCurrentFunctionConfig = config;

        Intent intent = new Intent(mCoreConfig.getContext(), PhotoSelectActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mCoreConfig.getContext().startActivity(intent);
    }

    /**
     * 打开Gallery-
     *
     * @param requestCode
     * @param maxSize
     * @param callback
     */
    public static void openGalleryMulti(int requestCode, int maxSize, OnHandlerResultCallback callback) {
        FunctionConfig config = copyGlobalFuncationConfig();
        if (config != null) {
            config.maxSize = maxSize;
            openGalleryMulti(requestCode, null, config, callback);
        } else {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            AppDebugConfig.e(AppDebugConfig.TAG_GALLERY, "Please init GalleryFinal.");
        }
    }

    /**
     * 打开Gallery-
     *
     * @param requestCode
     * @param maxSize
     * @param callback
     */
    public static void openGalleryMulti(int requestCode, ArrayList<PhotoInfo> hasSelected,
                                        int maxSize, OnHandlerResultCallback callback) {
        FunctionConfig config = copyGlobalFuncationConfig();
        if (config != null) {
            config.maxSize = maxSize;
            openGalleryMulti(requestCode, hasSelected, config, callback);
        } else {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            AppDebugConfig.e(AppDebugConfig.TAG_GALLERY, "Please init GalleryFinal.");
        }
    }

    /**
     * 打开Gallery-多选
     *
     * @param requestCode
     * @param config
     * @param callback
     */
    public static void openGalleryMulti(int requestCode, ArrayList<PhotoInfo> hasSelected,
                                        FunctionConfig config, OnHandlerResultCallback
                                                callback) {
        if (mCoreConfig.getImageLoader() == null) {
            AppDebugConfig.e(AppDebugConfig.TAG_GALLERY, "Please init GalleryFinal.");
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            return;
        }

        if (config == null && mGlobalFunctionConfig == null) {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            return;
        }

        if (config.getMaxSize() <= 0) {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.maxsize_zero_tip));
            }
            return;
        }

        if (config.getSelectedList() != null && config.getSelectedList().size() > config.getMaxSize()) {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.select_max_tips));
            }
            return;
        }

        if (!Util_System_SDCard_Util.IsSdCardCanRead()) {
            Toast.makeText(mCoreConfig.getContext(), R.string.empty_sdcard, Toast.LENGTH_SHORT).show();
            return;
        }

        mRequestCode = requestCode;
        mCallback = callback;
        mCurrentFunctionConfig = config;

        config.mutiSelect = true;

        Intent intent = new Intent(mCoreConfig.getContext(), PhotoSelectActivity.class);
        intent.putExtra(GalleryFinal.PHOTO_LIST, (Serializable) hasSelected);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mCoreConfig.getContext().startActivity(intent);
    }


    /**
     * 打开相机
     *
     * @param requestCode
     * @param callback
     */
    public static void openCamera(int requestCode, OnHandlerResultCallback callback) {
        FunctionConfig config = copyGlobalFuncationConfig();
        if (config != null) {
            openCamera(requestCode, config, callback);
        } else {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            AppDebugConfig.e(AppDebugConfig.TAG_GALLERY, "Please init GalleryFinal.");
        }
    }

    /**
     * 打开相机
     *
     * @param config
     * @param callback
     */
    public static void openCamera(int requestCode, FunctionConfig config, OnHandlerResultCallback callback) {
        if (mCoreConfig.getImageLoader() == null) {
            AppDebugConfig.e(AppDebugConfig.TAG_GALLERY, "Please init GalleryFinal.");
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            return;
        }

        if (config == null && mGlobalFunctionConfig == null) {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            return;
        }

        if (!Util_System_SDCard_Util.IsSdCardCanRead()) {
            Toast.makeText(mCoreConfig.getContext(), R.string.empty_sdcard, Toast.LENGTH_SHORT).show();
            return;
        }

        mRequestCode = requestCode;
        mCallback = callback;

        config.mutiSelect = false;//拍照为单选
        mCurrentFunctionConfig = config;

        Intent intent = new Intent(mCoreConfig.getContext(), PhotoEditActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PhotoEditActivity.TAKE_PHOTO_ACTION, true);
        mCoreConfig.getContext().startActivity(intent);
    }

    /**
     * 打开裁剪
     *
     * @param requestCode
     * @param photoPath
     * @param callback
     */
    public static void openCrop(int requestCode, String photoPath, OnHandlerResultCallback callback) {
        FunctionConfig config = copyGlobalFuncationConfig();
        if (config != null) {
            openCrop(requestCode, config, photoPath, callback);
        } else {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            AppDebugConfig.e(AppDebugConfig.TAG_GALLERY, "Please init GalleryFinal.");
        }
    }

    /**
     * 打开裁剪
     *
     * @param requestCode
     * @param config
     * @param photoPath
     * @param callback
     */
    public static void openCrop(int requestCode, FunctionConfig config, String photoPath, OnHandlerResultCallback
            callback) {
        if (mCoreConfig.getImageLoader() == null) {
            AppDebugConfig.e(AppDebugConfig.TAG_GALLERY, "Please init GalleryFinal.");
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            return;
        }

        if (config == null && mGlobalFunctionConfig == null) {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            return;
        }

        if (!Util_System_SDCard_Util.IsSdCardCanRead()) {
            Toast.makeText(mCoreConfig.getContext(), R.string.empty_sdcard, Toast.LENGTH_SHORT).show();
            return;
        }

        if (config == null || TextUtils.isEmpty(photoPath) || !new File(photoPath).exists()) {
            AppDebugConfig.d(AppDebugConfig.TAG_GALLERY, "config为空或文件不存在");
            return;
        }
        mRequestCode = requestCode;
        mCallback = callback;

        //必须设置这个三个选项
        config.mutiSelect = false;//拍照为单选
        config.editPhoto = true;
        config.crop = true;

        mCurrentFunctionConfig = config;
        ArrayList<PhotoInfo> map = new ArrayList<>();
        PhotoInfo photoInfo = new PhotoInfo();
        photoInfo.setPhotoPath(photoPath);
        photoInfo.setPhotoId(Utils.getRandom(10000, 99999));
        map.add(photoInfo);
        Intent intent = new Intent(mCoreConfig.getContext(), PhotoEditActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PhotoEditActivity.CROP_PHOTO_ACTION, true);
        intent.putExtra(PhotoEditActivity.SELECT_MAP, map);
        mCoreConfig.getContext().startActivity(intent);
    }

    /**
     * 打开编辑
     *
     * @param requestCode
     * @param photoPath
     * @param callback
     */
    public static void openEdit(int requestCode, String photoPath, OnHandlerResultCallback callback) {
        FunctionConfig config = copyGlobalFuncationConfig();
        if (config != null) {
            openEdit(requestCode, config, photoPath, callback);
        } else {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            AppDebugConfig.e(AppDebugConfig.TAG_GALLERY, "Please init GalleryFinal.");
        }
    }

    /**
     * 打开编辑
     *
     * @param requestCode
     * @param config
     * @param photoPath
     * @param callback
     */
    public static void openEdit(int requestCode, FunctionConfig config, String photoPath, OnHandlerResultCallback
            callback) {
        if (mCoreConfig.getImageLoader() == null) {
            AppDebugConfig.e(AppDebugConfig.TAG_GALLERY, "Please init GalleryFinal.");
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            return;
        }

        if (config == null && mGlobalFunctionConfig == null) {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            return;
        }

        if (!Util_System_SDCard_Util.IsSdCardCanRead()) {
            Toast.makeText(mCoreConfig.getContext(), R.string.empty_sdcard, Toast.LENGTH_SHORT).show();
            return;
        }

        if (config == null || TextUtils.isEmpty(photoPath) || !new File(photoPath).exists()) {
            AppDebugConfig.d(AppDebugConfig.TAG_GALLERY, "config为空或文件不存在");
            return;
        }
        mRequestCode = requestCode;
        mCallback = callback;

        config.mutiSelect = false;//拍照为单选

        mCurrentFunctionConfig = config;
        ArrayList<PhotoInfo> map = new ArrayList<>();
        PhotoInfo photoInfo = new PhotoInfo();
        photoInfo.setPhotoPath(photoPath);
        photoInfo.setPhotoId(Utils.getRandom(10000, 99999));
        map.add(photoInfo);
        Intent intent = new Intent(mCoreConfig.getContext(), PhotoEditActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PhotoEditActivity.EDIT_PHOTO_ACTION, true);
        intent.putExtra(PhotoEditActivity.SELECT_MAP, map);
        mCoreConfig.getContext().startActivity(intent);
    }

    /**
     * 打开图片预览界面
     *
     * @param selectedIndex 选择首次显示图片的下标，从0开始
     * @param picsPath      待预览的图片地址字符串数组
     * @return 同步处理操作的结果
     */
    public static int openMultiPhoto(int selectedIndex, String... picsPath) {
        if (picsPath == null || picsPath.length == 0) {
            return Error.RET_NO_SELECTED_PHOTO;
        }

        List<PhotoInfo> photoInfos = new ArrayList<>(picsPath.length);
        for (String s : picsPath) {
            PhotoInfo p = new PhotoInfo();
            p.setPhotoId(s.hashCode());
            p.setPhotoPath(s);
            photoInfos.add(p);
        }
        return openMultiPhoto(0x101, selectedIndex, null, null, photoInfos);
    }

    /**
     * 打开图片预览界面
     *
     * @param selectedIndex 选择首次显示图片的下标，从0开始
     * @param photoInfos    待预览的图片信息数组
     * @return 同步处理操作的结果
     */
    public static int openMultiPhoto(int selectedIndex, List<PhotoInfo> photoInfos) {
        return openMultiPhoto(0x101, selectedIndex, null, null, photoInfos);
    }

    /**
     * 打开图片预览界面
     *
     * @param requestCode   请求码
     * @param selectedIndex 选择首次显示图片的下标，从0开始
     * @param config        预览的配置设置，如果已经设置了全局，可默认为null
     * @param callback      异步执行返回
     * @param photoInfos    待预览的图片信息列表
     * @return 同步处理操作的结果
     */
    public static int openMultiPhoto(int requestCode, int selectedIndex, FunctionConfig config,
                                     OnHandlerResultCallback callback, List<PhotoInfo> photoInfos) {
        if (mCoreConfig.getImageLoader() == null) {
            AppDebugConfig.e(AppDebugConfig.TAG_GALLERY, "Please init GalleryFinal.");
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            return Error.RET_INIT_FAIL;
        }

        if (config == null && mGlobalFunctionConfig == null) {
            if (callback != null) {
                callback.onHandlerFailure(requestCode, mCoreConfig.getContext().getString(R.string.open_gallery_fail));
            }
            return Error.RET_INIT_FAIL;
        }


        mRequestCode = requestCode;
        mCallback = callback;
        mCurrentFunctionConfig = config;
        Intent intent = new Intent(mCoreConfig.getContext(), PhotoPreviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(GalleryFinal.PHOTO_INDEX, selectedIndex);
        intent.putExtra(GalleryFinal.PHOTO_LIST, (Serializable) photoInfos);
        intent.putExtra(GalleryFinal.PHOTO_SHOW_TAB, true);
        intent.putExtra(GalleryFinal.PHOTO_SHOW_TITLE, false);
        mCoreConfig.getContext().startActivity(intent);
        return Error.SUCCESS;
    }

    /**
     * 清除缓存文件
     */
    public static void cleanCacheFile() {
        if (mCurrentFunctionConfig != null && mCoreConfig.getEditPhotoCacheFolder() != null) {
            //清楚裁剪冗余图片
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Util_System_File.delete(mCoreConfig.getEditPhotoCacheFolder());
                }
            }.start();
        }
    }

    public static int getRequestCode() {
        return mRequestCode;
    }

    public static OnHandlerResultCallback getCallback() {
        return mCallback;
    }

    public static abstract class Error {
        public static final int SUCCESS = 0;
        public static final int RET_INIT_FAIL = 0x101;
        public static final int RET_NO_SELECTED_PHOTO = 0x102;
    }

    /**
     * 处理结果
     */
    public interface OnHandlerResultCallback {
        /**
         * 处理成功
         *
         * @param requestCode
         * @param resultList
         */
        void onHandlerSuccess(int requestCode, List<PhotoInfo> resultList);

        /**
         * 处理失败或异常
         *
         * @param requestCode
         * @param errorMsg
         */
        void onHandlerFailure(int requestCode, String errorMsg);
    }
}
