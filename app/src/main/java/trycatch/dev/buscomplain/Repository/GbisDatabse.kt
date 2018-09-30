package trycatch.dev.buscomplain.Repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import trycatch.dev.buscomplain.DataModel.GbisDataModel

@Database(entities = [GbisDataModel::class], version = 1)
abstract class GbisDatabse : RoomDatabase() {
    abstract fun gbisDao(): GbisDao
}