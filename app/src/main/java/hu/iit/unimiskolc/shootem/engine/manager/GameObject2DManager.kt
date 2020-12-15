/**
 * Created by: Dávid Bozó
 * Modified by:  Attila Szilvási, Dániel Szöllősi
 */


package hu.iit.unimiskolc.shootem.engine.manager

import androidx.annotation.RawRes
import hu.iit.unimiskolc.shootem.R
import hu.iit.unimiskolc.shootem.engine.GameObject2D

/**
 * Loads game objects to the 2D space
 * Game object contains all the assets to render (texture, mesh, coordinates)
 */
class GameObject2DManager(
    private val textureManager: TextureManager,
    private val shaderManager: ShaderManager,
    private val meshManager: MeshManager
) {
    /**
     * Load to a fix width, calcs the corresponding height with the given ratio
     */
    fun loadByWidth(
        @RawRes vertexShaderId: Int, @RawRes fragmentShaderId: Int,
        left: Int, top: Int, right: Int, bottom: Int, requiredWidth: Float
    ): GameObject2D {

        val usedTextureHeight: Float = bottom.toFloat() - top
        val usedTextureWidth: Float = right.toFloat() - left
        val ratio = usedTextureHeight / usedTextureWidth

        val calculatedHeight: Float = requiredWidth * ratio

        return load(
            vertexShaderId,
            fragmentShaderId,
            left,
            top,
            right,
            bottom,
            calculatedHeight,
            requiredWidth
        )
    }

    /**
     * Load to a fix height, calcs the corresponding width with the given ratio
     */
    fun loadByHeight(
        @RawRes vertexShaderId: Int, @RawRes fragmentShaderId: Int,
        left: Int, top: Int, right: Int, bottom: Int, requiredHeight: Float
    ): GameObject2D {

        val usedTextureHeight: Float = bottom.toFloat() - top
        val usedTextureWidth: Float = right.toFloat() - left
        val ratio = usedTextureWidth / usedTextureHeight
        val calculatedWidth: Float = requiredHeight * ratio

        return load(
            vertexShaderId,
            fragmentShaderId,
            left,
            top,
            right,
            bottom,
            requiredHeight,
            calculatedWidth
        )
    }

    /**
     * Contains the load logic
     * @param height height of the object in pixels
     * @param width width of the object in pixels
     * @param left coords in pixels on the texture
     */
    private fun load(
        @RawRes vertexShaderId: Int, @RawRes fragmentShaderId: Int,
        left: Int, top: Int, right: Int, bottom: Int, height: Float, width: Float
    ): GameObject2D {
        val texture = textureManager.loadTexture(R.drawable.atlas)

        val vertices = floatArrayOf(
            0.0f, 0.0f, 0.0f,
            0.0f, height, 0.0f,
            width, height, 0.0f,

            0.0f, 0.0f, 0.0f,
            width, height, 0.0f,
            width, 0.0f, 0.0f
        )

        val leftF = left.toFloat()
        val topF = texture.height - top.toFloat()
        val bottomF = texture.height - bottom.toFloat()
        val rightF = right.toFloat()
        val textCoords =
            floatArrayOf(
                leftF / texture.width, bottomF / texture.height,
                leftF / texture.width, topF / texture.height,
                rightF / texture.width, topF / texture.height,
                leftF / texture.width, bottomF / texture.height,
                rightF / texture.width, topF / texture.height,
                rightF / texture.width, bottomF / texture.height
            )

        val mesh = meshManager.load(vertices, textCoords)

        val shaderProgram = shaderManager.load(
            vertexShaderId,
            fragmentShaderId,
            arrayOf("texture_sampler", "projectionMatrix")
        )

        return GameObject2D(shaderProgram, mesh, texture, width.toInt(), height.toInt())
    }
}