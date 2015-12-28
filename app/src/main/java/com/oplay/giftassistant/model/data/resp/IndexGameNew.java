package com.oplay.giftassistant.model.data.resp;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexGameNew {

	// 游戏图标
	public String img;
	// 游戏名
	public String name;
	// 在玩人数
	public int playCount;
	// 拥有礼包数
	public int hasGiftCount;
	// 最新礼包名
	public String giftName;
	// 游戏大小，MB
	public float size;
	// 下载地址
	public String downloadUrl;
	// 下载验签地址
	// 验证下载文件是否正常，根据需求而定
	public String downlaodSign;

}
