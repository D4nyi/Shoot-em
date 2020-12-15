/**
 * Created by: Dávid Bozó
 * Modified by:  Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.engine

import android.opengl.GLES30

/**
 * Texture data
 */
data class Texture(
    private val id: Int,
    val textureResourceId: Int,
    val width: Int,
    val height: Int,
) {

    fun bind() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id)
    }

    fun unbind() {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }
}