package net.ouwan.umipay.android.entry.gson;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Gson_Cmd_GetPush
 *
 * @author zacklpx
 *         date 15-5-4
 *         description
 */
public class Gson_Cmd_RedPoint extends Gson_Base<Gson_Cmd_RedPoint.Cmd_RedPoint_Data> {

	public Gson_Cmd_RedPoint(Context context, int code, String message, Cmd_RedPoint_Data cmd_redPoint_data) {
		super(context, code, message, cmd_redPoint_data);
	}

	public class Cmd_RedPoint_Data {
		@SerializedName("notice")
		private List<String> mNotices;
		@SerializedName("board")
		private List<String> mBoards;
		@SerializedName("gift")
		private List<String> mGift;
		@SerializedName("trumpet")
		private List<String> mTrumpet;

		public List<String> getNotices() {
			return mNotices;
		}

		public void setNotices(List<String> notices) {
			mNotices = notices;
		}

		public List<String> getBoards() {
			return mBoards;
		}

		public void setBoards(List<String> boards) {
			mBoards = boards;
		}

		public List<String> getGift() {
			return mGift;
		}

		public void setGift(List<String> gift) {
			mGift = gift;
		}
		public List<String> getTrumpet() {
			return mTrumpet;
		}

		public void setTrumpet(List<String> trumpet) {
			mTrumpet = trumpet;
		}
	}
}
