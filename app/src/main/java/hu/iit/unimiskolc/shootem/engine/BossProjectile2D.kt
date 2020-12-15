/**
 * Created by: Dávid Bozó
 * Modified by:  Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.engine

import kotlin.random.Random

class BossProjectile2D (
    shaderProgram: ShaderProgram,
    mesh: Mesh,
    texture: Texture,
    width: Int,
    height: Int,
    private val canvasWidth: Int
) : GameObject2D(shaderProgram, mesh, texture, width, height) {

    private val fallSpeed:Int = 8
    private var xSpeed: Int = Random.nextInt(-6, 6)

    constructor(gameObject: GameObject2D, canvasWidth: Int): this(gameObject.shaderProgram, gameObject.mesh, gameObject.texture, gameObject.width, gameObject.height, canvasWidth)

    /**
     * render logic for boss (moving sid-to-side and falling)
     */
    override fun render(projectionMatrix: FloatArray) {
        y -= fallSpeed
        x += xSpeed
        if ((x > canvasWidth - 150) || x < 0){
            xSpeed *= -1
        }
        super.render(projectionMatrix)
    }

    fun copy(newX: Int, newY: Int): GameObject2D {
        return BossProjectile2D(shaderProgram, mesh, texture, width, height, canvasWidth).apply {
            x = newX
            y = newY
            health = 1
        }
    }
}