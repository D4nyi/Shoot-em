/**
 * Created by: Dávid Bozó
 * Modified by:  Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.engine

import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

class ContextFactory : GLSurfaceView.EGLContextFactory {

    override fun createContext(egl: EGL10, display: EGLDisplay, eglConfig: EGLConfig): EGLContext {
        Log.w("Game", "creating OpenGL ES $glVersion context")
        return egl.eglCreateContext(
            display,
            eglConfig,
            EGL10.EGL_NO_CONTEXT,
            intArrayOf(EGL_CONTEXT_CLIENT_VERSION, glVersion.toInt(), EGL10.EGL_NONE)
        ) // returns null if 3.0 is not supported
    }

    override fun destroyContext(egl: EGL10?, display: EGLDisplay?, context: EGLContext?) {
        egl?.eglDestroyContext(display, context)
    }
}