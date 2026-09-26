package com.huanli233.hibari.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.huanli233.hibari.animation.AnimatedVisibility
import com.huanli233.hibari.animation.MutableTransitionState
import com.huanli233.hibari.animation.animateFloat
import com.huanli233.hibari.animation.fadeIn
import com.huanli233.hibari.animation.fadeOut
import com.huanli233.hibari.animation.updateTransition
import com.huanli233.hibari.foundation.Box
import com.huanli233.hibari.foundation.Column
import com.huanli233.hibari.foundation.Node
import com.huanli233.hibari.foundation.attributes.alpha
import com.huanli233.hibari.foundation.attributes.matchParentSize
import com.huanli233.hibari.foundation.attributes.matchParentWidth
import com.huanli233.hibari.foundation.attributes.onClick
import com.huanli233.hibari.foundation.attributes.scaleX
import com.huanli233.hibari.foundation.attributes.scaleY
import com.huanli233.hibari.material.ConstraintLayout
import com.huanli233.hibari.material.Text
import com.huanli233.hibari.runtime.HibariView
import com.huanli233.hibari.runtime.attribute.attrs
import com.huanli233.hibari.runtime.currentContext
import com.huanli233.hibari.runtime.effects.LaunchedEffect
import com.huanli233.hibari.runtime.getValue
import com.huanli233.hibari.runtime.id
import com.huanli233.hibari.runtime.mutableStateOf
import com.huanli233.hibari.runtime.remember
import com.huanli233.hibari.runtime.setValue
import com.huanli233.hibari.ui.Modifier
import com.huanli233.hibari.ui.viewClass

class TestActivity: AppCompatActivity() {

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        var showContent by mutableStateOf(true)
        setContentView(
            HibariView(this) {
                val context = currentContext
                LaunchedEffect {
                    Toast.makeText(context, "Hello, World!", Toast.LENGTH_SHORT).show()
                }
                LaunchedEffect {
                    showContent = false
                }
                val transition = updateTransition(showContent)
                val scale by transition.animateFloat {
                    if (it) 3f else 1.5f
                }
                val alpha by transition.animateFloat {
                    if (it) 1f else 0.5f
                }
                Box(modifier = Modifier.matchParentSize().onClick { showContent = !showContent }) {
                    val textModifier = Modifier.gravity(Gravity.CENTER).scaleX(scale).scaleY(scale).alpha(alpha)
                    if (!showContent) {
                        Text(
                            "Hello, World!",
                            textModifier
                        )
                    } else {
                        Text(
                            "World, Hello!",
                            textModifier
                        )
                    }
                    Column(matchParentWidth().gravity(Gravity.CENTER)) {
                        AnimatedVisibility(
                            showContent,
                            matchParentSize(),
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Text("test")
                        }
                    }
                    // ConstraintLayout smoke test. The nested layout used to leave its ConstraintSet
                    // behind for the sibling emitted after it, so "cl bottom" ended up constrained
                    // inside the inner layout and disappeared. Children carry an explicit id because
                    // an anonymous one is regenerated on every retune.
                    ConstraintLayout(modifier = Modifier.matchParentSize()) {
                        Text(
                            "cl top",
                            Modifier.id("cl-top").constraint {
                                top constraintTo parent.top
                                start constraintTo parent.start
                            }
                        )
                        ConstraintLayout(
                            modifier = Modifier.id("cl-inner").constraint {
                                top constraintTo parent.top
                                end constraintTo parent.end
                            }
                        ) {
                            Text(
                                "cl nested",
                                Modifier.id("cl-nested").constraint {
                                    top constraintTo parent.top
                                }
                            )
                        }
                        Text(
                            "cl bottom",
                            Modifier.id("cl-bottom").constraint {
                                bottom constraintTo parent.bottom
                                start constraintTo parent.start
                            }
                        )
                    }
                    // Runtime-attrs smoke test: textColor/textSize arrive only through the
                    // synthesized AttributeSet, not through any typed Modifier.
                    Node(
                        modifier = Modifier
                            .gravity(Gravity.CENTER)
                            .viewClass(android.widget.TextView::class.java)
                            .attrs {
                                set("android:text", "runtime attrs")
                                set("android:textColor", 0xFFE91E63.toInt())
                                set("android:textSize", "22sp")
                            }
                    )
                }
            }
        )
    }

}