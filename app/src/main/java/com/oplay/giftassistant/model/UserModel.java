package com.oplay.giftassistant.model;

/**
 * Created by zsigui on 15-12-16.
 */
public class UserModel {
	public String pwd;
	public String name;
	public String img;

	public UserModel() {
	}

	public UserModel(String pwd, String name, String imgUrl) {
		this.pwd = pwd;
		this.name = name;
		this.img = imgUrl;
	}
}
