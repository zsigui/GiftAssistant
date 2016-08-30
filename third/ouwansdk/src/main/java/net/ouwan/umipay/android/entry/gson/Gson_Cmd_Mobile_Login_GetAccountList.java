package net.ouwan.umipay.android.entry.gson;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;

import java.util.List;

/**
 * Gson_Cmd_Login
 *
 * @author zacklpx
 *         date 15-4-24
 *         description
 */
public class Gson_Cmd_Mobile_Login_GetAccountList extends Gson_Base<Gson_Cmd_Mobile_Login_GetAccountList.Cmd_Account_List_Data> {

	public Gson_Cmd_Mobile_Login_GetAccountList(Context context, int code, String message, Cmd_Account_List_Data cmd_login_data) {
		super(context, code, message, cmd_login_data);
	}

	@Override
	public boolean checkData() {
		if (mCode == UmipaySDKStatusCode.SUCCESS && super.checkData()) {
			return !(mData.getAccountList() == null || mData.getAccountList().size() <= 0);
		} else {
			return super.checkData();
		}
	}
	public class Cmd_Account_List_Data {
		@SerializedName("userinfo")
		private List<Cmd_Account_List_Item_Data> mAccountList;
		@SerializedName("ts")
		private int ts;

		public List<Cmd_Account_List_Item_Data> getAccountList() {
			return mAccountList;
		}

		public void setAccountList(List<Cmd_Account_List_Item_Data> accountList) {
			mAccountList = accountList;
		}

		public int getTs() {
			return ts;
		}

		public void setTs(int ts) {
			this.ts = ts;
		}
	}

	public class Cmd_Account_List_Item_Data {
		@SerializedName("uid")
		private int uid;
		@SerializedName("username")
		private String username;
		@SerializedName("type")
		private int type;

		private String calling_code;
		private String mobile;
		private int ts;

		public int getUid() {
			return uid;
		}

		public void setUid(int uid) {
			this.uid = uid;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public int getTs() {
			return ts;
		}

		public void setTs(int ts) {
			this.ts = ts;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public void setMobile(String mobile){
			this.mobile = mobile;
		}

		public String getMobile(){
			return this.mobile;
		}

		public void setCallingCode(String calling_code){
			this.calling_code = calling_code;
		}

		public String getCallingCode(){
			return this.calling_code;
		}

	}
}
