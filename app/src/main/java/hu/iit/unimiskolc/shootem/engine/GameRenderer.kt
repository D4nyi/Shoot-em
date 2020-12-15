/**
 * Created by: Dávid Bozó
 * Modified by:  Attila Szilvási, Dániel Szöllősi
 */

package hu.iit.unimiskolc.shootem.engine

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent
import hu.iit.unimiskolc.shootem.GameActivity
import hu.iit.unimiskolc.shootem.R
import hu.iit.unimiskolc.shootem.engine.manager.GameObject2DManager
import hu.iit.unimiskolc.shootem.engine.manager.MeshManager
import hu.iit.unimiskolc.shootem.engine.manager.ShaderManager
import hu.iit.unimiskolc.shootem.engine.manager.TextureManager
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.random.Random

/**
 * The game it self
 * Contains the game logic and rendering logic
 */
class GameRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val shaderManager: ShaderManager = ShaderManager(context)
    private val textureManager: TextureManager = TextureManager(context)
    private val meshManager: MeshManager = MeshManager()
    private val gameObject2DManager: GameObject2DManager =
        GameObject2DManager(textureManager, shaderManager, meshManager)

    private lateinit var shaderProgram: ShaderProgram
    private lateinit var texture: Texture

    private lateinit var gameObject2DBackground: GameObject2D
    private lateinit var playerObject: Array<GameObject2D>
    private lateinit var pointsObjects: Array<GameObject2D>
    private lateinit var projectile: GameObject2D
    private lateinit var bossProjectile: BossProjectile2D
    private var projectiles: MutableList<GameObject2D> = ArrayList()
    private var bossProjectiles: MutableList<GameObject2D> = ArrayList()
    private lateinit var starObjects: Array<GameObject2D>

    private lateinit var enemy1: GameObject2D
    private lateinit var enemy2: GameObject2D
    private var boss: GameObject2D? = null
    private var enemyObjects: MutableList<GameObject2D> = ArrayList()

    private var enemyCount: Int = 0
    private var enemyFallSpeed: Int = 3
    private var nextX = 3
    private var bossFallSpeed: Int = 2
    private var gameMoveSpeed: Float = 7.5f
    private var projectileSpeed: Int = 15
    private var lastRenderTime: Long = 0L
    private var spriteRenderTime: Long = 0L

    private var playerScore: Int = 0

    private var projectionMatrix: FloatArray = FloatArray(4 * 4)

    //Dimension of the canvas
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    private var currentPlayerSprite: Int = 0
    private var playerPosition: Int = canvasWidth / 2
    private var playerHealth: Int = 3

    /**
     * Starts the EGL rendering.
     * Handled by the Android framework
     */
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val version = gl!!.glGetString(GL10.GL_VERSION)
        Log.w("Renderer", "Version: $version")

        gl.glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
        GLES30.glDisable(GLES30.GL_CULL_FACE)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
    }

    /**
     * Initializing game objects
     */
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        this.canvasWidth = width
        this.canvasHeight = height

        initShader()
        initGameObjects()
        initTexture()
        generateEnemies()

        Matrix.orthoM(
            projectionMatrix,
            0,
            0.0f,
            width.toFloat(),
            0.0f,
            height.toFloat(),
            0.0f,
            5.0f
        )
    }

    /**
     * Game loop
     */
    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        gameObject2DBackground.render(projectionMatrix)

        renderStars()
        renderPlayer()
        shoot()
        generateEnemies()
        renderEnemies()
        hitCheck()
        renderPoints()

        if (playerHealth == 0) {
            val data = Intent()
            data.putExtra("GameState", playerScore)
            val gameActivity = (this.context as GameActivity)
            gameActivity.setResult(RESULT_OK, data)
            gameActivity.finish()
        }
    }

    /**
     * Handling touch event to move the player
     */
    fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val touchWith = event.x.toInt()
                    // 150 is the player obj's width, we subtracts it to keep the player in-screen
                    // if we pull the player too close the edges
                    playerPosition = if (touchWith > (canvasWidth - 150)) {
                        canvasWidth - 150
                    } else {
                        touchWith
                    }
                }
            }
        }
        return true
    }

    private fun renderPlayer() {
        val player = playerObject[currentPlayerSprite]
        player.x = playerPosition
        player.render(projectionMatrix)

        val millis = System.currentTimeMillis()
        // 30FPS limiting: 1000 / 30 = 33.333... ~= 33
        // to limit the animation speed
        if (33L < (millis - spriteRenderTime)) {
            spriteRenderTime = millis
            if (++currentPlayerSprite > playerObject.size - 1) {
                currentPlayerSprite = 0
            }
        }
    }

    private fun shoot() {
        // shoot timeout
        if (System.currentTimeMillis() - lastRenderTime > 700L) {
            projectiles.add(projectile.copy(playerPosition + 75))
            if (boss != null) {
                bossProjectiles.add(bossProjectile.copy(boss!!.x, boss!!.y))
            }
            lastRenderTime = System.currentTimeMillis()
        }

        // render those projectiles that are inside of the screen
        for (projectile in projectiles) {
            projectile.y += projectileSpeed
            if (projectile.y < canvasHeight) {
                projectile.render(projectionMatrix)
            }
        }

        // render those boss projectiles that are inside of the screen
        for (projectile in bossProjectiles) {
            projectile.y -= projectileSpeed
            if (projectile.y > 0) {
                projectile.render(projectionMatrix)
            }
        }

        // filter out those projectiles that are out side of the screen to avoid memory leaks
        projectiles = projectiles.filter { it.y < canvasHeight }.toMutableList()
        bossProjectiles = bossProjectiles.filter { it.y < canvasHeight }.toMutableList()
    }

    /**
     * Animated background
     */
    private fun renderStars() {
        if (starObjects[0].y <= -canvasHeight) {
            val g0 = starObjects[0]
            val g1 = starObjects[1]
            starObjects[0] = g1
            starObjects[1] = g0
            g0.y = canvasHeight
        }
        starObjects.forEach {
            it.render(projectionMatrix)
            moveGameObject(it)
        }
    }

    /**
     * Moves the provided game object with the game spedd
     */
    private fun moveGameObject(it: GameObject2D) {
        it.y -= gameMoveSpeed.toInt() // TODO: Should be pixel/s
    }

    private fun renderEnemies() {
        for (enemy in enemyObjects) {
            // moving enemies (falling)
            enemy.y -= enemyFallSpeed
            // if they are at the same y coords or below
            // the player looses 1 health point
            if (enemy.y > 0) {
                enemy.render(projectionMatrix)
            } else if (playerHealth > 0) {
                playerHealth--
            }
        }

        // moving the boss (if exists)
        // side-to-side and down
        if (boss != null) {
            // !! is needed because kotlin cannot validate at compile time the boss is not null
            // error says "other threads may modified the value of 'boss'"
            // https://kotlinlang.org/docs/reference/null-safety.html#the--operator
            boss!!.y -= bossFallSpeed
            boss!!.x += nextX
            if ((boss!!.x > canvasWidth - 150) || boss!!.x < 0) {
                nextX *= -1
            }

            // player will loose if the boss is at the same y coordinate
            if (boss!!.y > 0) {
                boss!!.render(projectionMatrix)
            } else {
                playerHealth = 0
            }
        }
    }

    private fun hitCheck() {
        for (projectile in projectiles) {
            for (enemy in enemyObjects) {
                // the consts (50, 150) are needed to correctly check hits
                // so the hit check applies to the whole width of the player and projectile
                // not just its center coords
                if (
                    projectile.x >= enemy.x &&
                    projectile.x <= enemy.x + 150 &&
                    projectile.y + 50 >= enemy.y
                ) {
                    playerScore++
                    enemy.health--
                    projectile.health--
                }
            }

            // same as above just for the boss
            if (boss != null &&
                (projectile.x >= boss!!.x &&
                        projectile.x <= boss!!.x + 300 &&
                        projectile.y + 50 >= boss!!.y)
            ) {
                playerScore++
                boss!!.health--
                // if the boss got a hit from the player jumps to a random x position
                boss!!.x = Random.nextInt(0, canvasWidth - 300)
                projectile.health--
            }
        }

        val player = playerObject[currentPlayerSprite]
        for (projectile in bossProjectiles) {
            // same as above just for the boss projectiles
            if (
                projectile.x + 145 >= player.x &&
                projectile.x <= player.x + 145 &&
                projectile.y <= 75
            ) {
                projectile.health--
                playerHealth--
            }
        }

        // if the boss got no health remove it
        if (boss != null && boss!!.health < 1) {
            boss = null
        }

        // filtering out the enemies that are out of the screen, or has no health
        enemyObjects = enemyObjects.filter { it.health > 0 && it.y > 0 }.toMutableList()
        // filtering out the projectiles that are out of the screen, or has no health
        projectiles = projectiles.filter { it.health > 0 && it.y < canvasHeight }.toMutableList()
        bossProjectiles = bossProjectiles.filter { it.health > 0 && it.y > 0 }.toMutableList()
    }

    /**
     * Render player points
     */
    private fun renderPoints() {
        //render 1 digit player score
        if (playerScore < 10) {
            pointsObjects[playerScore].render(projectionMatrix)
        // Render 2 digit player score, by moving the 1st digit a bit left
        } else if (playerScore < 100) {
            val stringPlayerScore = playerScore.toString()
            val firstScoreNumber = stringPlayerScore.substring(0, 1).toInt()
            val secondScoreNumber = stringPlayerScore.substring(1).toInt()
            pointsObjects[firstScoreNumber].let {
                it.x -= 100
                it.render(projectionMatrix)
                it.x += 100
            }
            pointsObjects[secondScoreNumber].render(projectionMatrix)
        // Render 3 digit player score, by moving the 1st and 2nd digits a bit left
        } else {
            val stringPlayerScore = playerScore.toString()
            val firstScoreNumber = stringPlayerScore.substring(0, 1).toInt()
            val secondScoreNumber = stringPlayerScore.substring(1, 2).toInt()
            val thirdScoreNumber = stringPlayerScore.substring(2).toInt()
            pointsObjects[firstScoreNumber].let {
                it.x -= 200
                it.render(projectionMatrix)
                it.x += 200
            }
            pointsObjects[secondScoreNumber].let {
                it.x -= 100
                it.render(projectionMatrix)
                it.x += 100
            }
            pointsObjects[thirdScoreNumber].render(projectionMatrix)
        }
    }

    /**
     * Generates a random amount of enemies, if previous set is empty (killed)
     */
    private fun generateEnemies() {
        if (enemyObjects.isNotEmpty() || boss != null) {
            return
        }
        val enemyNum = Random.nextInt(1, 8)
        enemyCount += enemyNum
        enemyObjects = (1..enemyNum).map {
            //render them above (out of) the screen
            enemy1.copy().apply {
                x = Random.nextInt(0, 1290)
                y = 3200 + Random.nextInt(200, 500)
            }
        }.toMutableList()

        // if we rendered more then 10 we render a boss, a bit higher the the other enemies
        if (enemyCount > 10) {
            enemyCount = 0
            boss = enemy2.copy().apply {
                x = Random.nextInt(0, 1290)
                y = 3200 + Random.nextInt(200, 500)
                health = 10
            }
        }
    }

    /**
     * Loading game objects from sprite sheet by giving exact pixel positions
     */
    private fun initGameObjects() {
        gameObject2DBackground = gameObject2DManager.loadByHeight(
            R.raw.vertex,
            R.raw.fragment,
            0,
            0,
            320,
            480,
            3120f //TODO: This is predefined, ratio not good with height variable
        )

        starObjects = arrayOf(
            gameObject2DManager.loadByHeight(
                R.raw.vertex,
                R.raw.fragment,
                323,
                4,
                636,
                481,
                canvasHeight.toFloat()
            ).also {
                it.x = 0
                it.y = 0
            },
            gameObject2DManager.loadByHeight(
                R.raw.vertex,
                R.raw.fragment,
                323,
                4,
                636,
                481,
                canvasHeight.toFloat()
            ).also {
                it.x = 0
                it.y = canvasHeight
            }
        )

        playerObject = arrayOf(
            gameObject2DManager.loadByWidth(
                R.raw.vertex,
                R.raw.fragment,
                0,
                485,
                61,
                572,
                150f
            ).also {
                it.x = canvasWidth / 2
                it.y = 75
            },
            gameObject2DManager.loadByWidth(
                R.raw.vertex,
                R.raw.fragment,
                90,
                489,
                149,
                572,
                150f
            ).also {
                it.x = canvasWidth / 2
                it.y = 75
            },
            gameObject2DManager.loadByWidth(
                R.raw.vertex,
                R.raw.fragment,
                161,
                487,
                221,
                570,
                150f
            ).also {
                it.x = canvasWidth / 2
                it.y = 75
            },
            gameObject2DManager.loadByWidth(
                R.raw.vertex,
                R.raw.fragment,
                225,
                488,
                286,
                571,
                150f
            ).also {
                it.x = canvasWidth / 2
                it.y = 75
            },
            gameObject2DManager.loadByWidth(
                R.raw.vertex,
                R.raw.fragment,
                297,
                491,
                367,
                574,
                150f
            ).also {
                it.x = canvasWidth / 2
                it.y = 75
            },
            gameObject2DManager.loadByWidth(
                R.raw.vertex,
                R.raw.fragment,
                366,
                489,
                427,
                571,
                150f
            ).also {
                it.x = canvasWidth / 2
                it.y = 75
            },
            gameObject2DManager.loadByWidth(
                R.raw.vertex,
                R.raw.fragment,
                433,
                489,
                494,
                574,
                150f
            ).also {
                it.x = canvasWidth / 2
                it.y = 75
            },
            gameObject2DManager.loadByWidth(
                R.raw.vertex,
                R.raw.fragment,
                499,
                489,
                558,
                578,
                150f
            ).also {
                it.x = canvasWidth / 2
                it.y = 75
            },
            gameObject2DManager.loadByWidth(
                R.raw.vertex,
                R.raw.fragment,
                568,
                488,
                626,
                580,
                150f
            ).also {
                it.x = canvasWidth / 2
                it.y = 75
            },
            gameObject2DManager.loadByWidth(
                R.raw.vertex,
                R.raw.fragment,
                629,
                490,
                686,
                581,
                150f
            ).also {
                it.x = canvasWidth / 2
                it.y = 75
            },
            gameObject2DManager.loadByWidth(
                R.raw.vertex,
                R.raw.fragment,
                698,
                488,
                756,
                577,
                150f
            ).also {
                it.x = canvasWidth / 2
                it.y = 75
            })

        projectile = gameObject2DManager.loadByWidth(
            R.raw.vertex,
            R.raw.fragment,
            62,
            492,
            86,
            572,
            50f
        ).also {
            it.x = playerPosition
            it.y = 75
        }

        enemy1 = gameObject2DManager.loadByWidth(
            R.raw.vertex,
            R.raw.fragment,
            0,
            582,
            61,
            675,
            150f
        )

        enemy2 = gameObject2DManager.loadByWidth(
            R.raw.vertex,
            R.raw.fragment,
            0,
            686,
            58,
            781,
            300f
        )

        val numberTop = 789
        val numberBottom = 935

        pointsObjects = arrayOf(
            gameObject2DManager.loadByHeight(
                R.raw.vertex,
                R.raw.fragment,
                3,
                numberTop,
                89,
                numberBottom,
                150f
            ).also {
                it.x = canvasWidth - 200
                it.y = canvasHeight - 300
            },

            gameObject2DManager.loadByHeight(
                R.raw.vertex,
                R.raw.fragment,
                107,
                numberTop,
                152,
                numberBottom,
                150f
            ).also {
                it.x = canvasWidth - 200
                it.y = canvasHeight - 300
            },
            gameObject2DManager.loadByHeight(
                R.raw.vertex,
                R.raw.fragment,
                176,
                numberTop,
                263,
                numberBottom,
                150f
            ).also {
                it.x = canvasWidth - 200
                it.y = canvasHeight - 300
            },
            gameObject2DManager.loadByHeight(
                R.raw.vertex,
                R.raw.fragment,
                278,
                numberTop,
                365,
                numberBottom,
                150f
            ).also {
                it.x = canvasWidth - 200
                it.y = canvasHeight - 300
            },
            gameObject2DManager.loadByHeight(
                R.raw.vertex,
                R.raw.fragment,
                387,
                numberTop,
                483,
                numberBottom,
                150f
            ).also {
                it.x = canvasWidth - 200
                it.y = canvasHeight - 300
            },
            gameObject2DManager.loadByHeight(
                R.raw.vertex,
                R.raw.fragment,
                506,
                numberTop,
                584,
                numberBottom,
                150f
            ).also {
                it.x = canvasWidth - 200
                it.y = canvasHeight - 300
            },
            gameObject2DManager.loadByHeight(
                R.raw.vertex,
                R.raw.fragment,
                607,
                numberTop,
                692,
                numberBottom,
                150f
            ).also {
                it.x = canvasWidth - 200
                it.y = canvasHeight - 300
            },
            gameObject2DManager.loadByHeight(
                R.raw.vertex,
                R.raw.fragment,
                710,
                numberTop,
                792,
                numberBottom,
                150f
            ).also {
                it.x = canvasWidth - 200
                it.y = canvasHeight - 300
            },
            gameObject2DManager.loadByHeight(
                R.raw.vertex,
                R.raw.fragment,
                814,
                numberTop,
                905,
                numberBottom,
                150f
            ).also {
                it.x = canvasWidth - 200
                it.y = canvasHeight - 300
            },
            gameObject2DManager.loadByHeight(
                R.raw.vertex,
                R.raw.fragment,
                926,
                numberTop,
                1006,
                numberBottom,
                150f
            ).also {
                it.x = canvasWidth - 200
                it.y = canvasHeight - 300
            },
        )

        bossProjectile = BossProjectile2D(
            gameObject2DManager.loadByHeight(
                R.raw.vertex,
                R.raw.fragment,
                95,
                602,
                128,
                633,
                150f
            ), canvasWidth
        ).also {
            it.x = canvasWidth / 2
            it.y = canvasHeight / 2
        }
    }

    /**
     * init shaders
     */
    private fun initShader() {
        shaderProgram = shaderManager.load(
            R.raw.vertex,
            R.raw.fragment,
            arrayOf("texture_sampler", "projectionMatrix", "objectPos")
        )
    }

    /**
     * init textures
     */
    private fun initTexture() {
        texture = textureManager.loadTexture(R.drawable.atlas)
    }
}