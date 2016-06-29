package com.oplay.giftcool.listener;

/**
 * Created by zsigui on 16-3-23.
 */
public interface ToolbarListener {

	void showRightBtn(int visibility, String text);

	void setRightBtnEnabled(boolean enabled);

	void setHandleListener(OnHandleListener handleListener);
}
