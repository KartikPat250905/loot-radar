package com.example.freegameradar

import android.content.Context
import com.example.freegameradar.core.image.AndroidContextHolder
import com.google.firebase.FirebaseApp

fun init(context: Context) {
    AndroidContextHolder.context = context
    FirebaseApp.initializeApp(context)
}