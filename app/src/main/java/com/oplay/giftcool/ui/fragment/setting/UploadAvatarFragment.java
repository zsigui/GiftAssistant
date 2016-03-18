package com.oplay.giftcool.ui.fragment.setting;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.req.ReqModifyAvatar;
import com.oplay.giftcool.model.data.resp.ModifyAvatar;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.BitmapUtil;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.coder.Coder_Md5;

import java.io.ByteArrayOutputStream;
import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-1-17.
 */
public class UploadAvatarFragment extends BaseFragment {

    private final static String PAGE_NAME = "上传头像";
    private final static String TOAST_FAILED = "上传失败";
    private static final int REQ_ID_PHOTO_ALBUM = 33;
    private static final int REQ_ID_PHOTO_CAMERA = 34;
    /*拍照的照片存储位置*/
    private static final File PHOTO_DIR = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera");
    private File mCurrentPhotoFile;//照相机拍照得到的图片
    private String mCurrentSelectFilePath;

    private ImageView ivAvatar;
    private RelativeLayout rlGallery;
    private RelativeLayout rlTakePhoto;

    // 封装请求Gallery的intent
    public static Intent getPhotoPickIntent() {
        final Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        return intent;
    }

    public static Intent getTakePickIntent(File f) {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    public static UploadAvatarFragment newInstance() {
        return new UploadAvatarFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        if (!AccountManager.getInstance().isLogin()) {
            ToastUtil.showShort(mApp.getResources().getString(R.string.st_hint_un_login));
            IntentUtil.jumpLogin(getContext());
            getActivity().finish();
            return;
        }
        setContentView(R.layout.fragment_user_set_avator);
        ivAvatar = getViewById(R.id.iv_icon);
        rlGallery = getViewById(R.id.rl_gallery);
        rlTakePhoto = getViewById(R.id.rl_take_photo);
    }

    @Override
    protected void setListener() {
        rlGallery.setOnClickListener(this);
        rlTakePhoto.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        ImageLoader.getInstance().displayImage(AccountManager.getInstance().getUserInfo().avatar, ivAvatar,
                Global.AVATAR_IMAGE_LOADER);
    }

    @Override
    protected void lazyLoad() {

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.rl_gallery:
                // 从相册中去获取
                doPickPhotoFromGallery();
                break;
            case R.id.rl_take_photo:
                String status = Environment.getExternalStorageState();
                //判断是否有SD卡
                if (status.equals(Environment.MEDIA_MOUNTED)) {
                    // 用户点击了从照相机获取
                    doTakePhoto();
                } else {
                    ToastUtil.showShort("没有SD卡");
                }
                break;
        }
    }

    /**
     * 拍照获取图片
     */
    public void doTakePhoto() {
        try {
            // Launch camera to take photo for selected contact
            if (!PHOTO_DIR.exists()) {
                // 创建照片的存储目录
                PHOTO_DIR.mkdirs();
            }
            mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());// 给新照的照片文件命名
            startActivityForResult(getTakePickIntent(mCurrentPhotoFile), REQ_ID_PHOTO_CAMERA);
        } catch (ActivityNotFoundException e) {
            ToastUtil.showShort("没有找到照相软件，请安装照相机软件");
        }
    }

    /**
     * 请求Gallery程序
     */
    public void doPickPhotoFromGallery() {
        try {
            // Launch picker to choose photo for selected contact
            startActivityForResult(getPhotoPickIntent(), REQ_ID_PHOTO_ALBUM);
        } catch (ActivityNotFoundException e) {
            ToastUtil.showShort("没有找到图片选择器");
        }
    }

    /**
     * 用当前时间给取得的图片命名
     */
    private String getPhotoFileName() {
        return String.format("IMG_%s.jpg", Coder_Md5.md5(DateUtil.getDate("yyyyMMddHHmmss", 0)));
    }

    /**
     * get file path from URI, throw IllegalArgumentException if not found
     *
     * @param context
     * @param uri
     * @return
     */
    public String getPath(Context context, Uri uri) {
        final String[] filePathColumn = {MediaStore.Images.Media.DATA};
        final Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        final int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        final String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    public void showLoading() {
        if (getActivity() != null) {
            ((BaseAppCompatActivity) getActivity()).showLoadingDialog("上传图片中...");
        }
    }

    public void hideLoading() {
        if (getActivity() != null) {
            ((BaseAppCompatActivity) getActivity()).hideLoadingDialog();
        }
    }

    /**
     * 上传头像的网络请求声明
     */
    private Call<JsonRespBase<ModifyAvatar>> mCall;

    /**
     * 执行上传头像的任务
     */
    public void onRequestUploadPortrait(final String filePath) {
        showLoading();
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (!NetworkUtil.isConnected(getContext())) {
                    hideLoading();
                    return;
                }
                if (mCall != null) {
                    mCall.cancel();
                }
                ReqModifyAvatar reqData = new ReqModifyAvatar();
                File file = new File(filePath);
                if (!file.exists() || !file.isFile()) {
                    ToastUtil.showShort("请求文件不存在 : " + filePath);
                    hideLoading();
                    return;
                }
                reqData.avatar = generateImageStringParam(filePath);
                mCall = Global.getNetEngine().modifyUserAvatar(new JsonReqBase<ReqModifyAvatar>(reqData));
                mCall.enqueue(new Callback<JsonRespBase<ModifyAvatar>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<ModifyAvatar>> call, Response<JsonRespBase<ModifyAvatar>> response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        hideLoading();
                        if (response != null && response.isSuccessful()) {
                            if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
                                ImageLoader.getInstance().displayImage(
                                        response.body().getData().avatar, ivAvatar, Global.AVATAR_IMAGE_LOADER);
                                UserModel model = AccountManager.getInstance().getUser();
                                try {
                                    ImageLoader.getInstance().getDiskCache().remove(model.userInfo.avatar);
                                } catch (Throwable ignored) {
                                }
                                model.userInfo.avatar = response.body().getData().avatar;
                                AccountManager.getInstance().notifyUserAll(model);
                                ScoreManager.getInstance().toastByCallback(response.body().getData());
                                return;
                            }
                            if (AppDebugConfig.IS_DEBUG) {
                                KLog.e(AppDebugConfig.TAG_FRAG,
                                        response.body() == null ? "解析失败" : response.body().error());
                            }
                            ToastUtil.blurErrorMsg(TOAST_FAILED, response.body());
                            return;
                        }
                        ToastUtil.blurErrorResp(TOAST_FAILED, response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<ModifyAvatar>> call, Throwable t) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        hideLoading();
                        if (AppDebugConfig.IS_DEBUG) {
                            KLog.e(AppDebugConfig.TAG_FRAG, t);
                        }
                        ToastUtil.blurThrow(TOAST_FAILED);
                    }
                });
            }
        });
    }

    /**
     * 根据文件获取图片字节数组的Base64编码字符串
     */
    private String generateImageStringParam(String filePath) {
        Bitmap bitmap = BitmapUtil.getBitmap(filePath, 10 * 1024 * 8,
                AppConfig.UPLOAD_PIC_WIDTH, AppConfig.UPLOAD_PIC_HEIGHT);
        ByteArrayOutputStream baos;
        int width = AppConfig.UPLOAD_PIC_WIDTH;
        int height = AppConfig.UPLOAD_PIC_HEIGHT;
        do {
            width = width * 2 / 3;
            height = height * 2 / 3;
            bitmap = BitmapUtil.createBitmapThumbnail(bitmap, true, width, height);
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, AppConfig.UPLOAD_PIC_QUALITY, baos);
        } while (baos.toByteArray().length > 100 * 1024);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (AppDebugConfig.IS_DEBUG) {
                AppDebugConfig.logMethodWithParams(this, "onActivityResult_Modify_UserInfo, requestCode:" +
                        requestCode + "  resultCode:" + resultCode + "  data:" + data);
            }
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            /**
             * 获取照片返回成功，开始对应处理
             */
            switch (requestCode) {
                case REQ_ID_PHOTO_ALBUM:
                    // 调用Gallery返回的
//                    final Bitmap photo = data.getParcelableExtra("data");
                    if (data != null && data.getData() != null) {
                        final Uri result = data.getData();
                        final String photoFilePath = getPath(getActivity(), result);
                        if (AppDebugConfig.IS_DEBUG) {
                            AppDebugConfig.logMethodWithParams(this, "PHOTO_PICKED:" + photoFilePath);
                        }
                        if (!TextUtils.isEmpty(photoFilePath)) {
                            onRequestUploadPortrait(photoFilePath);
                            mCurrentSelectFilePath = photoFilePath;
                        } else {
                            ToastUtil.showShort("无法获取到选择的图像，请使用照相功能。");
                        }
                    }
                    break;
                case REQ_ID_PHOTO_CAMERA: {
                    // 照相机程序返回的,再次调用图片剪辑程序去修剪图片
//                    doCropPhoto(mCurrentPhotoFile, 80, 80);
                    if (mCurrentPhotoFile != null) {
                        // 有可能只是选择了存储地址，但是实际上并没有拍照，也就是说没有存储成功，所以地址是无效的
                        if (mCurrentPhotoFile.exists()) {
                            final String takePath = mCurrentPhotoFile.getAbsolutePath();
                            if (AppDebugConfig.IS_DEBUG) {
                                AppDebugConfig.logMethodWithParams(this, "PHOTO_TAKEN:" + takePath);
                            }
                            onRequestUploadPortrait(takePath);
                            mCurrentSelectFilePath = takePath;
                        }
                    }
                    break;
                }
            }

        } catch (Exception e) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.e(e);
            }
        }
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }

    @Override
    public void release() {
        super.release();
        if (mCall != null) {
            mCall.cancel();
            mCall = null;
        }
    }
}
