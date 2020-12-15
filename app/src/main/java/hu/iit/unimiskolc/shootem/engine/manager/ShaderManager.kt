/**
 * Created by: Dávid Bozó
 * Modified by:  Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.engine.manager

import android.content.Context
import android.opengl.GLES30
import androidx.annotation.RawRes
import hu.iit.unimiskolc.shootem.engine.ShaderProgram
import java.io.InputStreamReader

/**
 * Manging shaders.
 * Caching shaders.
 */
class ShaderManager(private val context: Context) {
    private val pool: MutableMap<Pair<Int, Int>, ShaderProgram> = HashMap()

    /**
     * WARNING: Do not cache shader. Always creates new.
     */
    private fun load(
        vertexShaderCode: String,
        fragmentShaderCode: String,
        uniforms: Array<String>
    ): ShaderProgram {
        val programId = GLES30.glCreateProgram()
        if (programId == 0)
            throw Exception("Could not create Shader program")

        val vertexShaderId =
            createShaderThenAttach(programId, vertexShaderCode, GLES30.GL_VERTEX_SHADER)
        val fragmentShaderId =
            createShaderThenAttach(programId, fragmentShaderCode, GLES30.GL_FRAGMENT_SHADER)

        link(programId)

        val uniformsMap = locateUniforms(programId, uniforms)

        return ShaderProgram(
            programId, vertexShaderId, fragmentShaderId, uniformsMap
        )
    }


    fun load(
        @RawRes vertexShaderResource: Int,
        @RawRes fragmentShaderResource: Int,
        uniforms: Array<String>
    ): ShaderProgram {
        val poolKey = Pair(vertexShaderResource, fragmentShaderResource)
        val poolShader = pool[poolKey]
        if (poolShader != null)
            return poolShader

        val vertexShaderCode = readShaderCodeFromResource(vertexShaderResource)
        val fragmentShaderCode = readShaderCodeFromResource(fragmentShaderResource)

        val shaderProgram = load(vertexShaderCode, fragmentShaderCode, uniforms)
        pool[poolKey] = shaderProgram
        return shaderProgram
    }

    private fun locateUniforms(programId: Int, uniforms: Array<String>): Map<String, Int> {
        val uniformsMap: MutableMap<String, Int> = HashMap(uniforms.size)

        uniforms.forEach {
            val location = GLES30.glGetUniformLocation(programId, it)
            if (location < 0) {
                throw Exception("Could not find uniform: $it")
            }
            uniformsMap[it] = location
        }

        return uniformsMap
    }

    private fun link(programId: Int) {
        GLES30.glLinkProgram(programId)
        val linStatusCode = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linStatusCode, 0)
        if (linStatusCode[0] == GLES30.GL_FALSE) {
            val programInfo = GLES30.glGetProgramInfoLog(programId)
            throw Exception("Error linking shader program: $programInfo")
        }

        GLES30.glValidateProgram(programId)
        val validateionStatus = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_VALIDATE_STATUS, validateionStatus, 0)
        if (validateionStatus[0] == 0) {
            throw Exception(
                "Error failed to validate shader program: " + GLES30.glGetProgramInfoLog(
                    programId
                )
            )
        }
    }

    private fun createShaderThenAttach(programId: Int, shaderCode: String, shaderType: Int): Int {
        val shaderId = GLES30.glCreateShader(shaderType)
        if (shaderId == 0)
            throw Exception("Could not create shader. Shader code: $shaderCode")

        GLES30.glShaderSource(shaderId, shaderCode)
        GLES30.glCompileShader(shaderId)

        val shaderCompileStatus = IntArray(1)
        GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, shaderCompileStatus, 0)
        if (shaderCompileStatus[0] == GLES30.GL_FALSE) {
            throw Exception("Shader compile failed: " + GLES30.glGetShaderInfoLog(shaderId))
        }
        GLES30.glAttachShader(programId, shaderId)

        return shaderId
    }

    private fun readShaderCodeFromResource(@RawRes rawId: Int): String {
        val codeInputStream = context.resources.openRawResource(rawId)
        val inputStreamReder = InputStreamReader(codeInputStream)
        val lines = inputStreamReder.readLines()
        val codeStringBuilder = StringBuilder()
        lines.forEach {
            codeStringBuilder.appendLine(it)
        }
        return codeStringBuilder.toString()
    }
}