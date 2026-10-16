package com.keremtulu.questionnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/** G2 compile-only shell. The first CI build intentionally has no application interface. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { }
    }
}
