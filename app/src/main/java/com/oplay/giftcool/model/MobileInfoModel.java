package com.oplay.giftcool.model;

/**
 * Created by zsigui on 15-12-25.
 */
public class MobileInfoModel {
	private static MobileInfoModel instance;

	private MobileInfoModel(){}

	private String imei;
	private String imsi;
	private String cid;
	private String mac;
	private String apn;
	private String cn;
	private String dd;
	private String dv;
	private String os;
	private int version;
	private int chn;
	private boolean isInit;

	public static MobileInfoModel getInstance() {
		if (instance == null) {
			instance = new MobileInfoModel();
		}
		return instance;
	}


	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getApn() {
		return apn;
	}

	public void setApn(String apn) {
		this.apn = apn;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getDd() {
		return dd;
	}

	public void setDd(String dd) {
		this.dd = dd;
	}

	public String getDv() {
		return dv;
	}

	public void setDv(String dv) {
		this.dv = dv;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public int getChn() {
		return chn;
	}

	public void setChn(int chn) {
		this.chn = chn;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public static void setInstance(MobileInfoModel instance) {
		MobileInfoModel.instance = instance;
	}

	public boolean isInit() {
		return isInit;
	}

	public void setInit(boolean isInit) {
		this.isInit = isInit;
	}
}
