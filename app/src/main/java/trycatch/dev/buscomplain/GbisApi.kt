package trycatch.dev.buscomplain

import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.*

interface GbisApi {
    @GET("/ws/rest/baseinfoservice")
    fun getBaseInfoService(
            @Query("serviceKey", encoded = true) serviceKey: String = "1234567890"
    ): Observable<Response<ResponseBody>>

    @Streaming
    @GET
    fun getGbis(
            @Url fileUrl: String
    ): Observable<ResponseBody>

    companion object {
        fun create(): GbisApi = Retrofit.Builder().apply {
            baseUrl("http://openapi.gbis.go.kr/")
            client(OkHttpClient.Builder().build())
            addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        }.build().create(GbisApi::class.java)

        val key = "kUU0CHEP1CFp6KeqK8xKmsNDcVCr7eou9MQCYdSoWC%2Bh8Ys8kI4vUgR1UM6RxmdWpb6qw7JOPWRJVCBIf1Uq2A%3D%3D"
        val key2 = "IL6p9epV1V6%2FltcBm3WOJ%2F3TUYbZn8uKhgGhdpZ%2B%2FA7P3Mu87Pg1uhDhg0jIOQA%2F%2FbTvtMz6YaG4IBs0A7u3FA%3D%3D"
        val key3 = "ILaw5NQBylyHWwTHEziu6j6igRxXWVkVJynZ%2Big3%2Frp9D7ivOUqkw27fybFnn6mm3rYYuGK08yKz6VfDx9fsWw%3D%3D"
    }
}