/**
 * Created by: Dávid Bozó
 * Modified by:  Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.engine

import android.opengl.GLES30
import android.opengl.Matrix

/**
 * Contains data about the game object and logic about rendering
 */
open class GameObject2D(
    shaderProgram: ShaderProgram,
    val mesh: Mesh,
    val texture: Texture,
    val width: Int,
    val height: Int,
) : GameObject(shaderProgram) {

    var x: Int = 0
    var y: Int = 0
    var health = 2
    private val pos = FloatArray(4 * 4)

    /**
     * render logic
     */
    override fun render(projectionMatrix: FloatArray) {
        shaderProgram.use()

        val textureSamplerLocation = shaderProgram.getUniformLocation("texture_sampler")
        GLES30.glUniform1i(textureSamplerLocation, 0)
        val projectionLocation = shaderProgram.getUniformLocation("projectionMatrix")
        GLES30.glUniformMatrix4fv(projectionLocation, 1, false, projectionMatrix, 0)
        val positionLocation = shaderProgram.getUniformLocation("objectPos")
        Matrix.setIdentityM(pos, 0)
        Matrix.translateM(pos, 0, x.toFloat(), y.toFloat(), 0f)
        GLES30.glUniformMatrix4fv(positionLocation, 1, false, pos, 0)

        texture.bind()

        GLES30.glBindVertexArray(mesh.vaoId)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glEnableVertexAttribArray(1)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)

        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glBindVertexArray(0)

        texture.unbind()
    }

    /**
     * creating copies of the game object, (for enemies)
     */
    fun copy(): GameObject2D {
        return GameObject2D(shaderProgram, mesh, texture, width, height)
    }

    /**
     * creating copies of the projectiles
     */
    fun copy(newX: Int): GameObject2D {
        return GameObject2D(shaderProgram, mesh, texture, width, height).apply {
            x = newX
            health = 1
        }
    }
}