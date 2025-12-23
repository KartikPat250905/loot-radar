package com.example.lootradarkmp

import android.content.Context
import com.example.lootradarkmp.core.image.AndroidContextHolder
import com.google.firebase.FirebaseApp

fun init(context: Context) {
    AndroidContextHolder.context = context
    FirebaseApp.initializeApp(context)
}