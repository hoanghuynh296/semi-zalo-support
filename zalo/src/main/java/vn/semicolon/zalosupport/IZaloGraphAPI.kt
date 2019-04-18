package vn.semicolon.zalosupport

import android.app.Activity
import android.app.Dialog
import android.util.Log
import com.zing.zalo.zalosdk.oauth.LoginVia
import com.zing.zalo.zalosdk.oauth.OAuthCompleteListener
import com.zing.zalo.zalosdk.oauth.OauthResponse
import com.zing.zalo.zalosdk.oauth.ZaloSDK
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import retrofit2.http.GET
import retrofit2.http.Query


interface IZaloGraphAPI {
    @GET("me")
    fun getProfile(
        @Query("access_token") accessToken: String,
        @Query("fields") fields: String = "id,birthday,gender,picture,name"
    ): Flowable<ZaloUserModel>

    @GET("me/invitable_friends")
    fun getInvitableFriends(
        @Query("access_token") accessToken: String,
        @Query("fields") fields: String = "id,birthday,gender,picture,name",
        @Query("limit") limit: Int = 200,
        @Query("offset") offset: Int = 0
    ): Flowable<ZaloResponse<List<ZaloUserModel>>>


}

interface IZaloOauthAPI {
    @GET("access_token")
    fun getAccessToken(
        @Query("code") oauthCode: String,
        @Query("app_id") appId: String,
        @Query("app_secret") appSecret: String
    ): Flowable<ZaloUserModel.Token>
}

object ZaloAPI {
    fun getProfile(accessToken: String): Flowable<ZaloUserModel> {
        return ZaloAPIService.graph(IZaloGraphAPI::class.java).getProfile(accessToken)
    }

    fun getAccessToken(oauthCode: String, appId: String, appSecret: String): Flowable<ZaloUserModel.Token> {
        return ZaloAPIService.oauth(IZaloOauthAPI::class.java).getAccessToken(oauthCode, appId, appSecret)
    }

    fun getInvitableFriends(accessToken: String): Flowable<ZaloResponse<List<ZaloUserModel>>> {
        return ZaloAPIService.graph(IZaloGraphAPI::class.java).getInvitableFriends(accessToken)
    }

    fun getInvitableFriendsByOauthCode(
        oauthCode: String,
        appId: String,
        appSecret: String
    ): Flowable<ZaloResponse<List<ZaloUserModel>>> {
        val ob0 = getAccessToken(oauthCode, appId, appSecret)
        return ob0.subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .map {
                it.access_token
            }.flatMap {
                ZaloAPI.getInvitableFriends(it)
            }
    }

    fun auth(activity: Activity, callback: OAuthCompleteListener) {
        ZaloSDK.Instance.authenticate(activity, LoginVia.APP, callback)
    }
}