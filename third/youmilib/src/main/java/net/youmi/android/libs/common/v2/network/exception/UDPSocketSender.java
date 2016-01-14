package net.youmi.android.libs.common.v2.network.exception;

import net.youmi.android.libs.common.debug.Debug_SDK;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author zhitao
 * @since 2015-09-10 17:20
 */
public class UDPSocketSender {

	public static void send(JSONObject jsonObject, String monitor_host, int monitor_port) {

		try {
			DatagramSocket socket = new DatagramSocket(monitor_port);
			InetAddress serverAddress = InetAddress.getByName(monitor_host);
			byte data[] = jsonObject.toString().getBytes();
			DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, monitor_port);
			socket.send(packet);
			socket.close();
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, UDPSocketSender.class, e);
			}
		}
	}
}
