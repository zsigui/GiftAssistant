package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 16-1-7.
 */
public class ScoreMissionList implements Serializable {

	@SerializedName("mission_list")
	public ArrayList<ScoreMission> missions;
}
