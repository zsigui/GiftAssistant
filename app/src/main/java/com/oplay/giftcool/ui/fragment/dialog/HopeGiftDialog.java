package com.oplay.giftcool.ui.fragment.dialog;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;

/**
 * Created by zsigui on 16-3-4.
 */
public class HopeGiftDialog extends BaseFragment_Dialog implements TextWatcher {

	private int mGameId = 0;
	private String mName;
	private String mNote;
	private boolean mCanEditName = true;


	private EditText etName;
	private EditText etNote;

	public static HopeGiftDialog newInstance(int id, String name, boolean canEditName) {
		HopeGiftDialog dialog = new HopeGiftDialog();
		dialog.setGameId(id);
		dialog.setName(name);
		dialog.setCanEditName(canEditName);
		return dialog;
	}

	@Override
	protected void initView() {
		setContentView(R.layout.dialog_hope_gift);
		etName = getViewById(R.id.tv_name);
		etNote = getViewById(R.id.tv_note);
	}

	@Override
	protected void processLogic() {
		setTitle(getResources().getString(R.string.st_dialog_hope_gift_title));
		setPositiveBtnText(getResources().getString(R.string.st_dialog_hope_gift_btn_confirm));
		etName.addTextChangedListener(this);
		setName(mName);
		setNote(mNote);
		setCanEditName(mCanEditName);
	}

	public void setGameId(int id) {
		mGameId = id;
	}

	public void setName(String name) {
		mName = name;
		if (etName != null) {
			etName.setText(mName);
		}
	}

	public void setNote(String note) {
		mNote = note;
		if (etNote != null) {
			etNote.setText(mNote);
		}
	}

	public void setCanEditName(boolean canEditName) {
		mCanEditName = canEditName;
		if (etName != null) {
			etName.setEnabled(mCanEditName);
		}
	}

	public int getGameId() {
		return mGameId;
	}

	public String getName() {
		if (etName != null) {
			return etName.getText().toString().trim();
		}
		return mName;
	}

	public String getNote() {
		if (etNote != null) {
			return etNote.getText().toString().trim();
		}
		return mNote;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.toString().trim().length() > 0) {
			btnPositive.setEnabled(true);
		} else {
			btnPositive.setEnabled(false);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {

	}
}
