package vn.semicolon.zalosupport

import okhttp3.Interceptor


/**
 * Created by HuynhMH
 */
object SemiAPI {
    fun <S> createService(serviceClass: Class<S>, host: String): S {
        return SemiHttpFactory.create(
            serviceClass,
            host,
            interceptors = listOf(getHeaderInterceptor())
        )
    }

    fun getHeaderInterceptor(): Interceptor {
        return Interceptor {
            val request = it.request()
            val builder = request.newBuilder()
            return@Interceptor it.proceed(builder.build())
        }
    }
}
