package trycatch.dev.buscomplain.ViewModel

import android.content.Context
import android.net.RouteInfo
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.toObservable
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.error
import org.json.JSONArray
import org.json.XML
import trycatch.dev.buscomplain.BusApi
import trycatch.dev.buscomplain.DataModel.GbisDataModel
import trycatch.dev.buscomplain.DataModel.RouteInfoDataModel
import trycatch.dev.buscomplain.Repository.GbisDao

class SearchViewModel(val context: Context) : ViewModel() {
    private val busApi by lazy {
        BusApi.create()
    }

    private val gbisDao by lazy {
        GbisDao.create(context)
    }

    fun getBusRouteList(strSrch: String) =
            busApi.getBusRouteList(strSrch)
                    .filter{
                        it.isSuccessful
                    }
                    .map {
                        XML.toJSONObject(it.body()?.string())
                    }
                    .filter{
                        it.getJSONObject("ServiceResult").getJSONObject("msgHeader").getString("headerCd") == "0"
                    }
                    .map {
                        it.getJSONObject("ServiceResult")
                                ?.getJSONObject("msgBody")
                                ?.get("itemList")
                    }
                    .map {
                        when(it){
                            is JSONArray ->
                                it.toString()
                            else ->
                                "[$it]"
                        }
                    }
                    .map {
                        Gson().fromJson(
                                it,
                                Array<RouteInfoDataModel>::class.java
                        )
                    }
                    .flatMap {
                        it.toObservable()
                    }
                    .map {
                        if(it.routeType == "8") {
                            if (gbisDao.getGbisDataByRouteId(it.busRouteId).isNotEmpty()) {
                                val gbisData = gbisDao.getGbisDataByRouteId(it.busRouteId)
                                it.routeType = gbisData[0].routeTp
                            }
                            else{
                                it.routeType = "13"
                            }
                        }
                        it
                    }
}