package com.oplay.giftcool.ui.fragment.login;

import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.AccountAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.util.UserTypeUtil;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.manager.SocketIOManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.req.ReqLogin;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.LoginActivity;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.PermissionUtil;
import com.oplay.giftcool.util.StringUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;

import net.ouwan.umipay.android.view.MaxRowListView;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-1-11.
 */
public class PhoneLoginNewFragment extends BaseFragment implements TextView.OnEditorActionListener,
        OnItemClickListener<String>, View.OnFocusChangeListener, OnBackPressListener, TextWatcher {

    private final static String PAGE_NAME = "手机号登录";
    private final static String ERR_PREFIX = "登录失败";
    private AutoCompleteTextView etPhone;
    private EditText etCode;
    private TextView tvPhoneClear;
    private TextView tvCodeClear;
    private EditText etClearFocus;
    private TextView btnSendCode;
    //	private CheckedTextView ctvAgreeLaw;
//	private TextView tvLaw;
    private TextView btnLogin;
    private TextView tvAnotherLogin;
    private TextView tvCodeHint;
    private LinearLayout llPhone;
    private LinearLayout llCode;
    private ImageView ivMore;

    private PopupWindow mAccountPopup;
    private AccountAdapter mAccountAdapter;
    private AccountAdapter mCompleteAdapter;
    private boolean mIsInFirstStep = true;
    private ArrayList<String> mData;

    // 倒计时剩余时间，每次发送后刷新
    private final static int RESEND_DURATION = 60;
    private int sSendCodeRemainTime = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable setTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (sSendCodeRemainTime == 0) {
                btnSendCode.setEnabled(true);
                btnSendCode.setTextColor(AssistantApp.getInstance().getResources().getColor(R.color.co_btn_green));
                btnSendCode.setText(getResources().getString(R.string.st_login_phone_send_code));
            } else {
                sSendCodeRemainTime--;
                btnSendCode.setText(
                        String.format(getResources().getString(R.string.st_login_phone_resend_code),
                                sSendCodeRemainTime));
                mHandler.postDelayed(setTimeRunnable, 1000);
            }
        }
    };

    public static PhoneLoginNewFragment newInstance() {
        return new PhoneLoginNewFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_login_phone_new);
        etPhone = getViewById(R.id.et_input);
        tvPhoneClear = getViewById(R.id.tv_user_clear);
        tvCodeClear = getViewById(R.id.tv_code_clear);
        etClearFocus = getViewById(R.id.et_clear_focus);
        etCode = getViewById(R.id.et_phone_code);
        btnSendCode = getViewById(R.id.tv_send_code);
        ivMore = getViewById(R.id.iv_more);
//		ctvAgreeLaw = getViewById(R.id.ctv_law);
//		tvLaw = getViewById(R.id.tv_law);
        btnLogin = getViewById(R.id.btn_send);
        llCode = getViewById(R.id.ll_code);
        tvCodeHint = getViewById(R.id.tv_code_hint);
        tvAnotherLogin = getViewById(R.id.tv_another_login);
        llPhone = getViewById(R.id.ll_input);

    }

    @Override
    protected void setListener() {
        btnLogin.setOnClickListener(this);
//		tvLaw.setOnClickListener(this);
//		ctvAgreeLaw.setOnClickListener(this);
        btnSendCode.setOnClickListener(this);
        tvAnotherLogin.setOnClickListener(this);
        tvPhoneClear.setOnClickListener(this);
        tvCodeClear.setOnClickListener(this);
        etPhone.setOnEditorActionListener(this);
        etCode.setOnEditorActionListener(this);
        getViewById(R.id.ll_code).setOnClickListener(this);
        llPhone.setOnClickListener(this);
        ivMore.setOnClickListener(this);
        etPhone.setOnFocusChangeListener(this);
        etCode.setOnFocusChangeListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
//        InputTextUtil.initPswFilter(etPhone, etCode, tvPhoneClear, tvCodeClear, btnLogin, btnSendCode, false);
//		ctvAgreeLaw.setChecked(true);
        etPhone.addTextChangedListener(this);
        etCode.addTextChangedListener(this);
        btnLogin.setEnabled(false);
        btnSendCode.setEnabled(false);
        initHint();
        llCode.setVisibility(View.GONE);
        llPhone.setVisibility(View.VISIBLE);
        btnLogin.setText("下一步");
        if (mObserver == null) {
            mObserver = new SmsObserver(new Handler());
        }
        getContext().getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mObserver);
        mIsRegisterObserver = true;
    }

    private void initHint() {
        mData = AccountManager.getInstance().readPhoneAccount();
        if (mData != null && mData.size() > 0) {
            etPhone.setText(mData.get(0));
            etCode.requestFocus();
            ivMore.setVisibility(View.VISIBLE);
        } else {
            etPhone.requestFocus();
            ivMore.setVisibility(View.GONE);
        }
        InputMethodUtil.showSoftInput(getActivity());
        mAccountAdapter = new AccountAdapter(getContext(), mData, false);
        mCompleteAdapter = new AccountAdapter(getContext(), mData, false);
        View popup = View.inflate(getContext(), R.layout.listview_account_popup, null);
        MaxRowListView popupListView = getViewById(popup, R.id.lv_popup_list_content);
        popupListView.setAdapter(mAccountAdapter);
        etPhone.setAdapter(mCompleteAdapter);
        mAccountPopup = new PopupWindow(popup, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, true);
        mAccountAdapter.setListener(this);
        mCompleteAdapter.setListener(this);
        mAccountPopup.setOutsideTouchable(true);
        mAccountPopup.setBackgroundDrawable(new BitmapDrawable());
        etPhone.setDropDownBackgroundDrawable(null);
    }

    @Override
    protected void lazyLoad() {

    }

    private long mLastClickTime = 0;
    private long mGetCodeLastClickTime = 0;

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_send:
                if (mIsInFirstStep) {
                    mIsInFirstStep = false;
                    llPhone.setVisibility(View.GONE);
                    handleGetCode();
                    llCode.setVisibility(View.VISIBLE);
                    tvCodeHint.setText(Html.fromHtml(
                            String.format(Locale.CHINA,
                                    "已向您的手机 <font color='#20C585'>%s</font> 发送了一条验证短信",
                                    etPhone.getText().toString().trim())));
                    btnLogin.setText("登录");
                    btnLogin.setEnabled(false);
                } else {
                    handleLogin();
                }
                break;
            case R.id.tv_send_code:
                long getCodeCurTime = System.currentTimeMillis();
                if (getCodeCurTime - mGetCodeLastClickTime < Global.CLICK_TIME_INTERVAL) {
                    mGetCodeLastClickTime = getCodeCurTime;
                    return;
                }
                handleGetCode();
                break;
            case R.id.iv_more:
                etClearFocus.requestFocus();
                InputMethodUtil.hideSoftInput(getActivity());
                if (mData == null || mData.size() == 0) {
                    return;
                }
                if (mAccountPopup.isShowing()) {
                    mAccountPopup.dismiss();
                } else {
                    mAccountPopup.showAsDropDown(llPhone);
                }
                break;
            case R.id.tv_another_login:
                if (getActivity() != null) {
                    ((BaseAppCompatActivity) getActivity()).replaceFragWithTitle(R.id.fl_container,
                            OuwanLoginFragment.newInstance(), getResources().getString(R.string.st_login_ouwan_title),
                            false);
                }
                break;
            case R.id.tv_user_clear:
                clearText(etCode);
                clearText(etPhone);
                btnSendCode.setEnabled(false);
                btnSendCode.setTextColor(AssistantApp.getInstance().getResources().getColor(R.color.co_btn_grey));
                break;
            case R.id.tv_code_clear:
                clearText(etCode);
                break;
            case R.id.ll_input:
                etPhone.setSelection(etPhone.getText().length());
                etPhone.requestFocus();
                break;
            case R.id.ll_code:
                etCode.setSelection(etCode.getText().length());
                etCode.requestFocus();
                break;
            default:
                if (mAccountPopup != null && mAccountPopup.isShowing()) {
                    mAccountPopup.dismiss();
                }
                if (etPhone.isPopupShowing()) {
                    etPhone.dismissDropDown();
                }
        }
    }


    private void clearText(EditText et) {
        et.setText("");
        et.requestFocus();
        et.setSelection(0);
    }

    /**
     * 获取手机登录验证码的网络请求声明
     */
    private Call<JsonRespBase<UserModel>> mCallGetCode;

    /**
     * 处理获取验证码事件
     */
    private void handleGetCode() {
        final ReqLogin login = new ReqLogin();
        if (!login.setPhoneUser(etPhone.getText().toString())) {
            showToast(ConstString.TEXT_PHONE_ERROR);
            return;
        }
        showLoading();
        btnSendCode.setEnabled(false);
        btnSendCode.setTextColor(AssistantApp.getInstance().getResources().getColor(R.color.co_btn_grey));
        sSendCodeRemainTime = RESEND_DURATION;
        mHandler.postDelayed(setTimeRunnable, 1000);
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (!NetworkUtil.isConnected(AssistantApp.getInstance().getApplicationContext())) {
                    hideLoading();
                    showToast(ConstString.TEXT_NET_ERROR);
                    sSendCodeRemainTime = 0;
                    return;
                }


                if (mCallGetCode != null) {
                    mCallGetCode.cancel();
                }
                mCallGetCode = Global.getNetEngine().login(NetUrl.USER_PHONE_LOGIN_FIRST, new JsonReqBase<ReqLogin>
                        (login));
                mCallGetCode.enqueue(new Callback<JsonRespBase<UserModel>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<UserModel>> call, Response<JsonRespBase<UserModel>>
                            response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        hideLoading();
                        if (response != null && response.isSuccessful()) {
                            if (response.body() != null
                                    && response.body().getCode() == NetStatusCode.SUCCESS) {
                                PermissionUtil.judgeSmsPermission(getActivity(), PhoneLoginNewFragment.this);
                                showToast("短信已经发送，请注意接收");
                                return;
                            }
                            ToastUtil.blurErrorMsg(response.body());
                            resetRemain();
                            return;
                        }
                        ToastUtil.blurErrorResp(response);
                        resetRemain();
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<UserModel>> call, Throwable t) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        hideLoading();
                        AppDebugConfig.warn(t);
                        ToastUtil.blurThrow();
                        resetRemain();
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionUtil.RECEIVE_SMS:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (mIsRegisterObserver) {
                        getContext().getContentResolver().unregisterContentObserver(mObserver);
                        mIsRegisterObserver = false;
                    }
                }
                break;
        }
    }

    private void resetRemain() {
        mHandler.removeCallbacks(setTimeRunnable);
        sSendCodeRemainTime = 0;
        mHandler.postAtFrontOfQueue(setTimeRunnable);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mHandler.removeCallbacks(setTimeRunnable);
    }

    /**
     * 手机登录的网络请求声明
     */
    private Call<JsonRespBase<UserModel>> mCallLogin;

    /**
     * 处理手机登录事件
     */
    private void handleLogin() {
        long curTime = System.currentTimeMillis();
        if (curTime - mLastClickTime < Global.CLICK_TIME_INTERVAL) {
            mLastClickTime = curTime;
            return;
        }
        final ReqLogin login = new ReqLogin();
        if (!login.setPhoneUser(etPhone.getText().toString(), etCode.getText().toString())) {
            showToast(ConstString.TEXT_PHONE_ERROR);
            return;
        }
        showLoading();
        etCode.requestFocus();
        etCode.setSelection(etCode.getText().toString().length());
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (!NetworkUtil.isConnected(getContext())) {
                    hideLoading();
                    showToast(ConstString.TEXT_NET_ERROR);
                    return;
                }
                if (mCallLogin != null) {
                    mCallLogin.cancel();
                }
                mCallLogin = Global.getNetEngine().login(NetUrl.USER_PHONE_LOGIN_SECOND, new JsonReqBase<ReqLogin>
                        (login));
                mCallLogin.enqueue(new Callback<JsonRespBase<UserModel>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<UserModel>> call, Response<JsonRespBase<UserModel>>
                            response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        hideLoading();
                        if (response != null && response.isSuccessful()) {
                            if (response.body() != null
                                    && response.body().getCode() == NetStatusCode.SUCCESS) {
                                doAfterSuccess(response, login);
                                return;
                            }
                            ToastUtil.blurErrorMsg(response.body());
                            return;
                        }
                        ToastUtil.blurErrorResp(response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<UserModel>> call, Throwable t) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        hideLoading();
                        AppDebugConfig.warn(t);
                        ToastUtil.blurThrow();
                    }
                });
            }
        });
    }

    /**
     * 成功登录后的处理
     */
    private void doAfterSuccess(Response<JsonRespBase<UserModel>> response, ReqLogin login) {
        UserModel userModel = response.body().getData();
        userModel.userInfo.loginType = UserTypeUtil.TYPE_POHNE;
        MainActivity.sIsTodayFirstOpen = true;
        AccountManager.getInstance().writePhoneAccount(login.getPhone(), mData, false);
        AccountManager.getInstance().notifyUserAll(userModel);
        SocketIOManager.getInstance().connectOrReConnect(true);
        ScoreManager.getInstance().initTaskState(getContext());
        StatisticsManager.getInstance().trace(getContext(),
                StatisticsManager.ID.USER_PHONE_LOGIN,
                StatisticsManager.ID.STR_USER_PHONE_LOGIN,
                "手机号:" + userModel.userInfo.phone);

        Global.sHasShowedSignInHint = Global.sHasShowedLotteryHint = false;
        ((LoginActivity) getActivity()).doLoginBack();
    }


    public void hideLoading() {
        DialogManager.getInstance().hideLoadingDialog();
    }

    public void showLoading() {
        if (getChildFragmentManager() != null) {
            DialogManager.getInstance().showLoadingDialog(getChildFragmentManager());
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_NEXT:
                etCode.requestFocus();
                etCode.setSelection(etCode.getText().toString().length());
                break;
            case EditorInfo.IME_ACTION_DONE:
                handleLogin();
                break;
        }
        return false;
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }

    @Override
    public void onItemClick(String item, View view, int position) {
        switch (view.getId()) {
            case R.id.ll_header_item:
                etPhone.setText(item);
                etCode.requestFocus();
                break;
            case R.id.iv_account_list_delete:
                if (item.equals(etPhone.getText().toString().trim())) {
                    etPhone.setText("");
                    etCode.setText("");
                    etPhone.requestFocus();
                }
                AccountManager.getInstance().writePhoneAccount(item, mData, true);
                if (mData == null || mData.size() == 0) {
                    ivMore.setVisibility(View.GONE);
                }
                mCompleteAdapter.notifyDataChanged();
                mAccountAdapter.notifyDataChanged();
                break;
        }
        etPhone.dismissDropDown();
        if (mAccountPopup != null && mAccountPopup.isShowing()) {
            mAccountPopup.dismiss();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_input:
                tvCodeClear.setVisibility(View.GONE);
                if (hasFocus && !TextUtils.isEmpty(etPhone.getText().toString().trim())) {
                    tvPhoneClear.setVisibility(View.VISIBLE);
                } else {
                    tvPhoneClear.setVisibility(View.GONE);
                }
                break;
            case R.id.et_phone_code:
                tvPhoneClear.setVisibility(View.GONE);
                if (hasFocus && !TextUtils.isEmpty(etCode.getText().toString().trim())) {
                    tvCodeClear.setVisibility(View.VISIBLE);
                } else {
                    tvCodeClear.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public boolean onBack() {
        if (mAccountPopup != null && mAccountPopup.isShowing()) {
            mAccountPopup.dismiss();
            return true;
        }
        if (etPhone.isPopupShowing()) {
            etPhone.dismissDropDown();
            return true;
        }
        if (!mIsInFirstStep) {
            etCode.setText("");
            llCode.setVisibility(View.GONE);
            llPhone.setVisibility(View.VISIBLE);
            etPhone.requestFocus();
            btnLogin.setText("下一步");
            btnLogin.setEnabled(true);
            mIsInFirstStep = true;
            return true;
        }
        return false;
    }

    @Override
    public void release() {
        super.release();
        if (mCallLogin != null) {
            mCallLogin.cancel();
            mCallLogin = null;
        }
        if (mCallGetCode != null) {
            mCallGetCode.cancel();
            mCallGetCode = null;
        }
        btnLogin = null;
        etCode = null;
        if (mIsRegisterObserver) {
            getContext().getContentResolver().unregisterContentObserver(mObserver);
        }
        mObserver = null;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mIsInFirstStep) {
            if (StringUtil.matches(etPhone.getText().toString().trim(), "^1\\d{10}$", false)) {
                btnLogin.setEnabled(true);
            } else {
                btnLogin.setEnabled(false);
            }
        } else {
            if (StringUtil.matches(etCode.getText().toString().trim(), "\\d{4,6}", false)) {
                btnLogin.setEnabled(true);
            } else {
                btnLogin.setEnabled(false);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /* ----------- 注册短信的广播接收  ----------- */

    private int mLoginCountdown;
    private boolean mIsRegisterObserver = false;

    Runnable autoLoginRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mIsInFirstStep && btnLogin != null) {
                btnLogin.setText(String.format(Locale.CHINA, "自动登录中(%d)", mLoginCountdown));
                if (--mLoginCountdown < 0) {
                    handleLogin();
                    btnLogin.setText("登录");
                } else {
                    ThreadUtil.runOnUiThread(autoLoginRunnable, 1000);
                }
            }
        }
    };

    private SmsObserver mObserver;

    class SmsObserver extends ContentObserver {

        private Cursor mCursor = null;

        public SmsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (etCode != null) {
                //每当有新短信到来时，使用我们获取短消息的方法
                getSmsFromPhone();
            }
        }

        private void getSmsFromPhone() {
            mCursor = getContext().getContentResolver().query(Uri.parse("content://sms/inbox"),
                    new String[]{"_id", "address", "read", "body"}, "read=?",
                    new String[]{"0"}, "_id desc");
            // 按短信id排序，如果按date排序的话，修改手机时间后，读取的短信就不准了
            if (mCursor != null && mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                if (mCursor.moveToFirst()) {
                    String smsBody = mCursor
                            .getString(mCursor.getColumnIndex("body"));
                    if (smsBody.startsWith("【有米科技】验证码")) {
                        etCode.setText(smsBody.substring(10, smsBody.indexOf("，")));
                        mLoginCountdown = 3;
                        ThreadUtil.runOnUiThread(autoLoginRunnable);
                    }
                }
            }
        }
    }
}
