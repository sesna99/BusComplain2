package trycatch.dev.buscomplain.ViewModel

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import io.reactivex.rxkotlin.toObservable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.json.JSONArray
import org.json.XML
import trycatch.dev.buscomplain.BusApi
import trycatch.dev.buscomplain.DataModel.*
import trycatch.dev.buscomplain.GbisApi
import trycatch.dev.buscomplain.Repository.GbisDao


class MapViewModel(private val context: Context) : ViewModel(), AnkoLogger {
    private val busApi by lazy {
        BusApi.create()
    }

    private val gbisApi by lazy {
        GbisApi.create()
    }

    private val gbisDao by lazy {
        GbisDao.create(context)
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences("pref", MODE_PRIVATE)
    }

    fun getSurroundBus(x: Double, y: Double) =
            getSurroundStation(x, y)
                    .flatMap {
                        getRouteByStationId(
                                if(it.arsId.length < 5)
                                    "0${it.arsId}"
                                else
                                    it.arsId
                        )
                    }
                    .distinct()
                    .map {
                        it.busRouteId
                    }
                    .flatMap {
                        getBusById(it)
                    }!!

    fun getBusById(busRouteId: String) =
            getRouteInfo(busRouteId).flatMap { info ->
                getBusPosByRtId(info.busRouteId).map{ bus ->
                    bus.busRouteId = busRouteId
                    bus.busRouteNm = info.busRouteNm
                    if(info.routeType == "8"){
                        if(gbisDao.getGbisDataByRouteId(busRouteId).isNotEmpty()) {
                            val gbisData = gbisDao.getGbisDataByRouteId(busRouteId)[0]
                            bus.corpNm = gbisData.companyNm
                            bus.busType = gbisData.routeTp
                        }
                        else{
                            bus.busType = "13"
                        }
                    }
                    else{
                        bus.corpNm = info.corpNm
                        bus.busType = info.routeType
                    }

                    bus
                }
            }!!

    //근처 정류소 정보 가져오기
    private fun getSurroundStation(tmX: Double, tmY: Double) =
            busApi.getStaionsByPosList(tmX, tmY)
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
                                Array<StationDataModel>::class.java
                        )
                    }
                    .flatMap {
                        it.toObservable()
                    }

    //정류소 id로 노선 정보 받아오기
    private fun getRouteByStationId(arsId: String) =
            busApi.getRouteByStationList(arsId)
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

    private fun getBusPosByRtId(busRouteId: String) =
            busApi.getBusPosByRtidList(busRouteId)
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
                                Array<BusDataModel>::class.java
                        )
                    }
                    .flatMap {
                        it.toObservable()
                    }

    private fun getRouteInfo(busRouteId: String) =
            busApi.getRouteInfo(busRouteId)
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

    fun update(){
        val version: String? = sharedPreferences.getString("version", "0")
        gbisApi.getBaseInfoService()
                .filter{
                    it.isSuccessful
                }
                .map {
                    XML.toJSONObject(it.body()?.string())
                }
                .filter{
                    error { it }

                    it.getJSONObject("response")?.getJSONObject("msgHeader")?.getString("resultCode") == "0"
                }
                .filter {
                    it.getJSONObject("response")
                            ?.getJSONObject("msgBody")
                            ?.getJSONObject("baseInfoItem")
                            ?.getString("routeVersion") != version
                }
                .map {
                    it.getJSONObject("response")
                            ?.getJSONObject("msgBody")
                            ?.getString("baseInfoItem")
                }
                .map {
                    Gson().fromJson(
                            it,
                            BaseInfoServiceDataModel::class.java
                    )
                }
                .subscribe({
                    getGbis(it.routeVersion)
                    sharedPreferences.edit {
                        putString("version", it.routeVersion)
                    }
                }){
                    error { it }
                }
    }

    fun getGbis(version: String){
        gbisDao.clearAll()
        gbisApi.getGbis("/ws/download?route$version.txt")
                .subscribe({
                    val ins = it?.byteStream()
                    val data = ins?.bufferedReader()?.use {
                        it.readText()
                    }
                    data?.split("^").run {
                        this?.toObservable()
                                ?.skip(1)
                                ?.subscribe({
                                    it.split("|").run {
                                        gbisDao.add(
                                                GbisDataModel(
                                                        this[0],
                                                        this[1],
                                                        this[2],
                                                        this[15],
                                                        this[16],
                                                        this[17]
                                                )
                                        )
                                    }
                                }){
                                    error { it }
                                }
                    }
                }){
                    error { it }
                }
    }
}