package net.ouwan.umipay.android.api;

public class UmipaymentInfo {

	public static final int SERVICE_TYPE_RATE = 0;
	public static final int SERVICE_TYPE_QUOTA = 1;

	private String mCustomInfo;
	/**
	 * @Fields mServerId : 服务器id
	 */
	private String mServerId;
	/**
	 * @Fields mAmount : 充值金币数
	 */
	private int mAmount;
	/**
	 * @Fields mRoleId : 游戏角色Id
	 */
	private String mRoleId;
	/**
	 * @Fields mRoleName : 游戏角色名
	 */
	private String mRoleName;
	/**
	 * @Fields mRoleGrade : 游戏角色级别
	 */
	private String mRoleGrade;
	/**
	 * @Fields mAreadId : 游戏大区id
	 */
	private String mAreadId;
	//V3.00版本添加
	/**
	 * @Fields mServiceType 业务类型
	 */
	private int mServiceType;
	/**
	 * @Fields mDesc 订单描述
	 */
	private String mDesc;
	/**
	 * @Fields mTradeno 外部订单号
	 */
	private String mTradeno;
	/**
	 * @Fields mPayMoney 定额支付的支付金额
	 */
	private int mPayMoney;
	//V3.00版本添加
	/**
	 * false:支付完成会允许继续充值；
	 * true： 支付完成后关闭支付界面,不能继续充值（）
	 */
	private boolean mSinglePayMode;

	/**
	 * 充值时选择的最小金额,低于最小金额的无法支付
	 */
	private int mMinFee = 0;

	private boolean mPaySetSinglePayMode;
	private boolean mPaySetMinFee;

	public UmipaymentInfo() {

	}

	/**
	 * 获取自定义数据
	 *
	 * @return
	 */
	public String getCustomInfo() {
		return mCustomInfo;
	}

	/**
	 * 游戏开发商自定义数据，可选。该值将在用户充值成功后，在充值回调接口通知给游戏开发商时携带该数据
	 *
	 * @param mCustomInfo 128个可打印字符
	 */
	public void setCustomInfo(String mCustomInfo) {
		this.mCustomInfo = mCustomInfo;
	}

	/**
	 * 获取服务器ID
	 *
	 * @return
	 */
	public String getServerId() {
		return mServerId;
	}

	/**
	 * 设置服务器ID
	 *
	 * @param mServerId 用户充值的逻辑服务器的内部ID。
	 *                  俗称的开服列表单个服的内部ID（比如有米1服的ID是1001，用户在哪个服充值，就将这个服的内部ID传过来）
	 */
	public void setServerId(String mServerId) {
		this.mServerId = mServerId;
	}

	/**
	 * 获取充值金币数
	 *
	 * @return
	 */
	public int getAmount() {
		return mAmount;
	}

	/**
	 * 设置充值金币数
	 * ps：由于充值界面上能够改变充值金额，因此这个金额是非最终充值数额，最终充值额度以服务器通知结果为准
	 *
	 * @param mAmount
	 */
	public void setAmount(int mAmount) {
		this.mAmount = mAmount;
	}

	/**
	 * 获取角色ID
	 *
	 * @return
	 */
	public String getRoleId() {
		return mRoleId;
	}

	/**
	 * 设置角色ID
	 *
	 * @param mRoleId 角色信息的内部标识ID
	 */
	public void setRoleId(String mRoleId) {
		this.mRoleId = mRoleId;
	}

	/**
	 * 获取角色ID
	 *
	 * @return
	 */
	public String getRoleName() {
		return mRoleName;
	}

	/**
	 * 设置角色名
	 *
	 * @param mRoleName 角色信息的名字
	 */
	public void setRoleName(String mRoleName) {
		this.mRoleName = mRoleName;
	}

	/**
	 * 获取角色等级信息
	 *
	 * @return
	 */
	public String getRoleGrade() {
		return mRoleGrade;
	}

	/**
	 * 设置角色等级信息
	 *
	 * @param mRoleGrade 角色信息的等级信息
	 */
	public void setRoleGrade(String mRoleGrade) {
		this.mRoleGrade = mRoleGrade;
	}

	/**
	 * 获取角色所在区
	 *
	 * @return
	 */
	public String getAreadId() {
		return mAreadId;
	}

	/**
	 * 设置角色所在区
	 *
	 * @param mAreadId 角色信息的区信息。
	 *                 （如有米1服3区的区ID是0003，用户在哪个区充值，就将这个区的内部ID传过来）
	 */
	public void setAreadId(String mAreadId) {
		this.mAreadId = mAreadId;
	}

	/**
	 * 单次支付
	 *
	 * @return
	 */
	public boolean IsSinglePayMode() {
		return mSinglePayMode;
	}

	/**
	 * 设置单次支付
	 * ps：如果支持充值界面多次充值，在服务器回调给游戏服务器时，cdata内容有可能是重复的
	 *
	 * @param isSinglePayMode true:用户充值完成后自动关闭充值界面，无法继续进行充值
	 *                        false：支付完成后用户可以允许继续充值
	 */
	public void setSinglePayMode(boolean isSinglePayMode) {
		this.mSinglePayMode = isSinglePayMode;
		this.mPaySetSinglePayMode = true;
	}

	/**
	 * 获取最小支付金额
	 *
	 * @return
	 */
	public int getMinFee() {
		return mMinFee;
	}

	/**
	 * 设置最小支付金额
	 *
	 * @param minFee 定义用户每次充值至少需要充值minCost RMB
	 */
	public void setMinFee(int minFee) {
		this.mMinFee = minFee;
		this.mPaySetMinFee = true;
	}

	/**
	 * 获取业务类型 0：按汇率兑换虚拟货币  1： 固定额度充值(不能修改余额) V2.2 固定额度充值只能单次支付
	 *
	 * @return
	 */
	public int getServiceType() {
		return mServiceType;
	}

	/**
	 * 获取业务类型 0：按汇率兑换虚拟货币  1： 固定额度充值(不能修改余额) V2.2 固定额度充值只能单次支付
	 *
	 * @param ServiceType
	 */
	public void setServiceType(int ServiceType) {
		this.mServiceType = ServiceType;
	}

	/**
	 * 获取订单描述
	 *
	 * @return
	 */
	public String getDesc() {
		return mDesc;
	}

	/**
	 * 设置订单描述
	 *
	 * @param Desc
	 */
	public void setDesc(String Desc) {
		this.mDesc = Desc;
	}

	/**
	 * 获取外部订单号
	 *
	 * @return
	 */
	public String getTradeno() {
		return mTradeno;
	}

	/**
	 * 设置外部订单号
	 *
	 * @param Tradeno
	 */
	public void setTradeno(String Tradeno) {
		this.mTradeno = Tradeno;
	}

	/**
	 * 获取支付金额 单位：RMB
	 *
	 * @return
	 */
	public int getPayMoney() {
		return mPayMoney;
	}

	/**
	 * 设置支付金额 单位：RMB
	 *
	 * @param PayMoney
	 */
	public void setPayMoney(int PayMoney) {
		this.mPayMoney = PayMoney;
	}

	/**
	 * 使用UmiPaymentInfo的SinglePayMode
	 *
	 * @return
	 */
	public boolean IsPaySetSinglePayMode() {
		return mPaySetSinglePayMode;
	}

	/**
	 * 使用UmiPaymentInfo的MinFee
	 *
	 * @return
	 */
	public boolean IsPaySetMinFee() {
		return mPaySetMinFee;
	}

	@Override
	public String toString() {
		return "UmipaymentInfo{" +
				"mCustomInfo='" + mCustomInfo + '\'' +
				", mServerId='" + mServerId + '\'' +
				", mAmount=" + mAmount +
				", mRoleId='" + mRoleId + '\'' +
				", mRoleName='" + mRoleName + '\'' +
				", mRoleGrade='" + mRoleGrade + '\'' +
				", mAreadId='" + mAreadId + '\'' +
				", mServiceType=" + mServiceType +
				", mDesc='" + mDesc + '\'' +
				", mTradeno='" + mTradeno + '\'' +
				", mPayMoney=" + mPayMoney +
				", mSinglePayMode=" + mSinglePayMode +
				", mMinFee=" + mMinFee +
				", mPaySetSinglePayMode=" + mPaySetSinglePayMode +
				", mPaySetMinFee=" + mPaySetMinFee +
				'}';
	}
}
