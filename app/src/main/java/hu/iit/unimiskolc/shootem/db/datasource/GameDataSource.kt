/**
 * Created by: Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.db.datasource

import hu.iit.unimiskolc.shootem.db.dao.PlayerRecordDao
import hu.iit.unimiskolc.shootem.db.model.PlayerRecord

/**
 * Repository for abstracting db operations
 */
class GameDataSource(
    private val playerDao: PlayerRecordDao
) {
    suspend fun fetch(query: String): List<PlayerRecord> {
        return playerDao.fetchByScore()
    }

    suspend fun updateOrInsertRecord(player: PlayerRecord) {
        val exists = playerDao.exists(player.name)
        if (exists != null) {
            playerDao.updatePlayer(player.copy(
                id = exists,
                name = player.name,
                maxPoint = player.maxPoint
            ))
        } else {
            playerDao.insertPlayer(player)
        }
    }
}
