package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.req.ReqModifyAvatar;
import com.oplay.giftcool.model.data.resp.ModifyAvatar;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.BitmapUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
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
    private String mCurrentSelectFilePath;

    private ImageView ivAvatar;
    private RelativeLayout rlGallery;
    private RelativeLayout rlTakePhoto;

    public static UploadAvatarFragment newInstance() {
        return new UploadAvatarFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        if (!AccountManager.getInstance().isLogin()) {
            ToastUtil.showShort(ConstString.TOAST_SESSION_UNAVAILABLE);
            IntentUtil.jumpLoginNoToast(getContext());
            if (getActivity() != null) {
                getActivity().finish();
            }
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
                Global.getAvatarImgOptions());
    }

    @Override
    protected void lazyLoad() {

    }


    @Override
    public void onResume() {
        super.onResume();
        if (mCurrentSelectFilePath != null) {
            onRequestUploadPortrait(mCurrentSelectFilePath);
            mCurrentSelectFilePath = null;
        }

    }

    /**
     * 处理返回的信息
     */
    private GalleryFinal.OnHandlerResultCallback mResultCallback = new GalleryFinal.OnHandlerResultCallback() {
        @Override
        public void onHandlerSuccess(int requestCode, List<PhotoInfo> resultList) {
            switch (requestCode) {
                case REQ_ID_PHOTO_ALBUM:
                case REQ_ID_PHOTO_CAMERA:
                    if (resultList == null || resultList.size() == 0) {
                        mCurrentSelectFilePath = null;
                        ToastUtil.showShort(ConstString.TOAST_GET_PIC_FAILED);
                        return;
                    }
                    AppDebugConfig.d(AppDebugConfig.TAG_FRAG, "upload avatar path = " + resultList.get(0)
                            .getPhotoPath());
                    mCurrentSelectFilePath = resultList.get(0).getPhotoPath();
                    break;
            }
        }

        @Override
        public void onHandlerFailure(int requestCode, String errorMsg) {
            switch (requestCode) {
                case REQ_ID_PHOTO_ALBUM:
                    ToastUtil.showShort(errorMsg);
                    break;
                case REQ_ID_PHOTO_CAMERA:
                    break;
            }
            mCurrentSelectFilePath = null;
        }
    };

    private long mLastClickTime;

    @Override
    public void onClick(View v) {
        final long curTime = System.currentTimeMillis();
        if (curTime - mLastClickTime < Global.CLICK_TIME_INTERVAL) {
            // 对于连续点击不做处理
            return;
        }
        mLastClickTime = curTime;
        super.onClick(v);
        switch (v.getId()) {
            case R.id.rl_gallery:
                GalleryFinal.openGalleryMulti(REQ_ID_PHOTO_ALBUM, 1, mResultCallback);
                break;
            case R.id.rl_take_photo:
                GalleryFinal.openCamera(REQ_ID_PHOTO_CAMERA, mResultCallback);
                break;
        }
    }


    public void showLoading() {
        if (getContext() != null && getChildFragmentManager() != null) {
            DialogManager.getInstance().showLoadingDialog(getChildFragmentManager(),
                    getContext().getString(R.string.st_user_set_avatar_loading));
        }
    }

    public void hideLoading() {
        if (getContext() != null) {
            DialogManager.getInstance().hideLoadingDialog();
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
                    AppDebugConfig.d(AppDebugConfig.TAG_FRAG, "请求文件不存在 : " + filePath);
                    ToastUtil.showShort(ConstString.TOAST_GET_PIC_FAILED);
                    hideLoading();
                    return;
                }
                reqData.avatar = generateImageStringParam(filePath);
                // 图片解析出来后，将裁剪的冗余图片清空
                GalleryFinal.cleanCacheFile();
                mCall = Global.getNetEngine().modifyUserAvatar(new JsonReqBase<ReqModifyAvatar>(reqData));
                mCall.enqueue(new Callback<JsonRespBase<ModifyAvatar>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<ModifyAvatar>> call,
                                           Response<JsonRespBase<ModifyAvatar>> response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        hideLoading();
                        if (response != null && response.isSuccessful()) {
                            if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
                                ImageLoader.getInstance().displayImage(
                                        response.body().getData().avatar, ivAvatar, Global.getAvatarImgOptions());
                                UserModel model = AccountManager.getInstance().getUser();
                                try {
                                    ImageLoader.getInstance().getDiskCache().remove(model.userInfo.avatar);
                                } catch (Throwable ignored) {
                                }
                                model.userInfo.avatar = response.body().getData().avatar;
                                ScoreManager.getInstance().setTaskFinished(true);
                                AccountManager.getInstance().notifyUserAll(model);
                                return;
                            }
                        }
                        ToastUtil.blurErrorResp(TOAST_FAILED, response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<ModifyAvatar>> call, Throwable t) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        hideLoading();
                        ToastUtil.blurThrow(TOAST_FAILED, t);
                    }
                });
            }
        });
    }

    /**
     * 根据文件获取图片字节数组的Base64编码字符串
     */
    private String generateImageStringParam(String filePath) {
        ByteArrayOutputStream baos = BitmapUtil.getBitmapForBaos(filePath, AppConfig.UPLOAD_PIC_SIZE,
                AppConfig.UPLOAD_PIC_WIDTH, AppConfig.UPLOAD_PIC_HEIGHT);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
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
