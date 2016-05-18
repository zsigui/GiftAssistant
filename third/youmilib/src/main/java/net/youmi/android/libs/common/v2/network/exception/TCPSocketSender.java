package net.youmi.android.libs.common.v2.network.exception;

import net.youmi.android.libs.common.debug.Debug_SDK;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.Socket;

/**
 * @author zhitao
 * @since 2015-09-10 17:20
 */
public class TCPSocketSender {

	public static void send(JSONObject jsonObject, String monitor_host, int monitor_port) {

		try {
			Socket socket = new Socket(monitor_host, monitor_port);
			OutputStream outputStream = socket.getOutputStream();
			byte data[] = jsonObject.toString().getBytes();
			outputStream.write(data);
			outputStream.flush();
			outputStream.close();
			socket.close();

		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, TCPSocketSender.class, e);
			}
		}
	}
}
