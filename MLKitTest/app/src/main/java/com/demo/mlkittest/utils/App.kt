package com.demo.mlkittest.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.demo.mlkittest.R

class App : Application(), Application.ActivityLifecycleCallbacks {
    private var activityReferences = 0
    private var isActivityChangingConfigurations = false
    private val RAM_POLL_INTERVAL = 30000 //30000
    private var isAppInForeground = true
    private val isLowMemoryAlertOkToShow = true
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    /* override fun onActivityStarted(activity: Activity) {
         currentActivity = activity
         if (++activityReferences == 1 && !isActivityChangingConfigurations) {
             // com.lucidea.argusmobile.App enters foreground
             isAppInForeground = true
             val tabLayout: TabLayout = activity.findViewById<TabLayout>(R.id.tab_layout)
             val tabNumber = if (tabLayout == null) -1 else tabLayout.getSelectedTabPosition()
             val prefs: SharedPreferences = activity.getSharedPreferences(PREFS_KEY, MODE_PRIVATE)
             if (tabNumber == 0 || tabNumber == 1) {
                 if (showDialog) {
                     Utils.showDoneByConfirmation(activity)
                 } else {
                     showDialog = true
                 }
             } else {
                 val editor: SharedPreferences.Editor = prefs.edit()
                 editor.putBoolean("needToShowDialog", true)
                 editor.apply()
             }
         }
     }
 */
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {
        currentActivity = activity
        isActivityChangingConfigurations = activity.isChangingConfigurations
        /* if (--activityReferences == 0 && !isActivityChangingConfigurations) {
             // com.lucidea.argusmobile.App enters background
             showDialog = true
             isAppInForeground = false
         }*/
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        var showDialog = true
        var currentActivity: Activity? = null
        const val LOW_MEMORY_AMOUNT = 500 // 500
        var QRCaptureRef: AppCompatActivity? = null
        var isDialogShowing = false
        var QrScanBoxArea= 0.0

    }
}