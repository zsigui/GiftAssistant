package com.oplay.giftcool.manager;

import android.content.Context;

import com.google.gson.Gson;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.model.MobileInfoModel;
import com.oplay.giftcool.model.data.resp.MissionReward;
import com.oplay.giftcool.util.SystemUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.socks.library.KLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by zsigui on 16-4-12.
 */
public class SocketIOManager {

	private final String URI_WS = "http://172.16.5.76:80/ws";
	private static SocketIOManager manager;
	private Context mAppContext;

	private SocketIOManager() {
		mAppContext = AssistantApp.getInstance().getApplicationContext();
	}

	public static SocketIOManager getInstance() {
		if (manager == null) {
			manager = new SocketIOManager();
		}
		return manager;
	}

	private Socket mSocket;
	private int mTryCount = 0;

	public void connectOrReConnect() {
		if (AccountManager.getInstance().isLogin()) {
			try {
				if (isConnected()) {
					KLog.d(AppDebugConfig.TAG_WARN, "need to reconnect");
					mSocket.disconnect();
					mSocket.connect();
					return;
				}
				mTryCount = 3;
				IO.Options opts = new IO.Options();
				opts.forceNew = true;
				opts.reconnection = false;
//				opts.reconnectionAttempts = 3;
//				// 断开后，间隔10秒再次重试
//				opts.reconnectionDelay = 10 * 1000;
				mSocket = IO.socket(URI_WS, opts);
				mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
					@Override
					public void call(Object... args) {
						KLog.d(AppDebugConfig.TAG_WARN, "connect.id = " + mSocket.id());
						if (AccountManager.getInstance().isLogin()) {
							// 发送验证消息
							try {
								JSONObject obj = new JSONObject();
								JSONObject data = new JSONObject();
								data.put("cuid", AccountManager.getInstance().getUserSesion().uid);
								data.put("sessionId", AccountManager.getInstance().getUserSesion().session);
								data.put("cid", MobileInfoModel.getInstance().getCid());
								data.put("imei", MobileInfoModel.getInstance().getImei());
								obj.put("d", data);
								mSocket.emit(CustomSocket.EVENT_LOGIN, obj);
							} catch (JSONException e) {
								if (AppDebugConfig.IS_DEBUG) {
									KLog.d(AppDebugConfig.TAG_MANAGER, e);
								}
							}
						} else {
							// 已经退出登录，则主动断开连接
							mSocket.disconnect();
						}
					}
				}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
					@Override
					public void call(Object... args) {
						KLog.d(AppDebugConfig.TAG_WARN, "disconnect = " + (args != null && args.length > 0 ? args[0] :
								null));
						retryConnect();
					}
				}).on(CustomSocket.EVENT_AUTH_ERROR, new Emitter.Listener() {

					@Override
					public void call(Object... args) {
						// 连接失败，如果是已经登录，重试连接，如果不是，不连接
						if (AccountManager.getInstance().isLogin()) {
							retryConnect();
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_MANAGER, "msg = " + (args != null && args.length > 0 ? args[0] :
									null));
						}
					}
				}).on(CustomSocket.EVENT_AUTH_SUCCESS, new Emitter.Listener() {
					@Override
					public void call(Object... args) {
						// 验证成功
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_MANAGER, "SocketIO验证通过");
						}
					}
				}).on(CustomSocket.EVENT_MISSION_COMPLETE, new Emitter.Listener() {
					@Override
					public void call(Object... args) {
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_WARN, "args[0] = " + (args != null && args.length > 0 ? args[0]
									: null));
						}
						if (!SystemUtil.isBackground(mAppContext) && args != null && args.length > 0) {
							// 处于前台中，直接发送通知
							try {
								Gson gson = AssistantApp.getInstance().getGson();
								final MissionReward reward = gson.fromJson(args[0].toString(), MissionReward.class);
								ThreadUtil.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										ScoreManager.getInstance().toastByCallback(reward, true);
									}
								});
							} catch (Exception ignored) {
							}
						}
					}
				});
				mSocket.connect();
			} catch (URISyntaxException e) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_MANAGER, e);
				}
			}
		}
	}

	private void retryConnect() {
		if (mTryCount > 0) {
			mSocket.connect();
			mTryCount--;
		}
	}

	public void close() {
		if (isConnected()) {
			mSocket.disconnect();
			mSocket = null;
		}
	}

	public boolean isConnected() {
		return mSocket != null && mSocket.connected();
	}

	public abstract class CustomSocket {
		public static final String EVENT_LOGIN = "login";
		public static final String EVENT_MISSION_COMPLETE = "mission_complete";
		public static final String EVENT_AUTH_SUCCESS = "auth_success";
		public static final String EVENT_AUTH_ERROR = "auth_error";
	}
}
