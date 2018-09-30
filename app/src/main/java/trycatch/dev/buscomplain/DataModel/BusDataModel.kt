package trycatch.dev.buscomplain.DataModel

data class BusDataModel(
        var busRouteId: String,
        var busRouteNm: String,
        val sectOrd: String,
        val fullSectDist: String,
        val sectDist: String,
        val rtDist: String,
        val stopFlag: String,
        val sectionId: String,
        val dataTm: String,
        val gpsX: String,
        val gpsY: String,
        val vehId: String,
        val plainNo: String,
        var busType: String,
        val lastStTm: String,
        val nextStTm: String,
        val isrunyn: String,
        val trnstnid: String,
        val islastyn: String,
        val isFullFlag: String,
        val posX: String,
        val posY: String,
        val lastStnId: String,
        var corpNm: String
)