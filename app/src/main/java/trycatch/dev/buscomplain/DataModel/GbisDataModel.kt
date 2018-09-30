package trycatch.dev.buscomplain.DataModel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "gbis_repositories")
data class GbisDataModel(
    @PrimaryKey
    @SerializedName("route_id")
    @ColumnInfo(name = "route_id")
    val routeId: String,

    @SerializedName("route_nm")
    @ColumnInfo(name = "route_nm")
    val routeNm: String,

    @SerializedName("route_tp")
    @ColumnInfo(name = "route_tp")
    val routeTp: String,

    @SerializedName("company_id")
    @ColumnInfo(name = "company_id")
    val companyId: String,

    @SerializedName("company_nm")
    @ColumnInfo(name = "company_nm")
    val companyNm: String,

    @SerializedName("tel_no")
    @ColumnInfo(name = "tel_no")
    val telNo: String
)