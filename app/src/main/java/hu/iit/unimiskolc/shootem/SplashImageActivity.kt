/**
 * Created by: Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashImageActivity : AppCompatActivity() {
    private val SPLASH_WAIT_TIME: Long = 1500
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_image)

        val splashImageThread: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(SPLASH_WAIT_TIME)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }
        splashImageThread.start()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }
}