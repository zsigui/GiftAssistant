package com.oplay.giftcool.ui.fragment.login;

import android.Manifest;
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
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.model.data.req.ReqLogin;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.MixUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.PermissionUtil;
import com.oplay.giftcool.util.StringUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

import net.ouwan.umipay.android.view.MaxRowListView;
import net.youmi.android.libs.common.util.Util_System_Permission;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-1-11.
 */
public class PhoneLoginNewFragment extends BaseFragment implements TextView.OnEditorActionListener,
        OnItemClickListener<String>, View.OnFocusChangeListener, OnBackPressListener, TextWatcher {

    private final static String PAGE_NAME = "手机号登录";
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
    private LinearLayout llVoiceCode;
    private TextView tvVoiceCode;
    private TextView tvVoiceHint;

    private PopupWindow mAccountPopup;
    private AccountAdapter mAccountAdapter;
    private AccountAdapter mCompleteAdapter;
    private boolean mIsInFirstStep = true;
    private ArrayList<String> mData;

    // 倒计时剩余时间，每次发送后刷新
    private final static int RESEND_DURATION = 60;
    private int sSendCodeRemainTime = 0;
    // 当前是否正处于短信验证码界面
    private boolean mInVoice = false;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable setTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (sSendCodeRemainTime == 0) {
                btnSendCode.setEnabled(true);
                btnSendCode.setTextColor(AssistantApp.getInstance().getResources().getColor(R.color.co_btn_green));
                btnSendCode.setText(getResources().getString(mInVoice ?
                        R.string.st_login_send_voice_code : R.string.st_login_send_sms_code));
                tickState = STATE_NONE;
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
        tvVoiceCode = getViewById(R.id.tv_voice_code);
        llVoiceCode = getViewById(R.id.ll_voice_code);
        tvVoiceHint = getViewById(R.id.tv_voice_hint);
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
        tvVoiceCode.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
//        InputTextUtil.initPswFilter(etPhone, etCode, tvPhoneClear, tvCodeClear, btnLogin, btnSendCode, false);
//		ctvAgreeLaw.setChecked(true);
        etPhone.addTextChangedListener(this);
        etCode.addTextChangedListener(this);
        btnLogin.setEnabled(false);
        btnSendCode.setEnabled(true);
        btnSendCode.setTextColor(ViewUtil.getColor(getContext(), R.color.co_btn_green));
        initHint();
        showPhoneView(true);
        btnLogin.setText(getContext().getString(R.string.st_login_phone_btn_next));
        registerSmsObserver();
        changeCodeTypeUI(mInVoice);
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

    private void showPhoneView(boolean phoneVisible) {
        llCode.setVisibility(phoneVisible ? View.GONE : View.VISIBLE);
        llVoiceCode.setVisibility(phoneVisible ? View.GONE : View.VISIBLE);
        llPhone.setVisibility(phoneVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void lazyLoad() {

    }

    private long mLastClickTime = 0;
    private long mGetCodeLastClickTime = 0;

    @Override
    public void onClick(View v) {
        super.onClick(v);
        long getCodeCurTime;
        switch (v.getId()) {
            case R.id.btn_send:
                if (mIsInFirstStep) {
                    mIsInFirstStep = false;
                    showPhoneView(false);
                    if (sSendCodeRemainTime == 0) {
                        handleGetCode(mInVoice);
                    }
                    btnLogin.setText(getContext().getString(R.string.st_login_btn_text));
                    btnLogin.setEnabled(false);
                } else {
                    handleLogin();
                }
                break;
            case R.id.tv_send_code:
                getCodeCurTime = System.currentTimeMillis();
                if (getCodeCurTime - mGetCodeLastClickTime < Global.CLICK_TIME_INTERVAL) {
                    mGetCodeLastClickTime = getCodeCurTime;
                    return;
                }
                handleGetCode(mInVoice);
                break;
            case R.id.tv_voice_code:
                mInVoice = !mInVoice;
                changeCodeTypeUI(mInVoice);
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

    public final int STATE_NONE = 0;
    public final int STATE_SMS = 1;
    public final int STATE_VOICE = 2;
    int tickState = STATE_NONE;

    /**
     * 处理获取验证码事件
     */
    private void handleGetCode(final boolean isVoice) {
        final ReqLogin login = new ReqLogin();
        if (!login.setPhoneUser(etPhone.getText().toString())) {
            ToastUtil.showShort(ConstString.TOAST_PHONE_ERROR);
            return;
        }
        if (!NetworkUtil.isConnected(AssistantApp.getInstance().getApplicationContext())) {
            ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
            mLastSendTime = System.currentTimeMillis();
            resetRemain();
            return;
        }
        showLoading();
        btnSendCode.setEnabled(false);
        btnSendCode.setTextColor(AssistantApp.getInstance().getResources().getColor(R.color.co_btn_grey));
        mHandler.removeCallbacks(setTimeRunnable);
        login.sendType = isVoice ? "voice" : "sms";

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
                        if (isVoice) {
                            ToastUtil.showShort(ConstString.TOAST_VOICE_CODE_SEND);
                        } else {
                            ToastUtil.showShort(ConstString.TOAST_SMS_CODE_SEND);
                        }
                        sSendCodeRemainTime = RESEND_DURATION;
                        tickState = isVoice ? STATE_VOICE : STATE_SMS;
                        bindCodeHint(isVoice);
                        mHandler.postDelayed(setTimeRunnable, 1000);
                        return;
                    }
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
                ToastUtil.blurThrow(t);
                resetRemain();
            }
        });
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
        if (!login.setPhoneUser(etPhone.getText().toString().trim(), etCode.getText().toString().trim())) {
            ToastUtil.showShort(ConstString.TOAST_PHONE_ERROR);
            return;
        }
        if (!NetworkUtil.isConnected(getContext())) {
            hideLoading();
            ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
            return;
        }
        showLoading();
        etCode.requestFocus();
        etCode.setSelection(etCode.getText().toString().length());
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
                        UserModel um = response.body().getData();
                        MixUtil.doPhoneLoginSuccessNext(getContext(), um);
                        AccountManager.getInstance().writePhoneAccount(login.getPhone(), mData, false);
                        return;
                    }
                    if (response.body() != null
                            && response.body().getCode() == NetStatusCode.ERR_NEED_CHOOSE_MAIN_ACCOUNT) {
                        // 有多个绑定账号且无主账号，需要跳转绑定主账号界面
                        UserModel um = response.body().getData();
                        um.userInfo = new UserInfo();
                        um.userInfo.phone = login.getPhone();
                        ((BaseAppCompatActivity) getActivity()).replaceFragWithTitle(R.id.fl_container,
                                ChooseOwanFragment.newInstance(um),
                                getResources().getString(R.string.st_login_choose_owan_title), false);
                        return;
                    }
                }
                ToastUtil.blurErrorResp(response);
            }

            @Override
            public void onFailure(Call<JsonRespBase<UserModel>> call, Throwable t) {
                if (!mCanShowUI || call.isCanceled()) {
                    return;
                }
                hideLoading();
                ToastUtil.blurThrow(t);
            }
        });
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
        if (etPhone != null && etPhone.isPopupShowing()) {
            etPhone.dismissDropDown();
            return true;
        }
        if (!mIsInFirstStep) {
            etCode.setText("");
            showPhoneView(true);
            etPhone.requestFocus();
            btnLogin.setText(getString(R.string.st_login_phone_btn_next));
            btnLogin.setEnabled(true);
            mIsInFirstStep = true;
            return true;
        }
        return false;
    }


    private void changeCodeTypeUI(boolean isVoice) {
        etCode.setHint(getString(isVoice ? R.string.st_login_voice_code_hint : R.string.st_login_phone_code_hint));
        tvVoiceHint.setText(getString(isVoice ?
                R.string.st_login_sms_code_try : R.string.st_login_voice_phone_code_try));
        tvVoiceCode.setText(getString(isVoice ? R.string.st_login_sms_code : R.string.st_login_voice_code));
        bindCodeHint(isVoice);
    }

    private void bindCodeHint(boolean isVoice) {
        if (tickState == STATE_NONE
                || (tickState == STATE_SMS && isVoice)
                || (tickState == STATE_VOICE && !isVoice)) {
            tvCodeHint.setText(getString(isVoice ? R.string.st_login_voice_code_send_hint
                    : R.string.st_login_sms_code_send_hint));
        } else {
            tvCodeHint.setText(Html.fromHtml(
                    String.format(getString(mInVoice ? R.string.st_login_voice_code_toast
                                    : R.string.st_login_sms_code_toast),
                            etPhone.getText().toString().trim())));
        }
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
        unregisterSmsObserver();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionUtil.READ_SMS:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 该操作防止短信已过去
                    AppDebugConfig.w(AppDebugConfig.TAG_DEBUG_INFO, "sms permission granted");
                    getSmsFromPhone();
                    registerSmsObserver();
                }
                break;
        }
    }

    private int mLoginCountdown;
    private boolean mIsRegisterObserver = false;
    private long mLastSendTime;

    Runnable autoLoginRunnable = new Runnable() {
        @Override
        public void run() {
            if (btnLogin != null && !mIsInFirstStep && !mInVoice) {
                // 保证处于短信验证码区时才自动读取
                btnLogin.setText(String.format(getString(R.string.st_login_phone_btn_auto), mLoginCountdown));
                if (--mLoginCountdown < 0) {
                    handleLogin();
                    btnLogin.setText(getString(R.string.st_login_btn_text));
                } else {
                    ThreadUtil.runOnUiThread(autoLoginRunnable, 1000);
                }
            }
        }
    };

    private void registerSmsObserver() {
        if (!mIsRegisterObserver
                && Util_System_Permission.isWithPermission(getContext(), Manifest.permission.READ_SMS)) {
            if (mObserver == null) {
                mObserver = new SmsObserver(new Handler());
            }
            getContext().getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mObserver);
            mIsRegisterObserver = true;
        }
    }

    private void unregisterSmsObserver() {
        if (mIsRegisterObserver && mObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mObserver);
        }
        mIsRegisterObserver = false;
    }

    private void getSmsFromPhone() {
        if (etCode != null) {
            //每当有新短信到来时，使用我们获取短消息的方法
            Cursor mCursor = getContext().getContentResolver().query(Uri.parse("content://sms/inbox"),
                    new String[]{"_id", "body"}, "read=? and date>?",
                    new String[]{"0", String.valueOf(mLastSendTime)}, "_id desc");
            final String appName = getContext().getString(R.string.app_name);
            // 按短信id排序，如果按date排序的话，修改手机时间后，读取的短信就不准了
            if (mCursor != null && mCursor.getCount() > 0) {
                try {
                    while (mCursor.moveToNext()) {
                        String smsBody = mCursor
                                .getString(mCursor.getColumnIndex("body"));
                        if (smsBody != null && smsBody.startsWith("【有米科技】验证码")
                                && smsBody.contains(appName)) {
                            etCode.setText(smsBody.substring(10, smsBody.indexOf("，")));
                            mLoginCountdown = 3;
                            ThreadUtil.remove(autoLoginRunnable);
                            ThreadUtil.runOnUiThread(autoLoginRunnable);
                            break;
                        }
                    }
                } finally {
                    mCursor.close();
                }
            }
        }
    }

    private SmsObserver mObserver;

    class SmsObserver extends ContentObserver {

        public SmsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            getSmsFromPhone();
        }

    }
}
