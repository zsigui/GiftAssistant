package com.oplay.giftcool.engine;

import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.model.data.resp.CommentDetail;
import com.oplay.giftcool.model.data.resp.GameDetail;
import com.oplay.giftcool.model.data.resp.PostCommentList;
import com.oplay.giftcool.model.data.resp.PostDetail;
import com.oplay.giftcool.model.data.resp.PostVoteInfo;
import com.oplay.giftcool.model.json.base.JsonRespBase;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 设置不需要加密的连接请求(不适用json方式)
 * <p>
 * Created by zsigui on 16-4-13.
 */
public interface NoEncryptEngine {

    /**
     * 上传回复，带图片
     */
//	@Multipart
//	@POST(NetUrl.POST_REPLY)
//	Call<JsonRespBase<Void>> postReply(@PartMap HashMap<String, RequestBody> pics);
//
//	@POST(NetUrl.POST_REPLY)
//	Call<JsonRespBase<Void>> commitReply(@Body JsonReqBase<ReqCommitReply> reqData);
    @POST(NetUrl.POST_REPLY)
    Call<JsonRespBase<Void>> commitReply(@Body String reqData);

    @GET(NetUrl.GAME_GET_DETAIL)
    Call<JsonRespBase<GameDetail>> obtainGameDetail(@Query("app_id") int appId);

    @GET(NetUrl.POST_GET_DETAIL)
    Call<JsonRespBase<PostDetail>> obtainPostDetail(@Query("activity_id") int postId);

    @GET(NetUrl.COMMENT_GET_DETAIL)
    Call<JsonRespBase<PostCommentList>> obtainPostCommentList(@Query("activity_id") int postId,
                                                              @Query("page_id") int pageId,
                                                              @Query("page_size") int pageSize);

    @GET(NetUrl.COMMENT_GET_DETAIL)
    Call<JsonRespBase<CommentDetail>> obtainCommentDetail(@Query("activity_id") int postId,
                                                          @Query("comment_id") int commentId,
                                                          @Query("page_id") int pageId,
                                                          @Query("page_size") int pageSize);

    /**
     *
     * @param postId 文章ID
     * @param commentId 被点赞的评论ID
     * @param status 0 添加点赞 1 取消点赞
     * @return
     */
    @GET(NetUrl.COMMENT_SET_ADMIRE)
    Call<JsonRespBase<Void>> putAdmireState(@Query("activity_id") int postId,
                                            @Query("comment_id") int commentId,
                                            @Query("status") int status);

    /**
     * 投票接口
     * @param ids 多个id用逗号分割
     */
    @GET(NetUrl.POST_SET_VOTE)
    Call<JsonRespBase<PostVoteInfo>> putVote(@Query("vote_id") int voteId, @Query("item_ids") String ids);
}
