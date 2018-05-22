/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.hellosceneform

import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.widget.ImageView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnTouch
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Plane.Type
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */

val TAG = HelloSceneformActivity::class.java.simpleName

class HelloSceneformActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var spotLightYellow: Light

    @BindView(R.id.light_content)
    lateinit var lightBtnImageView: ImageView
    private var andyRenderable: ModelRenderable? = null
    //private ViewRenderable lightDirectionRdn;
    private var andy: TransformableNode? = null

    private enum class Mode {
        LIGHT, CONTENT
    }

    private var mode = Mode.CONTENT

    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_ux)

        ButterKnife.bind(this)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
            .setSource(this, R.raw.andy)
            .build()
            .thenAccept { renderable -> andyRenderable = renderable }
            .exceptionally { throwable ->
                val toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
                null
            }

        //    ViewRenderable.builder()
        //            .setView(this, R.layout.view_shadow_config)
        //            .build()
        //            .thenAccept(rdn -> lightDirectionRdn = rdn);


        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (andyRenderable == null) {
                return@setOnTapArPlaneListener
            }

            if (plane.type != Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }

            when (mode) {
                Mode.CONTENT -> {
                    // Create the Anchor.
                    val anchor = hitResult.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arFragment.arSceneView.scene)

                    // Create the transformable andy and add it to the anchor.
                    andy = TransformableNode(arFragment.transformationSystem).apply {
                        setParent(anchorNode)
                        renderable = andyRenderable
                        select()
                    }
                }
                Mode.LIGHT -> {
                    val anchor = hitResult.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arFragment.arSceneView.scene)
                    spotLightYellow = Light.builder(Light.Type.SPOTLIGHT)
                        .setColor(com.google.ar.sceneform.rendering.Color(Color.YELLOW))
                        .setShadowCastingEnabled(true)
                        .build()

                    val lightNode = Node()
                    //lightNode.setParent(anchorNode)
                    lightNode.setParent(arFragment.arSceneView.scene)
                    lightNode.light = spotLightYellow
                    lightNode.localPosition = Vector3(
                        anchorNode.localPosition.x,
                        anchorNode.localPosition.y + 0.1f,
                        anchorNode.localPosition.z)

                }
            }

        }
    }


    @OnTouch(R.id.up, R.id.down, R.id.left, R.id.right)
    internal fun controllerTouched(view: ImageView, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            ACTION_DOWN, ACTION_MOVE -> {
                startMove(view.id)
            }
        }
        return true
    }

    @OnClick(R.id.light_content)
    internal fun lightContentSelected() {
        when(mode) {
            Mode.LIGHT -> {
                mode = Mode.CONTENT
                lightBtnImageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_lightbulb_outline_white_24dp))
            }
            Mode.CONTENT -> {
                mode = Mode.LIGHT
                lightBtnImageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_lightbulb_outline_black_24dp))
            }
        }
    }

    private fun startMove(direction: Int) {
        when (direction) {
            R.id.up -> {
                moveAndy(0f, 0f, 0.01f)
            }
            R.id.down -> {
                moveAndy(0f, 0f, -0.01f)
            }
            R.id.left -> {
                moveAndy(-0.01f, 0f, 0f)
            }
            R.id.right -> {
                moveAndy(0.01f, 0f, 0f)
            }
            else -> {
            }
        }
    }

    private fun moveAndy(x: Float, y: Float, z: Float) {
        andy?.let {
            val currentPosition = it.localPosition
            val newPosition = Vector3(
                currentPosition.x + x,
                currentPosition.y + y,
                currentPosition.z + z)
            it.localPosition = newPosition
        }
    }
}
