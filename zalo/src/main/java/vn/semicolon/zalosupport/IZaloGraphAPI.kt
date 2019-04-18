package vn.semicolon.zalosupport

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.util.Log
import com.zing.zalo.zalosdk.oauth.*
import com.zing.zalo.zalosdk.oauth.OAuthCompleteListener
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

object SZaloSDK {
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
                SZaloSDK.getInvitableFriends(it)
            }
    }

    fun auth(activity: Activity, callback: vn.semicolon.zalosupport.OAuthCompleteListener) {
        ZaloSDK.Instance.authenticate(activity, LoginVia.APP, object : OAuthCompleteListener() {
            override fun onGetOAuthComplete(oauthResponse: com.zing.zalo.zalosdk.oauth.OauthResponse?) {
                super.onGetOAuthComplete(oauthResponse)
                callback.onGetOAuthComplete(
                    OauthResponse(
                        oauthResponse?.errorCode ?: 0,
                        oauthResponse?.errorMessage,
                        oauthResponse?.getuId() ?: 0,
                        oauthResponse?.oauthCode,
                        oauthResponse?.channel,
                        oauthResponse?.facebookAccessToken,
                        oauthResponse?.facebookExpireTime ?: 0,
                        oauthResponse?.socialId,
                        oauthResponse?.isRegister ?: false
                    )
                )
            }

            override fun onAuthenError(i: Int, str: String?) {
                super.onAuthenError(i, str)
                Log.d("Semi-Zalo", str)
                callback.onAuthenError(i, str)
            }

            override fun onGetPermissionData(i: Int) {
                super.onGetPermissionData(i)
                callback.onGetPermissionData(i)
            }

            override fun onRequestAccountProtect(i: Int, str: String?) {
                super.onRequestAccountProtect(i, str)
                callback.onRequestAccountProtect(i, str)
            }

            override fun onProtectAccComplete(i: Int, str: String?, dialog: Dialog?) {
                super.onProtectAccComplete(i, str, dialog)
                callback.onProtectAccComplete(i, str, dialog)
            }
        })
    }

    fun wrap(application: Application) {
        ZaloSDKApplication.wrap(application)
    }
}

interface OAuthCompleteListener {
    fun onGetOAuthComplete(oauthResponse: OauthResponse?)
    fun onAuthenError(i: Int, str: String?)
    fun onGetPermissionData(i: Int)
    fun onRequestAccountProtect(i: Int, str: String?)
    fun onProtectAccComplete(i: Int, str: String?, dialog: Dialog?)
}

data class OauthResponse(
    var errorCode: Int = 0,
    var errorMessage: String? = null,
    var uid: Long = 0,
    var oauthCode: String? = null,
    var channel: LoginChannel? = null,
    var facebookAccessToken: String? = null,
    var facebookExpireTime: Long = 0,
    var socialId: String? = null,
    var isRegister: Boolean = false
)