/**
 * Created by: Dávid Bozó
 * Modified by:  Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.engine.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.opengl.GLES30
import android.opengl.GLUtils
import hu.iit.unimiskolc.shootem.engine.Texture

/**
 * Manging textures.
 * Caching textures.
 */
class TextureManager(private val context: Context) {
    private val pool: MutableMap<Int, Texture> = HashMap()

    fun loadTexture(textureResourceId: Int): Texture {
        val poolTexture = pool[textureResourceId]
        if (poolTexture != null)
            return poolTexture

        val texture = createTexture(textureResourceId)
        pool[textureResourceId] = texture
        return texture
    }

    private fun createTexture(textureResourceId: Int): Texture {
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        if (textures[0] == 0)
            throw Exception("Failed to generate texture.")

        val id = textures[0]

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id)
        //Have to set filters, there is no default
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_NEAREST
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_NEAREST
        )
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 1)
        val options: BitmapFactory.Options = BitmapFactory.Options().apply {
            this.inScaled = false
        }

        val textureData: Bitmap =
            BitmapFactory.decodeResource(context.resources, textureResourceId, options)
        val textureDataFlipped: Bitmap =
            Bitmap.createBitmap(
                textureData,
                0,
                0,
                textureData.width,
                textureData.height,
                Matrix().apply {
                    this.setScale(1f, -1f)
                },
                true
            )

        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, textureDataFlipped, 0)

        return Texture(id, textureResourceId, textureDataFlipped.width, textureDataFlipped.height)
    }
}