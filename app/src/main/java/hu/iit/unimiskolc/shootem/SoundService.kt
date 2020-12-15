/**
 * Created by: Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

/**
 * Plays music in the back ground
 */
class SoundService : Service() {
    private lateinit var player: MediaPlayer

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer.create(applicationContext, R.raw.music)

        player.isLooping = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            player.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
    }
}