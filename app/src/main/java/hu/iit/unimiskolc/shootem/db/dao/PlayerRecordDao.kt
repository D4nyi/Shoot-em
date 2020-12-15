/**
 * Created by: Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import hu.iit.unimiskolc.shootem.db.model.PlayerRecord

@Dao
/**
 * Interface to the database operations
 * Implemented by the Room ORM package at compile time
 */
interface PlayerRecordDao {

    @Insert
    suspend fun insertPlayer(record: PlayerRecord): Long

    @Update
    suspend fun updatePlayer(record: PlayerRecord)

    @Query("SELECT id FROM players WHERE name = :query")
    suspend fun exists(query: String): Long?

    @Query("Select * from players order by name asc")
    suspend fun fetchPlayers(): List<PlayerRecord>

    @Query("Select * from players order by maxPoint desc")
    suspend fun fetchByScore(): List<PlayerRecord>
}