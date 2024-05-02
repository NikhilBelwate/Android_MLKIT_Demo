package com.demo.mlkittest.utils

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import com.demo.mlkittest.R

class Alerts {
    companion object {
        private const val TAG = "Alerts.kt"

        /**
         * Static method to call an alert
         * @param activity current activity
         * @param title Title of alert
         * @param message message in alert
         */
        fun alertDialog(activity: Context, title: String, message: String) {
            val dialog = AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("ok") { dialog1, id -> dialog1.cancel() }
                .show()

            val keep = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            keep.transformationMethod = null
            keep.setTextColor(activity.resources.getColor(R.color.black, activity.theme))
        }

        /**
         * Static method to call popup with confirm or cancel
         * @param activity calling activity
         * @param title title of popup
         * @param message message of popup
         * @param negative cancel button message
         * @param positive ok button message
         * @param negListener Callback for when cancel button is clicked
         * @param posListener Callback for when ok button is clicked
         */
        fun alertConfirm(
            activity: Context,
            title: String,
            message: String,
            negative: String,
            positive: String,
            negListener: DialogInterface.OnClickListener,
            posListener: DialogInterface.OnClickListener
        ) {

            val dialog = AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(negative, negListener)
                .setPositiveButton(positive, posListener)
                .show()

            val keep = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            keep.transformationMethod = null
            keep.setTextColor(activity.resources.getColor(R.color.black, activity.theme))

            val clear = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            clear.transformationMethod = null
            clear.setTextColor(Color.RED)
        }

        fun alertConfirm(
            activity: Context,
            title: String,
            message: String,
            positive: String,
            posListener: DialogInterface.OnClickListener
        ) {

            val dialog = AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, posListener)
                .show()

            val keep = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            keep.transformationMethod = null
            keep.setTextColor(activity.resources.getColor(R.color.black, activity.theme))

            val clear = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            clear.transformationMethod = null
            clear.setTextColor(Color.RED)
        }
    }
}