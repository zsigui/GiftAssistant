package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zsigui on 16-8-24.
 */
public class ServerInfo extends GameDownloadInfo{

    @SerializedName("server_name")
    public String serverName;

    @SerializedName("time")
    public String time;

    @SerializedName("test_type")
    public String testType;

    @SerializedName("operator")
    public String operator;

    @SerializedName("game_type")
    public String gameType;
}
