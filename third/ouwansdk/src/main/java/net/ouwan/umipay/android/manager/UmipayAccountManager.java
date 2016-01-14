package net.ouwan.umipay.android.manager;

import android.content.Context;
import android.text.TextUtils;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.youmi.android.libs.common.cache.Proxy_DB_Cache_Helper;
import net.youmi.android.libs.common.cache.Proxy_Serializable_CacheManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * UmipayAccountManager
 *
 * @author zacklpx
 *         date 15-2-3
 *         description
 */
public class UmipayAccountManager {
	private final static String ACCOUNT_DB_NAME = "NMrY528poBxc";
	private final static String ACCOUNT_DB_PSW = "5cS71sCy";
	private final static int ACCOUNT_DB_VERSION = 1;

	private static UmipayAccountManager mInstance;

	private UmipayAccount mCurrentAccount;
	private boolean mIsLogin;
	private boolean mIsLogout;

	private Context mContext;
	private Proxy_Serializable_CacheManager mSerializeableManager;
	private List<UmipayAccount> mAccountList;


	private UmipayAccountManager(Context context) {
		this.mContext = context;
		mIsLogin = false;
		mIsLogout = false;
		mSerializeableManager = new Proxy_Serializable_CacheManager(context, ACCOUNT_DB_PSW,
				new Proxy_DB_Cache_Helper(context, ACCOUNT_DB_NAME, ACCOUNT_DB_VERSION));
		mAccountList = new ArrayList<UmipayAccount>();
		updateAccountList();
	}

	public static synchronized UmipayAccountManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new UmipayAccountManager(context);
		}
		return mInstance;
	}

	public UmipayAccount getCurrentAccount() {
		return mCurrentAccount;
	}

	public void setCurrentAccount(UmipayAccount currentAccount) {
		if (currentAccount == null) {
			mCurrentAccount = null;
		}else {
			mCurrentAccount = new UmipayAccount(currentAccount);
		}
	}

	public boolean isLogin() {
		return mIsLogin;
	}

	public void setLogin(boolean isLogin) {
		mIsLogin = isLogin;
	}

	public boolean isLogout() {
		return mIsLogout;
	}

	public void setIsLogout(boolean isLogout) {
		mIsLogout = isLogout;
	}

	public void updateAccountList() {
		mAccountList = new ArrayList<UmipayAccount>();
		try {
			if (mSerializeableManager != null) {
				String[] keys = mSerializeableManager.getKeys();
				if (keys != null) {
					for (String accountKey : keys) {
						UmipayAccount account = new UmipayAccount(accountKey);
						if (mSerializeableManager.getCache(account)) {
							mAccountList.add(account);
						}
					}
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	public UmipayAccount getAccountByUserName(String username) {
		if (TextUtils.isEmpty(username)) {
			return null;
		}
		try {
			List<UmipayAccount> list = getAccountList();
			if (list != null) {
				for (UmipayAccount umipayAccount : list) {
					if (username.equals(umipayAccount.getUserName())) {
						return umipayAccount;
					}
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	public UmipayAccount getAccountByOauthId(String oauthid) {
		if (TextUtils.isEmpty(oauthid)) {
			return null;
		}
		try {
			List<UmipayAccount> list = getAccountList();
			if (list != null) {
				for (UmipayAccount umipayAccount : list) {
					if (oauthid.equals(umipayAccount.getOauthID())) {
						return umipayAccount;
					}
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	public UmipayAccount getAccountByOauthId(String oauthid, int type) {
		if (TextUtils.isEmpty(oauthid)) {
			return null;
		}
		try {
			List<UmipayAccount> list = getAccountList();
			if (list != null) {
				for (UmipayAccount umipayAccount : list) {
					if (oauthid.equals(umipayAccount.getOauthID()) && type == umipayAccount.getOauthType()) {
						return umipayAccount;
					}
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	public List<UmipayAccount> getAccountList() {
		if (mAccountList != null && !mAccountList.isEmpty()) {
			sort(mAccountList);
		}
		return mAccountList;
	}

	private void sort(List<UmipayAccount> list) {
		Comparator<UmipayAccount> comparator = new Comparator<UmipayAccount>() {
			@Override
			public int compare(UmipayAccount lhs, UmipayAccount rhs) {
				try {
					if (lhs.getLastLoginTime_ms() > rhs.getLastLoginTime_ms()) {
						return -1;
					} else if (lhs.getLastLoginTime_ms() == rhs
							.getLastLoginTime_ms()) {
						return 0;
					} else {
						return 1;
					}
				} catch (Throwable e) {
					return 0;
				}
			}
		};
		Collections.sort(list, comparator);
	}

	public boolean saveAccount(UmipayAccount account) {
		boolean res = false;
		UmipayAccount saveAccount = new UmipayAccount(account);
		try {
			if (saveAccount != null) {
				if (!saveAccount.isRemenberPsw()) {
					saveAccount.setPsw("");
				}
			}
			res = mSerializeableManager.saveCache(saveAccount);
			for (UmipayAccount item : mAccountList) {
				if (item.getUserName().equals(saveAccount.getUserName())) {
					mAccountList.remove(item);
					break;
				}
			}
			mAccountList.add(saveAccount);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return res;
	}

	public boolean deleteAccount(UmipayAccount deleteAccount) {
		boolean res = false;
		try {
			if (deleteAccount != null) {
				res = mSerializeableManager.removeCacheByCacheKey(deleteAccount.getCacheKey());
				mAccountList.remove(deleteAccount);
				if (deleteAccount.isRemenberPsw()) {
					LocalPasswordManager.getInstance(mContext).removePassword(deleteAccount.getUserName());
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return res;
	}

	/**
	 * 获取非第三方登录列表
	 *
	 * @return
	 */
	public List<UmipayAccount> getNormalAccountList() {
		List<UmipayAccount> list = getAccountList();
		List<UmipayAccount> normalAccounts = new ArrayList<UmipayAccount>();
		for (UmipayAccount item : list) {
			if (item.getOauthType() == 0) {
				normalAccounts.add(item);
			}
		}
		return normalAccounts;
	}

	public UmipayAccount getFirstNormalAccount() {
		UmipayAccount account = getCurrentAccount();
		if (account != null && account.getOauthType() == 0) {
			return account;
		} else {
			account = null;
			List<UmipayAccount> normalList = getNormalAccountList();
			if (normalList.size() > 0) {
				account = normalList.get(0);
			}
		}
		return account;
	}

	public UmipayAccount getFirstAccount() {
		UmipayAccount account = null;
		List<UmipayAccount> accountList = getAccountList();
		if (accountList != null && accountList.size() > 0) {
			account = accountList.get(0);
		}
		return account;
	}

}
