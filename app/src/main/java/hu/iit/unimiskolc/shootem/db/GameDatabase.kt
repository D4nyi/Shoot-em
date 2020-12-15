/**
 * Created by: Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import hu.iit.unimiskolc.shootem.BuildConfig
import hu.iit.unimiskolc.shootem.db.dao.PlayerRecordDao
import hu.iit.unimiskolc.shootem.db.model.PlayerRecord

/**
 * High level database access (DB context)
 */
@Database(
    entities = [
        PlayerRecord::class
    ],
    version = 1
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun getPlayerRecordDao(): PlayerRecordDao

    companion object {

        private lateinit var INSTANCE: RoomDatabase
        private const val DATABASE_NAME = BuildConfig.APPLICATION_ID + ".db"

        fun getInstance(context: Context): RoomDatabase {
            if (!this::INSTANCE.isInitialized) {
                INSTANCE = createInstance(context)
            }
            return INSTANCE
        }

        private fun createInstance(context: Context): GameDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                GameDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}