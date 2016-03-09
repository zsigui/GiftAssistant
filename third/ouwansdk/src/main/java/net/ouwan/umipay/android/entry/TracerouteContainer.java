/*
This file is part of the project TraceroutePing, which is an Android library
implementing Traceroute with ping under GPL license v3.
Copyright (C) 2013  Olivier Goutay

TraceroutePing is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

TraceroutePing is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with TraceroutePing.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.ouwan.umipay.android.entry;

import java.io.Serializable;

/**
 * 
 * @author Olivier Goutay
 * 
 */
public class TracerouteContainer implements Serializable {

	private String hostname;
	private String ipToPing;
	private String ip;
	private float ms;
	private int ttl;
	private boolean isSuccessful;

	public TracerouteContainer(String hostname, String ipToPing, String ip,float ms, boolean isSuccessful, int ttl) {
		this.hostname = hostname;
		this.ip = ip;
		this.ipToPing = ipToPing;
		this.ms = ms;
		this.isSuccessful = isSuccessful;
		this.ttl = ttl;
	}

	public boolean getIsSuccessful(){
		return isSuccessful;
	}

	public String getIpToPing(){
		return ipToPing;
	}

	public String getIp(){
		return ip;
	}

	public int getTTL(){
		return ttl;
	}

	@Override
	public String toString() {
		String str = (isSuccessful)?(" ,ipToPing="+ipToPing+" ,Hostname=" + hostname + " ,ip=" + ip + " ,Milliseconds=" + ms+" ms;;"):" is fail;;";
		return "#Traceroute"+":ttl= "+ttl+str;
	}
}
