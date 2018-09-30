package trycatch.dev.buscomplain

import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.*

interface SendApi {
    @FormUrlEncoded
    @POST("civil_01-process.htm")
    fun sendSeoulComplain(
            @Field("title", encoded = true) title: String,
            @Field("gubun", encoded = true) division: String,
            @Field("name", encoded = true) name: String,
            @Field("mobile1") mobile1: String,
            @Field("mobile2") mobile2: String,
            @Field("mobile3") mobile3: String,
            @Field("company", encoded = true) company: String,
            @Field("line_no", encoded = true) line_no: String,
            @Field("driver", encoded = true) driver: String,
            @Field("password", encoded = true) password: String,
            @Field("remarks", encoded = true) remarks: String,
            @Field("wseq") wseq: String,
            @Field("sortseq") sortseq: String,
            @Field("Page") page: String,
            @Field("searchkey") searchkey: String,
            @Field("search") search: String,
            @Field("job") job: String
    ): Observable<Response<ResponseBody>>

    @FormUrlEncoded
    @POST("civil_01-process.htm")
    fun sendSeoulCompliment(
            @Field("title", encoded = true) title: String,
            @Field("name", encoded = true) name: String,
            @Field("company", encoded = true) company: String,
            @Field("line_no", encoded = true) line_no: String,
            @Field("driver", encoded = true) driver: String,
            @Field("password", encoded = true) password: String,
            @Field("remarks", encoded = true) remarks: String,
            @Field("wseq") wseq: String,
            @Field("sortseq") sortseq: String,
            @Field("Page") page: String,
            @Field("searchkey") searchkey: String,
            @Field("search") search: String,
            @Field("job") job: String
    ): Observable<Response<ResponseBody>>

    companion object {
        fun create(): SendApi = Retrofit.Builder().apply {
            baseUrl("http://www.sbus.or.kr/2018/civil/")
            client(
                    OkHttpClient.Builder()
                            .addInterceptor(HttpLoggingInterceptor().apply {
                                level = HttpLoggingInterceptor.Level.BODY
                            })
                            .build()
            )
            addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        }.build().create(SendApi::class.java)
    }
}