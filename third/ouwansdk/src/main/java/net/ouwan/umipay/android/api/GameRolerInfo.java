package net.ouwan.umipay.android.api;

/**
 * 游戏角色信息
 */
public class GameRolerInfo {
	/**
	 * 登陆游戏角色的时候
	 */
	public final static int AT_LOGIN_GAME = 1;
	/**
	 * 游戏角色升级的时候
	 */
	public final static int AT_LEVEL_UP = 2;
	/**
	 * 创建游戏角色的时候
	 */
	public final static int AT_CREATE_ROLE = 3;

	private String mServerId;
	private String mServerName;
	private String mRoleId;
	private String mRoleName;
	private String mRoleLevel;
	private String mVip;
	private int mBalance;
	private static GameRolerInfo mCurrentGameRolerInfo;

	public static synchronized GameRolerInfo getCurrentGameRolerInfo() {
		return mCurrentGameRolerInfo;
	}

	public static synchronized void setCurrentGameRolerInfo(GameRolerInfo info) {
		mCurrentGameRolerInfo = info;
	}

	public String getServerId() {
		return mServerId;
	}

	public void setServerId(String ServerId) {
		this.mServerId = ServerId;
	}

	public String getServerName() {
		return mServerName;
	}

	public void setServerName(String ServerName) {
		this.mServerName = ServerName;
	}

	public String getRoleId() {
		return mRoleId;
	}

	public void setRoleId(String RoleId) {
		this.mRoleId = RoleId;
	}

	public String getRoleName() {
		return mRoleName;
	}

	public void setRoleName(String RoleName) {
		this.mRoleName = RoleName;
	}

	public String getRoleLevel() {
		return mRoleLevel;
	}

	public void setRoleLevel(String RoleLevel) {
		this.mRoleLevel = RoleLevel;
	}

	public String getVip() {
		return mVip;
	}

	public void setVip(String vip) {
		mVip = vip;
	}

	public int getBalance() {
		return mBalance;
	}

	public void setBalance(int balance) {
		mBalance = balance;
	}
}
