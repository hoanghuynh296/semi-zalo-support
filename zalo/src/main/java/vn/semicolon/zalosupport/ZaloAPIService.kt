package vn.semicolon.zalosupport

object ZaloAPIService {
    const val ZALO_OAUTH_HOST = "https://oauth.zaloapp.com/v3/"
    const val ZALO_GRAPH_HOST = "https://graph.zalo.me/v2.0/"
    fun <S> oauth(serviceClass: Class<S>): S {
        return SemiAPI.createService(serviceClass, ZALO_OAUTH_HOST)
    }
    
    fun <S> graph(serviceClass: Class<S>): S {
        return SemiAPI.createService(serviceClass, ZALO_GRAPH_HOST)
    }
}

data class ZaloResponse<T>(
    var summary: Summary,
    var data: T,
    var paging: Paging
) {
    data class Paging(
        var next: String? = null,
        var previous: String? = null
    )

    data class Summary(
        var total_count: Int = 0
    )
}