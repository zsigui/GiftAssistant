package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.message.AdmireMessageFragment;
import com.oplay.giftcool.ui.fragment.message.CommentMessageFragment;
import com.oplay.giftcool.ui.fragment.message.MessageCentralFragment;
import com.oplay.giftcool.ui.fragment.message.NewNotifyMessageFragment;
import com.oplay.giftcool.ui.fragment.message.SystemMessageFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsigui on 16-1-5.
 */
public class MessageActivity extends BaseAppCompatActivity implements ObserverManager.UserUpdateListener{

	private List<Integer> mTypeHierarchy;

	@Override
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void processLogic() {
		mTypeHierarchy = new ArrayList<>(5);
		handleRedirect(getIntent());
		ObserverManager.getInstance().addUserUpdateListener(this);
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ObserverManager.getInstance().removeUserUpdateListener(this);
	}

	private void handleRedirect(Intent intent) {

		if (intent == null) {
			ToastUtil.showShort("跳转出错");
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.d(AppDebugConfig.TAG_FRAG, "no intent");
			}
			return;
		}
		int type = intent.getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
		if (type == KeyConfig.TYPE_ID_DEFAULT) {
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.d(AppDebugConfig.TAG_FRAG, "no type");
			}
			ToastUtil.showShort("跳转出错");
			return;
		}
		mTypeHierarchy.add(type);
		switch (type) {
			case KeyConfig.TYPE_ID_MSG:
				replaceFragWithTitle(R.id.fl_container, MessageCentralFragment.newInstance(),
						getResources().getString(R.string.st_msg_central_title));
				break;
			case KeyConfig.TYPE_ID_MSG_ADMIRE:
				replaceFragWithTitle(R.id.fl_container, AdmireMessageFragment.newInstance(),
						getResources().getString(R.string.st_msg_central_admire));
				break;
			case KeyConfig.TYPE_ID_MSG_SYSTEM:
				replaceFragWithTitle(R.id.fl_container, SystemMessageFragment.newInstance(),
						getResources().getString(R.string.st_msg_central_system));
				break;
			case KeyConfig.TYPE_ID_MSG_COMMENT:
				replaceFragWithTitle(R.id.fl_container, CommentMessageFragment.newInstance(),
						getResources().getString(R.string.st_msg_central_comment));
				break;
			case KeyConfig.TYPE_ID_MSG_NEW_GIFT_NOTIFY:
				replaceFragWithTitle(R.id.fl_container, NewNotifyMessageFragment.newInstance(),
						getResources().getString(R.string.st_msg_central_new_gift_notify));
				break;
			default:
				mTypeHierarchy.remove(mTypeHierarchy.size() - 1);
				if (AppDebugConfig.IS_FRAG_DEBUG) {
					KLog.d(AppDebugConfig.TAG_FRAG, "type = " + type);
				}
				ToastUtil.showShort("跳转出错");
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (mTypeHierarchy == null) {
			mTypeHierarchy = new ArrayList<>();
		}
		int type = mTypeHierarchy.size() == 0 ?
				KeyConfig.TYPE_ID_DEFAULT: mTypeHierarchy.get(mTypeHierarchy.size() - 1);
		if (intent != null
				&& type != KeyConfig.TYPE_ID_DEFAULT
				&& type != intent.getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT)) {
			handleRedirect(intent);
		}
	}

	/**
	 * 重新复写处理后退事件
	 */
	@Override
	public boolean onBack() {
		InputMethodUtil.hideSoftInput(this);
		if (getTopFragment() != null && getTopFragment() instanceof OnBackPressListener
				&& ((OnBackPressListener) getTopFragment()).onBack()) {
			// back事件被处理
			return false;
		}
		if (!popFrag() && !isFinishing()) {
			mNeedWorkCallback = false;
			if (MainActivity.sGlobalHolder == null) {
				IntentUtil.jumpHome(this, false);
			}
			finish();
		} else {
			if (getTopFragment() instanceof BaseFragment) {
				setBarTitle(((BaseFragment) getTopFragment()).getTitleName());
			}
			if (mTypeHierarchy != null && mTypeHierarchy.size() > 0) {
				mTypeHierarchy.remove(mTypeHierarchy.size() - 1);
			}
		}
		return true;
	}

	@Override
	public void onUserUpdate(int action) {
		if (!AccountManager.getInstance().isLogin()) {
			ToastUtil.showShort(getResources().getString(R.string.st_hint_un_login));
			IntentUtil.jumpLoginNoToast(this);
			finish();
		}
	}
}
