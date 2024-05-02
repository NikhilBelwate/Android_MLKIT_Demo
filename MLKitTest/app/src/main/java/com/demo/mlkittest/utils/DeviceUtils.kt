package com.demo.mlkittest.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
class DeviceUtils {
    companion object {
        fun isTablet(context: Context): Boolean {
            val configuration = context.resources.configuration
            val xlarge = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_XLARGE
            val large = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
            return xlarge || large
        }

        fun isTabletByDensity(context: Context): Boolean {
            val displayMetrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            val density = displayMetrics.density
            return density >= 1.5 // You can adjust this threshold based on your requirements
        }

        fun isTabletByDeviceType(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2 && (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N && !Build.VERSION.RELEASE.startsWith("6.")))
        }
    }
}