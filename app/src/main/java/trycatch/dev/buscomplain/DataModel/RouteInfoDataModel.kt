package trycatch.dev.buscomplain.DataModel

data class RouteInfoDataModel(
        var busRouteId: String,
        val busRouteNm: String,
        val length: String,
        var routeType: String,
        val stStationNm: String,
        val edStationNm: String,
        val term: String,
        val lastBusYn: String,
        val firstBusTm: String,
        val lastBusTm: String,
        val firstLowTm: String,
        val lastLowTm: String,
        val corpNm: String,
        var busRouteType: String,
        val stBegin: String,
        val stEnd: String,
        val nextBus: String,
        val firstBusTmLow: String,
        val lastBusTmLow: String
)