package net.ouwan.umipay.android.handler;

import net.youmi.android.libs.webjs.js.base.extendjs.JsModel_Browser_Target_Basic_Extend_Js_Interface_Factory;
import net.youmi.android.libs.webjs.js.interfaces.Interface_Js_Handler;

/**
 * JsModel_Browser_PayExtent_Js_Interface_Factory
 *
 * @author zacklpx
 *         date 15-3-18
 *         description
 */
public class JsModel_Browser_PayExtent_Js_Interface_Factory extends
		JsModel_Browser_Target_Basic_Extend_Js_Interface_Factory {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 是否可以处理目标js目录
	 *
	 * @param cat
	 * @return 如果不能处理返回false，否则返回true
	 */
	@Override
	public boolean canYouHandleThisCatalog(int cat) {
		if (cat == 201) {
			return true;
		}
		return false;
	}

	@Override
	public Interface_Js_Handler getActionHanlder(int cat, int action) {
		if (cat == 201) {
			switch (action) {
				case 0:
					return new JsHandler_Pay_With_Payeco();
				case 1:
					return new JsHandler_Pay_With_Alipay();
				case 2:
					return null;
				case 3:
					return new JsHandler_CloseView_SetVisibility();
				case 4:
					return new JsHandler_Pay_CallBack();
				case 5:
					return new JsHandler_Pay_With_UPMP();
				case 6:
					return new JsHandler_Get_SupportPayType();
				case 7:
					return new JsHandler_UnInstall_ShortCut();
				case 8:
					return new JsHandler_FloatMenu_Save_Readed();
				case 9:
					return new JsHandler_FloatMenu_Copy_To_Clipboar();
				case 11:
					return new JsHandler_Logout_CloseBrowser();
				case 12:
					return new JsHandler_Pay_With_AlipaySDK();
				case 13:
					return new JsHandler_Push_Notification();
				case 14:
					return new JsHandler_Pay_With_WECHAT();
				case 15:
					return new JsHandler_Action_CallBack();
				default:
					break;
			}
		}
		return null;
	}
}
