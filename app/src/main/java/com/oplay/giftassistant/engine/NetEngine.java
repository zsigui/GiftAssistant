package com.oplay.giftassistant.engine;

import com.oplay.giftassistant.model.DataModel;
import com.oplay.giftassistant.model.HomeModel;
import com.oplay.giftassistant.model.UserModel;

import java.util.List;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by zsigui on 15-12-16.
 */
public interface NetEngine {


	@GET("test/user/{user}")
	Call<DataModel<UserModel>> obtainUser(@Path("user") String user);

	@GET("test/getusers/new")
	Call<DataModel<List<UserModel>>> obtainNewUser();

	@GET("test/getusers")
	Call<DataModel<List<UserModel>>> obtainAllUser();

	@POST("test/{user}/post")
	Call<DataModel<Object>> createUser(@Path("user") String name, @Body UserModel user);

	@POST("test/{user}/update")
	@FormUrlEncoded
	Call<DataModel<Object>> postUser(@Path("user") String name, @Field("user") String user, @Field("pwd") String pwd);

	@GET("test/getjson")
	Call<DataModel<HomeModel>> getHomeData(@Query("token") String token);

	@POST("test/data")
	Call<DataModel<Object>> postJson(@Body DataModel pwd);
}
