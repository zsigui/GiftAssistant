package net.ouwan.umipay.android.entry;

/**
 * Created by liangpeixing on 14-3-14.
 */
public class Visitor {
	private String mAccount;
	private String mToken;

	public Visitor(String Account, String Token) {
		this.mAccount = Account;
		this.mToken = Token;
	}

	public String getAccount() {
		return mAccount;
	}

	public void setAccount(String Account) {
		this.mAccount = Account;
	}

	public String getToken() {
		return mToken;
	}

	public void setToken(String Token) {
		this.mToken = Token;
	}
}
