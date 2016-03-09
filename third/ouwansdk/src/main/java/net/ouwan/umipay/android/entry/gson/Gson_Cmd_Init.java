package net.ouwan.umipay.android.entry.gson;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;

/**
 * Gson_Cmd_Init
 *
 * @author zacklpx
 *         date 15-4-10
 *         description
 */
public class Gson_Cmd_Init extends Gson_Base<Gson_Cmd_Init.Cmd_Init_Data> {

	public Gson_Cmd_Init(Context context, int code, String message, Cmd_Init_Data cmd_init_data) {
		super(context, code, message, cmd_init_data);
	}

	@Override
	public boolean checkData() {
		return mCode != UmipaySDKStatusCode.SUCCESS || super.checkData() && mData.getConfig() != null && mData
				.getConfig().getOuwan() != null;
	}

	public class Cmd_Init_Data {
		@SerializedName("status")
		private int status;
		@SerializedName("config")
		private Cmd_Init_Data_Config config;

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public Cmd_Init_Data_Config getConfig() {
			return config;
		}

		public void setConfig(Cmd_Init_Data_Config config) {
			this.config = config;
		}
	}

	public class Cmd_Init_Data_Config {
		@SerializedName("iseal")
		private int iseal;
		@SerializedName("iserp")
		private int iserp;
		@SerializedName("iseqr")
		private int iseqr;
		@SerializedName("isevp")
		private int isevp;
		@SerializedName("isevm")
		private int isevm;
		@SerializedName("isefm")
		private int isefm;
		@SerializedName("https")
		private int https;
		@SerializedName("show3rd")
		private int show3rd;
		@SerializedName("showgift")
		private int showgift;
		@SerializedName("showmsg")
		private int showmsg;
		@SerializedName("showbbs")
		private int showbbs;
		@SerializedName("showhelp")
		private int showhelp;
		@SerializedName("showaccount")
		private int showaccount;
		@SerializedName("uploaderrmsg")
		private int uploaderrmsg;
		@SerializedName("redpointtime")
		private int redpointtime;
		@SerializedName("epayidentify")
		private String epayidentify;
		@SerializedName("ouwan")
		private Cmd_Init_Data_Config_Ouwan ouwan;

		public int getIseal() {
			return iseal;
		}

		public void setIseal(int iseal) {
			this.iseal = iseal;
		}

		public int getIserp() {
			return iserp;
		}

		public void setIserp(int iserp) {
			this.iserp = iserp;
		}

		public int getIseqr() {
			return iseqr;
		}

		public void setIseqr(int iseqr) {
			this.iseqr = iseqr;
		}

		public int getIsevp() {
			return isevp;
		}

		public void setIsevp(int isevp) {
			this.isevp = isevp;
		}

		public int getIsevm() {
			return isevm;
		}

		public void setIsevm(int isevm) {
			this.isevm = isevm;
		}

		public int getIsefm() {
			return isefm;
		}

		public void setIsefm(int isefm) {
			this.isefm = isefm;
		}

		public int getHttps() {
			return https;
		}

		public void setHttps(int https) {
			this.https = https;
		}

		public int getShow3rd() {
			return show3rd;
		}

		public void setShow3rd(int show3rd) {
			this.show3rd = show3rd;
		}

		public int getShowgift() {
			return showgift;
		}

		public void setShowgift(int showgift) {
			this.showgift = showgift;
		}

		public int getShowmsg() {
			return showmsg;
		}

		public void setShowmsg(int showmsg) {
			this.showmsg = showmsg;
		}

		public int getShowbbs() {
			return showbbs;
		}

		public void setShowbbs(int showbbs) {
			this.showbbs = showbbs;
		}

		public int getShowhelp() {
			return showhelp;
		}

		public void setShowhelp(int showhelp) {
			this.showhelp = showhelp;
		}

		public int getShowaccount() {
			return showaccount;
		}

		public void setShowaccount(int showaccount) {
			this.showaccount = showaccount;
		}

		public int getRedpointtime() {
			return redpointtime;
		}

		public void setRedpointtime(int redpointtime) {
			this.redpointtime = redpointtime;
		}

		public String getEpayidentify() {
			return epayidentify;
		}

		public void setEpayidentify(String epayidentify) {
			this.epayidentify = epayidentify;
		}

		public Cmd_Init_Data_Config_Ouwan getOuwan() {
			return ouwan;
		}

		public void setOuwan(Cmd_Init_Data_Config_Ouwan ouwan) {
			this.ouwan = ouwan;
		}

		public void setEnableErrorReport(int uploaderrmsg){
			this.uploaderrmsg = uploaderrmsg;
		}

		public int getEnableErrorReport(){
			return this.uploaderrmsg;
		}
	}

	public class Cmd_Init_Data_Config_Ouwan {
		@SerializedName("packagename")
		private String mPackageName;
		@SerializedName("communityurl")
		private String mCommunityUrl;
		@SerializedName("communitytext")
		private String mCommunityText;
		@SerializedName("downloadurl")
		private String mDownloadUrl;
		@SerializedName("downloadtext")
		private String mDownloadText;

		public String getPackageName() {
			return mPackageName;
		}

		public void setPackageName(String packageName) {
			mPackageName = packageName;
		}

		public String getCommunityUrl() {
			return mCommunityUrl;
		}

		public void setCommunityUrl(String communityUrl) {
			mCommunityUrl = communityUrl;
		}

		public String getCommunityText() {
			return mCommunityText;
		}

		public void setCommunityText(String communityText) {
			mCommunityText = communityText;
		}

		public String getDownloadUrl() {
			return mDownloadUrl;
		}

		public void setDownloadUrl(String downloadUrl) {
			mDownloadUrl = downloadUrl;
		}

		public String getDownloadText() {
			return mDownloadText;
		}

		public void setDownloadText(String downloadText) {
			mDownloadText = downloadText;
		}
	}
}
