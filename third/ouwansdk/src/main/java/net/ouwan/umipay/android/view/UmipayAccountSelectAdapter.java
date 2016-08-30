package net.ouwan.umipay.android.view;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Mobile_Login_GetAccountList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UmipayAccountSelectAdapter extends BaseAdapter {

	private ArrayList<Gson_Cmd_Mobile_Login_GetAccountList.Cmd_Account_List_Item_Data> mAccountList = new ArrayList<Gson_Cmd_Mobile_Login_GetAccountList.Cmd_Account_List_Item_Data>();
	private Activity mParentActivity;
	private Map<Integer, Boolean> mSelectMap;

	public UmipayAccountSelectAdapter(Activity activity, ArrayList<Gson_Cmd_Mobile_Login_GetAccountList.Cmd_Account_List_Item_Data> list) {
		this.mParentActivity = activity;
		this.mAccountList = list;
		mSelectMap = new HashMap<Integer, Boolean>();
		if(this.mAccountList != null){
			for(int i=0;i<mAccountList.size();i++){
				mSelectMap.put(i,false);
			}
			mSelectMap.put(0,true);//默认勾选
		}
	}

	@Override
	public int getCount() {
		return mAccountList.size();
	}

	@Override
	public Object getItem(int position) {
		return mAccountList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mParentActivity).inflate(Util_Resource.getIdByReflection
					(mParentActivity, "layout", "umipay_select_account_list_item"), null);
			holder.userNameTv = (TextView) convertView.findViewById(Util_Resource.getIdByReflection(mParentActivity,
					"id", "umipay_account_username_tv"));
			holder.typeTv = (TextView) convertView.findViewById(Util_Resource.getIdByReflection(mParentActivity,
					"id", "umipay_account_type_tv"));
			holder.selectCb = (CheckBox) convertView.findViewById(Util_Resource.getIdByReflection(mParentActivity,
					"id", "umipay_select_account_cb"));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		StringBuffer type = new StringBuffer();
		switch (mAccountList.get(position).getType()){
			case UmipayAccount.TYPE_NORMAL:
				type.append("偶玩游戏");
				break;
			case UmipayAccount.TYPE_QQ:
				type.append("QQ");
				break;
			case UmipayAccount.TYPE_SINA:
				type.append("微博");
				break;
			case UmipayAccount.TYPE_VISITOR:
				type.append("游客");
				break;
			case UmipayAccount.TYPE_QUICKREGISTER:
				type.append("一键注册");
				break;
			case UmipayAccount.TYPE_GIFT_COOL:
				type.append("礼包库");
				break;
			case UmipayAccount.TYPE_MOBILE:
				type.append("手机注册");
				break;

		}
		if(TextUtils.isEmpty(type)){
			type.append(mAccountList.get(position).getType());
		}
		holder.typeTv.setText(type);
		holder.userNameTv.setText(mAccountList.get(position).getUsername());

		if(holder.selectCb != null){
			holder.selectCb.setChecked(mSelectMap.get(position));
		}
		return convertView;
	}
	public void onItemClick(final int position){
		// 当前点击的CB为true
		// 先将所有的置为FALSE
		for(Integer p : mSelectMap.keySet()) {
			mSelectMap.put(p, false);
		}
		// 再将当前选择CB的为true
		mSelectMap.put(position, true);
		notifyDataSetChanged();
	}

	class ViewHolder {
		TextView userNameTv;
		TextView typeTv;
		CheckBox selectCb;
	}

}
