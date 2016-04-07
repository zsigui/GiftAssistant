package com.oplay.giftcool.model;

/**
 * Created by zsigui on 16-4-7.
 */
public class EmojiModel {

	/**
	 * Emoji规格串开始下标
	 */
	private int mStartIndex;
	/**
	 * Emoji规格串结束下标
	 */
	private int mEndIndex;
	/**
	 * Emoji规格串
	 */
	private String mPhrase;
	/**
	 * Emoji图片名称
	 */
	private String mImageName;

	public int getStartIndex() {
		return mStartIndex;
	}

	public void setStartIndex(int startIndex) {
		mStartIndex = startIndex;
	}

	public int getEndIndex() {
		return mEndIndex;
	}

	public void setEndIndex(int endIndex) {
		mEndIndex = endIndex;
	}

	public String getPhrase() {
		return mPhrase;
	}

	public void setPhrase(String phrase) {
		mPhrase = phrase;
	}

	public String getImageName() {
		return mImageName;
	}

	public void setImageName(String imageName) {
		mImageName = imageName;
	}
}
