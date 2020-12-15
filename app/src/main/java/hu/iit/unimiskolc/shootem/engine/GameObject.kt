/**
 * Created by: Dávid Bozó
 * Modified by:  Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.engine

/**
 * Abstracting game object logic
 */
abstract class GameObject(
    internal var shaderProgram: ShaderProgram
) {
    var position = 0

    abstract fun render(projectionMatrix: FloatArray)
}