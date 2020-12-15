/**
 * Created by: Dávid Bozó
 * Modified by:  Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.engine

import android.opengl.GLES30

/**
 * Shader info
 */
class ShaderProgram(
    private var programId: Int,
    private var vertexShaderId: Int,
    private var fragmentShaderId: Int,
    private var uniforms: Map<String, Int>
) {

    fun getUniformLocation(uniform: String): Int {
        val location = uniforms[uniform]
        if (location != null) {
            return location
        } else {
            throw Exception("Uniform not found in map.")
        }
    }

    fun use() {
        GLES30.glUseProgram(programId)
    }
}