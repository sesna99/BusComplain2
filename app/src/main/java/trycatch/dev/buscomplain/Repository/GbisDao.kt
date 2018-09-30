package trycatch.dev.buscomplain.Repository

import android.content.Context
import androidx.room.*
import io.reactivex.Flowable
import trycatch.dev.buscomplain.DataModel.GbisDataModel

@Dao
interface GbisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(repo: GbisDataModel)

    @Query("SELECT * FROM gbis_repositories WHERE route_id = :routeId")
    fun getGbisDataByRouteId(routeId: String): Array<GbisDataModel>

    @Query("SELECT * FROM gbis_repositories")
    fun getAll(): Array<GbisDataModel>

    @Query("DELETE FROM gbis_repositories")
    fun clearAll()

    companion object {
        fun create(context: Context) = Room.databaseBuilder(context.applicationContext,
                GbisDatabse::class.java, "bus_complain.db")
                .build()
                .gbisDao()
    }
}