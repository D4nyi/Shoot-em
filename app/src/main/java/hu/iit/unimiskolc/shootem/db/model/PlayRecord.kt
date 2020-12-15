/**
 * Created by: Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a Player record in the DB
 */
@Entity(tableName = "players")
data class PlayerRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long?,
    val name: String,
    var maxPoint: Int
)
