package net.ouwan.umipay.android.entry.gson;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Gson_Cmd_GetAccountList
 *
 * @author zacklpx
 *         date 15-4-22
 *         description
 */
public class Gson_Cmd_GetAccountList extends Gson_Base<Gson_Cmd_GetAccountList.Cmd_GetAccountList_Data> {

	public Gson_Cmd_GetAccountList(Context context, int code, String message, Cmd_GetAccountList_Data
			cmd_getAccountList_data) {
		super(context, code, message, cmd_getAccountList_data);
	}

	public class Cmd_GetAccountList_Data {
		@SerializedName("username")
		private List<String> mAccountList;

		public List<String> getAccountList() {
			return mAccountList;
		}

		public void setAccountList(List<String> accountList) {
			mAccountList = accountList;
		}
	}
}
