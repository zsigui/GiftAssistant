package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 15-12-16.
 */
public class UserModel implements Serializable {

    @SerializedName("sess")
    public UserSession userSession;

    @SerializedName("info")
    public UserInfo userInfo;

    @SerializedName("account_list")
    public ArrayList<BindAccount> bindAccounts;

    @SerializedName("token")
    public String token;
}
