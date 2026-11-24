package com.radarlabs.freegameradar

import android.content.pm.ApplicationInfo
import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun isDebugBuild(): Boolean =
    (FreeGameRadarApp.appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0