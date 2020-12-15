/**
 * Created by: Dávid Bozó
 * Modified by:  Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.view

import android.content.Context
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import hu.iit.unimiskolc.shootem.R
import hu.iit.unimiskolc.shootem.engine.ContextFactory
import hu.iit.unimiskolc.shootem.engine.GameRenderer

/**
 * The Android view which is the entry point of the game
 */
@RequiresApi(Build.VERSION_CODES.N)
class GameView : GLSurfaceView {
    private var renderer: GameRenderer

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    init {
        val factory = ContextFactory()
        setEGLContextFactory(factory)
        renderer = GameRenderer(context)
        setRenderer(renderer)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return renderer.onTouchEvent(event)
    }
}