package com.radarlabs.freegameradar

import android.content.Context
import com.radarlabs.freegameradar.core.image.AndroidContextHolder
import com.google.firebase.FirebaseApp

fun init(context: Context) {
    AndroidContextHolder.context = context
    FirebaseApp.initializeApp(context)
}