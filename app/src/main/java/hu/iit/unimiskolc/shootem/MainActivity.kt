/**
 * Created by: Dávid Bozó
 * Modified by:  Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import hu.iit.unimiskolc.shootem.db.GameDatabase
import hu.iit.unimiskolc.shootem.db.datasource.GameDataSource
import hu.iit.unimiskolc.shootem.db.model.PlayerRecord
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var service: Intent
    private var playMusic: Boolean = false
    private lateinit var dataSource: GameDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = GameDatabase.getInstance(this.applicationContext) as GameDatabase
        dataSource = GameDataSource(db.getPlayerRecordDao())

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_main)

        service = Intent(this, SoundService::class.java)

        findViewById<ImageView>(R.id.music).setOnClickListener {
            val imageView = it as ImageView
            playMusic = !playMusic
            if (playMusic) {
                imageView.setImageResource(R.drawable.ic_baseline_volume_up_24)
                startService(service)
            } else {
                imageView.setImageResource(R.drawable.ic_baseline_volume_off_24)
                stopService(service)
            }
        }

        findViewById<TextView>(R.id.play).setOnClickListener {
            startActivityForResult(Intent(this, GameActivity::class.java), 10)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val gameState = data?.getIntExtra("GameState", -1)
        if (gameState != null && gameState != -1) {
            findViewById<TextView>(R.id.play).text = getString(R.string.game_over, gameState)
            runBlocking {
                dataSource.updateOrInsertRecord(PlayerRecord(null, "Test", gameState))
            }
        }
    }
}