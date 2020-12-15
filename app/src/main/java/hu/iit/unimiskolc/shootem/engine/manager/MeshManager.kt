/**
 * Created by: Dávid Bozó
 * Modified by:  Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.engine.manager

import android.opengl.GLES30
import hu.iit.unimiskolc.shootem.engine.Mesh
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * Manager class to handle mesh data
 */
class MeshManager {

    /**
     * From raw data
     */
    fun load(verticesData: FloatArray, textureData: FloatArray): Mesh {
        // VAO
        val vaoIdBuffer: IntBuffer = IntBuffer.allocate(1)
        GLES30.glGenVertexArrays(1, vaoIdBuffer)
        GLES30.glBindVertexArray(vaoIdBuffer[0])
        val vaoId = vaoIdBuffer[0]

        // Position VBO
        val vertexBufferId = IntArray(1)
        GLES30.glGenBuffers(1, vertexBufferId, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBufferId[0])
        val verticeDataBuffer = createBufferFromArray(verticesData)
        //size have to be in bytes
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            verticesData.size * 4,
            verticeDataBuffer,
            GLES30.GL_STATIC_DRAW
        )
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0)

        // Texture VBO
        val textureBufferId = IntArray(1)
        GLES30.glGenBuffers(1, textureBufferId, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, textureBufferId[0])
        val textureDataBuffer = createBufferFromArray(textureData)
        //size have to be in bytes
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            textureData.size * 4,
            textureDataBuffer,
            GLES30.GL_STATIC_DRAW
        )
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glBindVertexArray(0)

        return Mesh(vaoId)
    }

    private fun createBufferFromArray(floatArray: FloatArray): FloatBuffer {
        val floatBuffer: FloatBuffer = FloatBuffer.allocate(floatArray.size)
        floatBuffer.put(floatArray, 0, floatArray.size)
        floatBuffer.rewind()

        return floatBuffer
    }
}